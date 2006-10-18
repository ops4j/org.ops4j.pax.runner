package biz.aqute.lib.osgi;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

public class Macro
{

    Properties properties;
    Processor domain;

    public Macro( Properties properties, Processor domain )
    {
        this.properties = properties;
        this.domain = domain;
    }

    public Macro( Processor processor )
    {
        this( new Properties(), processor );
    }

    public String process( String line )
    {
        StringBuffer sb = new StringBuffer();
        process( line, 0, '\0', sb );
        return sb.toString();
    }

    int process( String line, int index, char type, StringBuffer result )
    {
        StringBuffer variable = new StringBuffer();
        outer:
        while( index < line.length() )
        {
            char c1 = line.charAt( index++ );
            if( ( ( type == '(' && c1 == ')' ) || ( type == '{' && c1 == '}' ) ) )
            {
                result.append( replace( variable.toString() ) );
                return index;
            }

            if( c1 == '$' && index < line.length() - 2 )
            {
                char c2 = line.charAt( index );
                if( c2 == '(' || c2 == '{' )
                {
                    index = process( line, index + 1, c2, variable );
                    continue outer;
                }
            }
            variable.append( c1 );
        }
        result.append( variable );
        return index;
    }

    protected String replace( String key )
    {
        String value = properties.getProperty( key );
        if( value != null )
        {
            return value;
        }

        value = doCommands( key );
        if( value != null )
        {
            return value;
        }

        value = System.getProperty( key );
        if( value != null )
        {
            return value;
        }

        return "${" + key + "}";
    }

    /**
     * Parse the key as a command. A command consist of parameters separated by
     * ':'.
     *
     * @param key
     * @return
     */
    static Pattern commands = Pattern.compile( ";" );

    private String doCommands( String key )
    {
        String[] args = commands.split( key );
        if( args == null || args.length == 0 )
        {
            return null;
        }

        String result = doCommand( domain, args );
        if( result != null )
        {
            return result;
        }

        return doCommand( this, args );
    }

    private String doCommand( Object target, String[] args )
    {
        String cname = "_" + args[ 0 ].replaceAll( "-", "_" );
        try
        {
            Method m = target.getClass().getMethod(
                cname,
                new Class[]{ String[].class }
            );
            return (String) m.invoke( target, new Object[]{ args } );
        }
        catch( Exception e )
        {
            // Ignore
        }
        return null;
    }

    public String _filter( String args[] )
    {
        if( args.length != 3 )
        {
            domain.warning( "Invalid nr of arguments to filter "
                            + Arrays.asList( args )
            );
            return null;
        }

        String list[] = args[ 1 ].split( "\\s*,\\s*" );
        StringBuffer sb = new StringBuffer();
        String del = "";
        for( int i = 0; i < list.length; i++ )
        {
            if( list[ i ].matches( args[ 2 ] ) )
            {
                sb.append( del );
                sb.append( list[ i ] );
                del = ", ";
            }
        }
        return sb.toString();
    }

    public String _filterout( String args[] )
    {
        if( args.length != 3 )
        {
            domain.warning( "Invalid nr of arguments to filterout "
                            + Arrays.asList( args )
            );
            return null;
        }

        String list[] = args[ 1 ].split( "\\s*,\\s*" );
        StringBuffer sb = new StringBuffer();
        String del = "";
        for( int i = 0; i < list.length; i++ )
        {
            if( !list[ i ].matches( args[ 2 ] ) )
            {
                sb.append( del );
                sb.append( list[ i ] );
                del = ", ";
            }
        }
        return sb.toString();
    }

    public String _sort( String args[] )
    {
        if( args.length != 2 )
        {
            domain.warning( "Invalid nr of arguments to join "
                            + Arrays.asList( args )
            );
            return null;
        }

        String list[] = args[ 1 ].split( "\\s*,\\s*" );
        StringBuffer sb = new StringBuffer();
        String del = "";
        Arrays.sort( list );
        for( int i = 0; i < list.length; i++ )
        {
            sb.append( del );
            sb.append( list[ i ] );
            del = ", ";
        }
        return sb.toString();
    }

    public String _join( String args[] )
    {
        if( args.length == 1 )
        {
            domain.warning( "Invalid nr of arguments to join "
                            + Arrays.asList( args )
            );
            return null;
        }

        StringBuffer sb = new StringBuffer();
        String del = "";
        for( int i = 1; i < args.length; i++ )
        {
            String list[] = args[ i ].split( "\\s*,\\s*" );
            for( int j = 0; j < list.length; j++ )
            {
                sb.append( del );
                sb.append( list[ j ] );
                del = ", ";
            }
        }
        return sb.toString();
    }

    public String _if( String args[] )
    {
        if( args.length < 3 )
        {
            domain.warning( "Invalid nr of arguments to if "
                            + Arrays.asList( args )
            );
            return null;
        }

        if( args[ 1 ].trim().length() != 0 )
        {
            return args[ 2 ];
        }
        if( args.length > 3 )
        {
            return args[ 3 ];
        }
        else
        {
            return "";
        }
    }

    public String _now( String args[] )
    {
        return new Date().toString();
    }

    public String _fmodified( String args[] )
        throws Exception
    {
        if( args.length != 2 )
        {
            domain.warning( "Fmodified takes only 1 parameter "
                            + Arrays.asList( args )
            );
            return null;
        }
        long time = 0;
        String list[] = args[ 1 ].split( "\\s*,\\s*" );
        for( int i = 0; i < list.length; i++ )
        {
            File f = new File( list[ i ].trim() );
            if( f.exists() && f.lastModified() > time )
            {
                time = f.lastModified();
            }
        }
        return "" + time;
    }

    public String _long2date( String args[] )
    {
        try
        {
            return new Date( Long.parseLong( args[ 1 ] ) ).toString();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        return "not a valid long";
    }

    /**
     * @param args
     */
    public String _replace( String args[] )
    {
        if( args.length != 4 )
        {
            domain.warning( "Invalid nr of arguments to replace "
                            + Arrays.asList( args )
            );
            return null;
        }

        String list[] = args[ 1 ].split( "\\s*,\\s*" );
        StringBuffer sb = new StringBuffer();
        String del = "";
        for( int i = 0; i < list.length; i++ )
        {
            String element = list[ i ].trim();
            if( !element.equals( "" ) )
            {
                sb.append( del );
                sb.append( element.replaceAll( args[ 2 ], args[ 3 ] ) );
                del = ", ";
            }
        }

        return sb.toString();
    }

}
