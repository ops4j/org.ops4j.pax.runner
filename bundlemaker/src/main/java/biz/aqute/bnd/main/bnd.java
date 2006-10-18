package biz.aqute.bnd.main;

import biz.aqute.lib.osgi.Analyzer;
import biz.aqute.lib.osgi.Builder;
import biz.aqute.lib.osgi.Jar;
import biz.aqute.lib.osgi.Processor;
import biz.aqute.lib.osgi.Resource;
import biz.aqute.lib.osgi.Verifier;
import biz.aqute.lib.osgi.eclipse.EclipseClasspath;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.xml.sax.SAXException;

/**
 * Utility to make bundles.
 *
 * TODO Add Javadoc comment for this type.
 *
 * @version $Revision$
 */
public class bnd extends Processor
{

    PrintStream out = System.out;

    static boolean exceptions = false;

    static boolean failok = false;

    public static void main( String args[] )
    {
        bnd main = new bnd();
        try
        {
            main.run( args );

        }
        catch( Exception e )
        {
            System.err.println( "Software error occurred " + e );
            // if (exceptions)
            e.printStackTrace();
        }
    }

    void run( String[] args )
        throws Exception
    {
        int cnt = 0;
        for( int i = 0; i < args.length; i++ )
        {
            if( "-failok".equals( args[ i ] ) )
            {
                failok = true;
            }
            else if( "-exceptions".equals( args[ i ] ) )
            {
                exceptions = true;
            }
            else if( "wrap".equals( args[ i ] ) )
            {
                cnt++;
                doWrap( args, ++i );
                break;
            }
            else if( "print".equals( args[ i ] ) )
            {
                cnt++;
                doPrint( args, ++i );
                break;
            }
            else if( "build".equals( args[ i ] ) )
            {
                cnt++;
                doBuild( args, ++i );
                break;
            }
            else if( "eclipse".equals( args[ i ] ) )
            {
                cnt++;
                doEclipse( args, ++i );
                break;
            }
            else if( "help".equals( args[ i ] ) )
            {
                cnt++;
                doHelp( args, ++i );
                break;
            }
            else
            {
                cnt++;
                String path = args[ i ];
                if( path.endsWith( DEFAULT_BND_EXTENSION ) )
                {
                    doBuild(
                        new File( path ),
                        new File[0],
                        new File[0],
                        null,
                        "",
                        false
                    );
                }
                else if( path.endsWith( DEFAULT_JAR_EXTENSION )
                         || path.endsWith( DEFAULT_BAR_EXTENSION ) )
                {
                    doPrint( path, -1 );
                }
                else
                {
                    doHelp( args, i );
                    break;
                }
            }
        }

        if( cnt == 0 )
        {
            doBuild(
                new File( "bnd.bnd" ),
                new File[0],
                new File[0],
                null,
                "",
                false
            );
        }
        int n = 1;
        switch( getErrors().size() )
        {
            case 0:
                // System.err.println("No errors");
                break;
            case 1:
                System.err.println( "One error" );
                break;
            default:
                System.err.println( getErrors().size() + " errors" );
        }
        for( Iterator i = getErrors().iterator(); i.hasNext(); )
        {
            String msg = (String) i.next();
            System.err.println( n++ + " : " + msg );
        }
        n = 1;
        switch( getWarnings().size() )
        {
            case 0:
                // System.err.println("No warnings");
                break;
            case 1:
                System.err.println( "One warning" );
                break;
            default:
                System.err.println( getErrors().size() + " warnings" );
        }
        for( Iterator i = getWarnings().iterator(); i.hasNext(); )
        {
            String msg = (String) i.next();
            System.err.println( n++ + " : " + msg );
        }

    }

    private void doEclipse( String[] args, int i )
        throws Exception
    {
        File dir = new File( "" ).getAbsoluteFile();
        if( args.length == i )
        {
            doEclipse( dir );
        }
        else
        {
            for( ; i < args.length; i++ )
            {
                doEclipse( new File( dir, args[ i ] ) );
            }
        }
    }

    private void doEclipse( File file )
        throws Exception
    {
        if( !file.isDirectory() )
        {
            error( "Eclipse requires a path to a directory: "
                   + file.getAbsolutePath()
            );
        }
        else
        {
            File cp = new File( file, ".classpath" );
            if( !cp.exists() )
            {
                error( "Cannot find .classpath in project directory: "
                       + file.getAbsolutePath()
                );
            }
            else
            {
                EclipseClasspath eclipse = new EclipseClasspath( file
                    .getParentFile(), file
                );
                out.printf( "Classpath    %s", eclipse.getClasspath() );
                out.println();
                out.printf( "Dependents   %s", eclipse.getDependents() );
                out.println();
                out.printf( "Sourcepath   %s", eclipse.getSourcepath() );
                out.println();
                out.printf( "Output       %s", eclipse.getOutput() );
                out.println();
            }
        }

    }

    private void doBuild( String[] args, int i )
        throws Exception
    {
        File[] classpath = new File[0];
        File[] sourcepath = new File[0];
        File output = null;
        String eclipse = "";
        boolean sources = false;

        for( ; i < args.length; i++ )
        {
            if( "-classpath".startsWith( args[ i ] ) )
            {
                String[] spaces = args[ ++i ].split( "\\s*,\\s*" );
                classpath = new File[spaces.length];
                for( int j = 0; j < spaces.length; j++ )
                {
                    File f = new File( spaces[ j ] );
                    if( !f.exists() )
                    {
                        error( "No such classpath entry: " + f.getAbsolutePath() );
                    }
                    classpath[ j ] = f;
                }
            }
            else if( "-sourcepath".startsWith( args[ i ] ) )
            {
                String arg = args[ ++i ];
                String[] spaces = arg.split( "\\s*,\\s*" );
                sourcepath = new File[spaces.length];
                for( int j = 0; j < spaces.length; j++ )
                {
                    File f = new File( spaces[ j ] );
                    if( !f.exists() )
                    {
                        error( "No such sourcepath entry: "
                               + f.getAbsolutePath()
                        );
                    }
                    sourcepath[ j ] = f;
                }
            }
            else if( "-eclipse".startsWith( args[ i ] ) )
            {
                eclipse = args[ ++i ];
            }
            else if( "-noeclipse".startsWith( args[ i ] ) )
            {
                eclipse = null;
            }
            else if( "-output".startsWith( args[ i ] ) )
            {
                output = new File( args[ ++i ] );
            }
            else if( "-sources".startsWith( args[ i ] ) )
            {
                sources = true;
            }
            else
            {
                File properties = new File( args[ i ] );
                if( !properties.exists() )
                {
                    error( "Cannot find properties file: " + args[ i ] );
                }
                else
                {
                    doBuild( properties, classpath, sourcepath, output, eclipse, sources );
                }
                output = null;
            }
        }

    }

    private void doBuild( File properties, File[] classpath, File[] sourcepath,
                          File output, String eclipse, boolean sources )
        throws Exception
    {

        Builder builder = new Builder();

        doEclipse( builder, properties, classpath, sourcepath, eclipse );

        builder.setClasspath( classpath );
        builder.setProperties( properties );
        if( sources )
        {
            builder.getProperties().setProperty( "-sources", "true" );
        }

        Jar jar = builder.build();
        getInfo( builder );
        if( getErrors().size() > 0 && !failok )
        {
            return;
        }

        if( output == null )
        {
            String name = properties.getAbsolutePath();
            int n = name.lastIndexOf( '.' );
            if( n > 0 )
            {
                name = name.substring( 0, n ) + DEFAULT_JAR_EXTENSION;
            }
            else
            {
                name = name + DEFAULT_JAR_EXTENSION;
            }

            output = new File( name );
        }

        jar.setName( output.getName() );
        jar.write( output );
        statistics( jar, output );
    }

    private void statistics( Jar jar, File output )
    {
        Integer size = new Integer( jar.getResources().size() );
        Integer length = new Integer( (int) output.length() );
        out.printf( "%-20s %10d resources %20d bytes", jar.getName(), size, length );
        out.println();
    }

    /**
     * @param properties
     * @param classpath
     * @param eclipse whether to do eclipse processing or not.
     * @param builder
     * @param sourcepath
     * @throws java.io.IOException if an IO problem occurs.
     */
    void doEclipse( Builder builder, File properties, File[] classpath,
                    File[] sourcepath, String eclipse )
        throws IOException
    {
        if( eclipse != null )
        {
            File project = new File( properties.getParentFile(), eclipse ).getAbsoluteFile();
            if( project.exists() && project.isDirectory() )
            {
                try
                {
                    EclipseClasspath path = new EclipseClasspath( project.getParentFile(), project );
                    List<File> newClasspath = Arrays.asList( classpath );
                    newClasspath.addAll( path.getClasspath() );
                    classpath = (File[]) newClasspath.toArray( classpath );

                    List<File> newSourcepath = Arrays.asList( sourcepath );
                    newSourcepath.addAll( path.getSourcepath() );
                    sourcepath = (File[]) newSourcepath.toArray( sourcepath );
                }
                catch( RuntimeException e )
                {
                    if( eclipse.length() > 0 )
                    {
                        error( "Eclipse specified (" + eclipse + ") but getting error processing: " + e );
                    }
                } catch( SAXException e )
                {
                    if( eclipse.length() > 0 )
                    {
                        error( "Eclipse specified (" + eclipse + ") but getting error processing: " + e );
                    }
                }
            }
            else
            {
                if( eclipse.length() > 0 )
                {
                    error( "Eclipse specified (" + eclipse + ") but no project directory found" );
                }
            }
        }
        builder.setClasspath( classpath );
        builder.setSourcepath( sourcepath );
    }

    private void doHelp( String[] args, int i )
    {
        if( args.length <= i )
        {
            System.out.println( "bnd -failok? -exceptions? ( wrap | print | build | eclipse )?" );
            System.out.println( "See http://www.aQute.biz/php/tools/bnd.php" );
        }
        else
        {
            while( args.length > i )
            {
                if( "wrap".equals( args[ i ] ) )
                {
                    System.out.println( "bnd wrap (-output <file>)? (-properties <file>)? <jar-file>" );
                }
                else if( "print".equals( args[ i ] ) )
                {
                    System.out.println( "bnd wrap -verify? -manifest? -list? -eclipse <jar-file>" );
                }
                else if( "build".equals( args[ i ] ) )
                {
                    System.out.println( "bnd build (-classpath <list>)? (-sourcepath <list>)? " );
                    System.out.println( "    -eclipse? -noeclipse? (-output <file>)? -sources? <bnd-file>" );
                }
                else if( "eclipse".equals( args[ i ] ) )
                {
                    System.out.println( "bnd eclipse" );
                }
                i++;
            }
        }
    }

    final static int VERIFY = 1;

    final static int MANIFEST = 2;

    final static int LIST = 4;

    final static int ECLIPSE = 8;

    private void doPrint( String[] args, int i )
        throws Exception
    {
        int options = 0;

        for( ; i < args.length; i++ )
        {
            if( "-verify".startsWith( args[ i ] ) )
            {
                options |= VERIFY;
            }
            else if( "-manifest".startsWith( args[ i ] ) )
            {
                options |= MANIFEST;
            }
            else if( "-list".startsWith( args[ i ] ) )
            {
                options |= LIST;
            }
            else if( "-eclipse".startsWith( args[ i ] ) )
            {
                options |= ECLIPSE;
            }
            else if( "-all".startsWith( args[ i ] ) )
            {
                options = -1;
            }
            else
            {
                doPrint( args[ i ], options );
            }
        }
    }

    private void doPrint( String string, int options )
        throws Exception
    {
        File file = new File( string );
        if( !file.exists() )
        {
            error( "File to print not found: " + string );
        }
        else
        {
            if( options == 0 )
            {
                options = -1;
            }

            Jar jar = new Jar( file.getName(), file );
            if( ( options & VERIFY ) != 0 )
            {
                Verifier verifier = new Verifier( jar );
                verifier.verify();
                getInfo( verifier );
            }
            if( ( options & MANIFEST ) != 0 )
            {
                Manifest manifest = jar.getManifest();
                if( manifest == null )
                {
                    warning( "JAR file has no manifest " + string );
                }
                else
                {
                    out.println( "[MANIFEST " + jar.getName() + "]" );
                    Set<String> sorted = new TreeSet<String>();
                    for( Object element : manifest.getMainAttributes().keySet() )
                    {
                        sorted.add( element.toString() );
                    }
                    for( String key : sorted )
                    {
                        Attributes attributes = manifest.getMainAttributes();
                        String value = attributes.getValue( key );
                        out.format( "%-28s %-50s", key, value );
                        out.println();
                    }
                }
                out.println();
            }
            if( ( options & LIST ) != 0 )
            {
                out.println( "[LIST " + jar.getName() + "]" );
                Map<String, Map<String, Resource>> directories = jar.getDirectories();
                for( Map.Entry<String, Map<String, Resource>> entry : directories.entrySet() )
                {
                    String name = entry.getKey();
                    Map<String,Resource> contents = entry.getValue();
                    out.println( name );
                    for( String element : contents.keySet() )
                    {
                        int n = element.lastIndexOf( '/' );
                        if( n > 0 )
                        {
                            element = element.substring( n + 1 );
                        }
                        out.print( "  " );
                        out.println( element );
                    }
                }
                out.println();
            }
        }
    }

    private void doWrap( String[] args, int i )
        throws Exception
    {
        int options = 0;
        File properties = null;
        File output = null;

        for( ; i < args.length; i++ )
        {
            if( "-output".startsWith( args[ i ] ) )
            {
                output = new File( args[ ++i ] );
            }
            else if( "-properties".startsWith( args[ i ] ) )
            {
                properties = new File( args[ ++i ] );
            }
            else
            {
                File bundle = new File( args[ i ] );
                doWrap( properties, bundle, output, options );
            }
        }
    }

    private void doWrap( File properties, File bundle, File output, int options )
        throws Exception
    {
        if( !bundle.exists() )
        {
            error( "No such file: " + bundle.getAbsolutePath() );
        }
        else
        {
            Analyzer analyzer = new Analyzer();
            analyzer.setJar( bundle );

            if( properties != null )
            {
                analyzer.setProperties( properties );
            }
            else
            {
                Properties p = new Properties();
                p.put( "Import-Package", "*" );
                p.put( "Created-By", "Made by bnd" );
                analyzer.setProperties( p );
            }

            if( output == null )
            {
                String path = bundle.getAbsolutePath() + "$";
                if( path.endsWith( DEFAULT_JAR_EXTENSION + "$" ) )
                {
                    path = path.replace( DEFAULT_JAR_EXTENSION + "$", DEFAULT_BAR_EXTENSION );
                }
                else
                {
                    path = bundle.getAbsolutePath() + DEFAULT_BAR_EXTENSION;
                }
                output = new File( path );
            }

            analyzer.calcManifest();
            Jar jar = analyzer.getJar();
            jar.write( output );
            statistics( jar, output );
        }
    }

}
