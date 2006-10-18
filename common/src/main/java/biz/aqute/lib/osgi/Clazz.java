package biz.aqute.lib.osgi;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Clazz
{

    static String type = "([BCDFIJSZ\\[]|L.+;)";

    static Pattern descriptor = Pattern.compile( "\\(" + type + "*\\)((" + type + ")|V)" );

    static byte SkipTable[] = {
        0, // 0 non existent
        -1, // 1 CONSTANT_utf8 UTF 8, handled in
        // method
        -1, // 2
        4, // 3 CONSTANT_Integer
        4, // 4 CONSTANT_Float
        8, // 5 CONSTANT_Long (index +=2!)
        8, // 6 CONSTANT_Double (index +=2!)
        -1, // 7 CONSTANT_Class
        2, // 8 CONSTANT_String
        4, // 9 CONSTANT_FieldRef
        4, // 10 CONSTANT_MethodRef
        4, // 11 CONSTANT_InterfaceMethodRef
        4, // 12 CONSTANT_NameAndType
    };
    Map<String,Map<String,String>> imports = new HashMap<String, Map<String, String>>();

    private String path;
    private boolean activator;
    private String className;

    public Clazz( String path, InputStream in )
        throws IOException
    {
        this.path = path;
        DataInputStream din = new DataInputStream( in );
        parseClassFile( din );
    }

    void parseClassFile( DataInputStream in )
        throws IOException
    {
        Set<Integer> classes = new HashSet<Integer>();
        Set<Integer> descriptors = new HashSet<Integer>();
        Hashtable pool = new Hashtable();
        try
        {
            int magic = in.readInt();
            if( magic != 0xCAFEBABE )
            {
                throw new IOException(
                    "Not a valid class file (no CAFEBABE header)"
                );
            }
            in.readShort(); // minor version
            in.readShort(); // major version
            int count = in.readUnsignedShort();
            process:
            for( int i = 1; i < count; i++ )
            {
                byte tag = in.readByte();
                switch( tag )
                {
                    case 0:
                        break process;
                    case 1:
                        // CONSTANT_Utf8
                        String name = in.readUTF();
                        pool.put( new Integer( i ), name );

                        // Check if this could be a descriptor.
                        // If so we assume it is.
                        if( name != null && name.length() > 0
                            && name.charAt( 0 ) == '('
                            && descriptor.matcher( name ).matches() )
                        {
                            parseDescriptor( name );
                        }
                        break;
                        // A Class constant is just a short reference in
                        // the constant pool
                    case 7:
                        // CONSTANT_Class
                        Integer index = new Integer( in.readShort() );
                        classes.add( index );
                        break;
                        // For some insane optimization reason are
                        // the long and the double two entries in the
                        // constant pool. See 4.4.5
                    case 5:
                        // CONSTANT_Long
                    case 6:
                        // CONSTANT_Double
                        in.skipBytes( 8 );
                        i++;
                        break;

                        // Interface Method Ref
                    case 12:
                        in.readShort(); // Name index
                        int descriptorIndex = in.readShort();
                        descriptors.add( new Integer( descriptorIndex ) );
                        break;

                        // We get the skip count for each record type
                        // from the SkipTable. This will also automatically
                        // abort when
                    default:
                        if( tag == 2 )
                        {
                            throw new IOException( "Invalid tag " + tag );
                        }
                        in.skipBytes( SkipTable[ tag ] );
                        break;
                }
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return;
        }
        //
        // Now iterate over all classes we found and
        // parse those as well. We skip duplicates
        //

        for( Integer n : classes )
        {
            String next = (String) pool.get( n );
            if( next != null )
            {
                String normalized = normalize( next );
                if( normalized != null )
                {
                    // For purposes of trying to guess the activator class, we
                    // assume
                    // that any class that references the BundleActivator
                    // interface
                    // must be a BundleActivator implementation.
                    if( normalized.startsWith( "org/osgi/framework/BundleActivator" ) )
                    {
                        className = path.replace( '/', '.' );
                        className = className.substring( 0, className.length() - ".class".length() );
                        activator = true;
                    }
                    String pack = getPackage( normalized );
                    packageReference( pack );
                }
            }
            else
            {
                throw new IllegalArgumentException( "Invalid class, parent=" );
            }
        }
        for( Iterator e = descriptors.iterator(); e.hasNext(); )
        {
            Integer n = (Integer) e.next();
            String prototype = (String) pool.get( n );
            if( prototype != null )
            {
                parseDescriptor( prototype );
            }
        }
    }

    void packageReference( String pack )
    {
        if( !imports.containsKey( pack ) )
        {
            imports.put( pack, new HashMap<String, String>() );
        }
    }

    void parseDescriptor( String prototype )
    {
        addReference( prototype );
        StringTokenizer st = new StringTokenizer( prototype, "(;)", true );
        while( st.hasMoreTokens() )
        {
            if( "(".equals( st.nextToken() ) )
            {
                String token = st.nextToken();
                while( !")".equals( token ) )
                {
                    addReference( token );
                    token = st.nextToken();
                }
                token = st.nextToken();
                addReference( token );
            }
        }
    }

    private void addReference( String token )
    {
        if( token.startsWith( "L" ) )
        {
            String clazz = normalize( token.substring( 1 ) );
            if( clazz.startsWith( "java/" ) )
            {
                return;
            }
            String pack = getPackage( clazz );
            packageReference( pack );
        }
    }

    static String normalize( String s )
    {
        if( s.startsWith( "[L" ) )
        {
            return normalize( s.substring( 2 ) );
        }
        if( s.startsWith( "[" ) )
        {
            if( s.length() == 2 )
            {
                return null;
            }
            else
            {
                return normalize( s.substring( 1 ) );
            }
        }
        if( s.endsWith( ";" ) )
        {
            return normalize( s.substring( 0, s.length() - 1 ) );
        }
        return s + ".class";
    }

    public static String getPackage( String clazz )
    {
        int n = clazz.lastIndexOf( '/' );
        if( n < 0 )
        {
            return ".";
        }
        return clazz.substring( 0, n ).replace( '/', '.' );
    }

    public Map<String,Map<String,String>> getReferred()
    {
        return imports;
    }

    String getClassName()
    {
        return className;
    }

    boolean isActivator()
    {
        return activator;
    }

    public String getPath()
    {
        return path;
    }

}
