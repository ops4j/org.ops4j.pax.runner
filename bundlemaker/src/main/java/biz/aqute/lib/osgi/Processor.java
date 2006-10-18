package biz.aqute.lib.osgi;

import biz.aqute.qtokens.QuotedTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor
{

    public final static String DEFAULT_BND_EXTENSION = ".bnd";
    public final static String DEFAULT_JAR_EXTENSION = ".jar";
    public final static String DEFAULT_BAR_EXTENSION = ".bar";

    List<String> errors = new ArrayList<String>();
    List<String> warnings = new ArrayList<String>();

    public void getInfo( Processor processor )
    {
        errors.addAll( processor.errors );
        warnings.addAll( processor.warnings );
    }

    public void warning( String string )
    {
        warnings.add( string );
    }

    public void error( String string )
    {
        errors.add( string );
    }

    public List<String> getWarnings()
    {
        return warnings;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    /**
     * Standard OSGi header parser. This parser can handle the format clauses
     * ::= clause ( ',' clause ) + clause ::= name ( ';' name ) (';' key '='
     * value )
     *
     * This is mapped to a Map { name => Map { attr|directive => value } }
     *
     * @param value
     * @return
     */
    static public Map<String, Map<String,String>> parseHeader( String value )
    {
        if( value == null || value.trim().length() == 0 )
        {
            return new HashMap<String, Map<String, String>>();
        }

        Map<String, Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
        QuotedTokenizer qt = new QuotedTokenizer( value, ";=," );
        char del = ',';
        while( del == ',' );
        {
            boolean hadAttribute = false;
            Map<String,String> clause = new HashMap<String, String>();
            List<String> aliases = new ArrayList<String>();
            aliases.add( qt.nextToken() );
            del = qt.getSeparator();
            while( del == ';' )
            {
                String adname = qt.nextToken();
                if( ( del = qt.getSeparator() ) != '=' )
                {
                    if( hadAttribute )
                    {
                        String message = "Header contains name field after attribute or directive: ";
                        String s = message + adname + " from " + value;
                        throw new IllegalArgumentException( s );
                    }
                    aliases.add( adname );
                }
                else
                {
                    String advalue = qt.nextToken();
                    clause.put( adname, advalue );
                    del = qt.getSeparator();
                    hadAttribute = true;
                }
            }
            for( String aliase : aliases )
            {
                result.put( aliase, clause );
            }
        }
        return result;
    }

    Map analyzeBundleClasspath( Jar dot, Map<String, Map<String,String>> bundleClasspath, Map contained, Map referred, Map uses )
        throws IOException
    {
        Map<String, Clazz> classSpace = new HashMap<String, Clazz>();

        if( bundleClasspath.isEmpty() )
        {
            analyzeJar( dot, "", classSpace, contained, referred, uses );
        }
        else
        {
            for( String path : bundleClasspath.keySet() )
            {
                //
                // There are 3 cases:
                // - embedded JAR file
                // - directory
                // - error
                //

                Resource resource = dot.getResource( path );
                if( resource != null )
                {
                    try
                    {
                        Jar jar = new Jar( path );
                        EmbeddedResource.build( jar, resource.openInputStream() );
                        analyzeJar( jar, "", classSpace, contained, referred, uses );
                    }
                    catch( IOException e )
                    {
                        warning( "Invalid bundle classpath entry: " + path + " " + e );
                    }
                }
                else
                {
                    if( dot.getDirectories().containsKey( path ) )
                    {
                        analyzeJar( dot, path, classSpace, contained, referred, uses );
                    }
                    else
                    {
                        warning( "No sub JAR or directory " + path );
                    }
                }
            }
        }
        return classSpace;
    }

    /**
     * We traverse through al the classes that we can find and calculate the
     * contained and referred set and uses. This method ignores the Bundle
     * classpath.
     *
     * @param jar
     * @param contained
     * @param referred
     * @param uses
     * @param prefix
     * @param classSpace
     * @throws IOException
     */
    private void analyzeJar( Jar jar,
                             String prefix,
                             Map<String, Clazz> classSpace, Map<String, Map<String,String>> contained,
                             Map<String, Map<String, String>> referred,
                             Map<String,Set<String>> uses )
        throws IOException
    {
        Map<String, Resource> resources = jar.getResources();
        for( String path : resources.keySet() )
        {
            if( path.startsWith( prefix ) )
            {
                if( path.endsWith( ".class" ) )
                {
                    Resource resource = jar.getResource( path );
                    String pathOfClass = path.substring( prefix.length() );
                    Clazz clazz = new Clazz( pathOfClass, resource.openInputStream() );
                    classSpace.put( pathOfClass, clazz );
                    String pack = Clazz.getPackage( pathOfClass );
                    if( !contained.containsKey( pack ) )
                    {
                        Map<String, String> map = new HashMap<String, String>();
                        contained.put( pack, map );
                        Resource pinfo = jar.getResource( prefix + pack.replace( '.', '/' ) + "/packageinfo" );
                        if( pinfo != null )
                        {
                            String version = parsePackageInfo( pinfo.openInputStream() );
                            if( version != null )
                            {
                                map.put( "version", version );
                            }
                        }
                    }
                    referred.putAll( clazz.getReferred() );

                    // Add all the used packages
                    // to this package
                    Set<String> t = uses.get( pack );
                    if( t == null )
                    {
                        t = new HashSet<String>();
                        uses.put( pack, t );
                    }
                    Map<String, Map<String, String>> referredClasses = clazz.getReferred();
                    t.addAll( referredClasses.keySet() );
                    t.remove( pack );
                }
            }
        }
    }

    static Pattern packageinfo = Pattern.compile( "version\\s+([0-9.]+).*" );

    static String parsePackageInfo( InputStream jar )
        throws IOException
    {
        try
        {
            byte[] buf = EmbeddedResource.collect( jar, 0 );
            String line = new String( buf ).trim();
            Matcher m = packageinfo.matcher( line );
            if( m.matches() )
            {
                return m.group( 1 );
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    static Map<String, Map<String, String>> removeKeys( Map source, String prefix )
    {
        Map<String, Map<String, String>> temp = new TreeMap<String, Map<String, String>>();
        Iterator<String> p = temp.keySet().iterator();
        while( p.hasNext() )
        {
            String pack = p.next();
            if( pack.startsWith( prefix ) )
            {
                p.remove();
            }
        }
		return temp;
	}

}
