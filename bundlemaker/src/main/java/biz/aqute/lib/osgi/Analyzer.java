package biz.aqute.lib.osgi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

/**
 * This class can calculate the required headers for a
 * (potential) JAR file. It analyzes a directory or JAR
 * for the packages that are contained and that are referred to
 * by the bytecodes. The user can the use regular expressions
 * to define the attributes and directives. The matching is
 * not fully regex for convenience. A * and ? get a . prefixed
 * and dots are escaped.
 * <pre>
 * 			*;auto=true				any
 * 			org.acme.*;auto=true    org.acme.xyz
 * 			org.[abc]*;auto=true    org.acme.xyz
 * </pre>
 * Additional, the package instruction can start with a
 * '=' or a '!'. The '!' indicates negation. Any matching
 * package is removed. The '=' is literal, the expression
 * will be copied verbatim and no matching will take place.
 *
 * Any headers in the given properties are used in the output
 * properties.
 */

public class Analyzer extends Processor
{

    public final static String BUNDLE_ACTIVATOR = "Bundle-Activator";
    public final static String BUNDLE_CLASSPATH = "Bundle-ClassPath";
    public final static String BUNDLE_CONTACTADDRESS = "Bundle-ContactAddress";
    public final static String BUNDLE_COPYRIGHT = "Bundle-Copyright";
    public final static String BUNDLE_DESCRIPTION = "Bundle-Description";
    public final static String BUNDLE_DOCURL = "Bundle-DocURL";
    public final static String BUNDLE_LICENSE = "Bundle-License";
    public final static String BUNDLE_LOCALIZATION = "Bundle-Localization";
    public final static String BUNDLE_MANIFESTVERSION = "Bundle-ManifestVersion";
    public final static String BUNDLE_NAME = "Bundle-Name";
    public final static String BUNDLE_NATIVECODE = "Bundle-NativeCode";
    public final static String BUNDLE_REQUIREDEXECUTIONENVIRONMENT = "Bundle-RequiredExecutionEnvironment";
    public final static String BUNDLE_SYMBOLICNAME = "Bundle-SymbolicName";
    public final static String BUNDLE_VENDOR = "Bundle-Vendor";
    public final static String BUNDLE_VERSION = "Bundle-Version";
    public final static String DYNAMICIMPORT_PACKAGE = "DynamicImport-Package";
    public final static String EXPORT_PACKAGE = "Export-Package";
    public final static String EXPORT_SERVICE = "Export-Service";
    public final static String FRAGMENT_HOST = "Fragment-Host";
    public final static String IMPORT_PACKAGE = "Import-Package";
    public final static String IMPORT_SERVICE = "Import-Service";
    public final static String PRIVATE_PACKAGE = "Private-Package";
    public final static String REQUIRE_BUNDLE = "Require-Bundle";
    public final static String SERVICE_COMPONENT = "Service-Component";

    public final static String[] headers = {
        BUNDLE_ACTIVATOR, BUNDLE_CONTACTADDRESS, BUNDLE_COPYRIGHT,
        BUNDLE_DOCURL, BUNDLE_LOCALIZATION, BUNDLE_NATIVECODE,
        BUNDLE_VENDOR, BUNDLE_VERSION, BUNDLE_LICENSE
    };

    static Pattern doNotCopy = Pattern.compile( "CVS|.svn" );

    private Properties properties   /* String->String */ = new Properties();
    private Map<String, Map<String, String>> contained =
        new HashMap<String, Map<String, String>>();            // package
    private Map<String, Map<String, String>> referred =
        new HashMap<String, Map<String, String>>();            // package
    private Map<String, Map<String, String>> uses = new HashMap<String, Map<String, String>>();            // package
    private Map<String, Clazz> classspace;
    private Map<String, Map<String, String>> exports;
    private Map<String, Map<String, String>> imports;
    private Map<String, Map<String, String>> bundleClasspath;                                                                // Bundle
    private Map<String, Map<String, String>> cpExports = new HashMap();
    private boolean analyzed;
    private File base = new File( "" );
    private Jar dot;                                                                            // The
    private String activator;

    private List<Jar> classpath = new ArrayList<Jar>();

    Macro replacer = new Macro( this );

    public void setJar( Jar jar )
    {
        this.dot = jar;
    }

    public void setJar( File jar )
        throws IOException
    {
        setJar( buildJar( jar ) );
    }

    public void setClasspath( File[] classpath )
        throws IOException
    {
        int size = 0;
        if( classpath != null )
        {
            size = classpath.length;
        }
        Jar[] cp = new Jar[size];
        for( int i = 0; i < cp.length; i++ )
        {
            cp[ i ] = buildJar( classpath[ i ] );
        }
        setClasspath( cp );
    }

    public void setProperties( File propertiesFile )
        throws IOException
    {
        this.base = propertiesFile.getAbsoluteFile().getParentFile();
        Properties local = getProperties( propertiesFile, new HashSet() );
        local.put( "project.file", propertiesFile.getAbsolutePath() );
        local.put( "project.name", propertiesFile.getName() );
        local.put( "project.dir", base.getAbsolutePath() );
        local.put( "project", stem( propertiesFile.getName() ) );

        if( !local.containsKey( IMPORT_PACKAGE ) )
        {
            local.put( IMPORT_PACKAGE, "*" );
        }
        setProperties( local );
    }

    public List<Jar> getClasspath()
    {
        return classpath;
    }

    public Jar getDot()
    {
        return dot;
    }

    public Map<String, Clazz> getClassSpace()
    {
        return classspace;
    }

    public File getBase()
    {
        return base;
    }

    private String stem( String name )
    {
        int n = name.lastIndexOf( '.' );
        if( n > 0 )
        {
            return name.substring( 0, n );
        }
        else
        {
            return name;
        }
    }

    Properties getProperties( File file, Set<File> done )
        throws IOException
    {
        Properties p = new Properties();
        InputStream in = new FileInputStream( file );
        p.load( in );
        in.close();
        String includes = p.getProperty( "-include" );
        if( includes != null )
        {
            String[] parts = includes.split( "\\s*,\\s*" );
            for( int i = 0; i < parts.length; i++ )
            {
                File next = new File( file.getParentFile(), parts[ i ] );
                if( next.exists() && !next.isDirectory() && !done.contains( next ) )
                {
                    done.add( next );
                    Properties level = getProperties( next, done );
                    level.putAll( p );
                    p = level;
                }
            }
        }
        return p;
    }

    public void setClasspath( Jar[] classpath )
    {
        this.classpath.addAll( Arrays.asList( classpath ) );
    }

    void analyzeClasspath()
        throws IOException
    {
        cpExports = new HashMap<String, Map<String, String>>();
        for( Iterator c = classpath.iterator(); c.hasNext(); )
        {
            Jar current = (Jar) c.next();
            checkManifest( current );
            Map<String, Map<String, Resource>> directories = current.getDirectories();
            for( String dir : directories.keySet() )
            {
                Resource resource = current.getResource( dir + "/packageinfo" );
                if( resource != null )
                {
                    String version = parsePackageInfo( resource.openInputStream() );
                    setPackageInfo( dir, "version", version );
                }
            }
        }
    }

    void setPackageInfo( String dir, String key, String value )
    {
        if( value != null )
        {
            String pack = dir.replace( '/', '.' );
            Map<String, String> map = cpExports.get( pack );
            if( map == null )
            {
                map = new HashMap<String, String>();
                cpExports.put( pack, map );
            }
            map.put( key, value );
        }
    }

    private void checkManifest( Jar jar )
    {
        try
        {
            Manifest m = jar.getManifest();
            if( m != null )
            {
                String exportHeader = m.getMainAttributes().getValue( EXPORT_PACKAGE );
                if( exportHeader != null )
                {
                    Map<String, Map<String, String>> exported = parseHeader( exportHeader );
                    if( exported != null )
                    // Hmm, moet mischien gemerged worden?
                    {
                        cpExports.putAll( exported );
                    }
                }
            }
        }
        catch( IOException e )
        {
            warning( "Erroneous Manifest for " + jar + " " + e );
        }
        catch( RuntimeException e )
        {
            warning( "Erroneous Manifest for " + jar + " " + e );
        }
    }

    public String getProperty( String headerName )
    {
        String value = properties.getProperty( headerName );
        if( value != null )
        {
            return replacer.process( value );
        }
        else
        {
            return null;
        }
    }

    String getProperty( String headerName, String deflt )
    {
        String v = getProperty( headerName );
        return v == null ? deflt : v;
    }

    public void setProperties( Properties properties )
    {
        this.properties = properties;
        replacer = new Macro( properties, this );

        String doNotCopy = getProperty( "-donotcopy" );
        if( doNotCopy != null )
        {
            Analyzer.doNotCopy = Pattern.compile( doNotCopy );
        }

        String cp = properties.getProperty( "-classpath" );
        if( cp != null )
        {
            String[] parts = cp.split( "\\s*,\\s*" );
            for( int i = 0; i < parts.length; i++ )
            {
                File file = new File( base, parts[ i ] );
                if( file.exists() )
                {
                    try
                    {
                        String filename = file.getName();
                        Jar jarFile = new Jar( filename, file );
                        classpath.add( jarFile );
                    }
                    catch( ZipException e )
                    {
                        error( "Classpath from properties had errors: " + parts[ i ] + " " + e );
                    }
                    catch( IOException e )
                    {
                        error( "Classpath from properties had errors: " + parts[ i ] + " " + e );
                    }
                }
                else
                {
                    error( "Trying to add non existent entry to the classpath from properties: "
                           + parts[ i ]
                    );
                }
            }
        }
    }

    Jar buildJar( File file )
        throws IOException
    {
        if( !file.exists() )
        {
            error( "File to build jar from does not exist: " + file );
            throw new FileNotFoundException( file.getAbsolutePath() );
        }
        Jar jar = new Jar( file.getName() );
        try
        {
            if( file.isDirectory() )
            {
                FileResource.build( jar, file, doNotCopy );
            }
            else if( file.exists() )
            {
                ZipResource.build( jar, file );
            }
            else
            {
                error( "No such file: " + file.getAbsolutePath() );
            }
        }
        catch( ZipException e )
        {
            error( "Failed to construct ZIP file: " + file.getAbsolutePath() );
        }
        return jar;

    }

    public void analyze()
        throws IOException
    {
        if( !analyzed )
        {
            analyzed = true;
            cpExports = new HashMap<String, Map<String, String>>();
            analyzeClasspath();

            activator = getProperty( BUNDLE_ACTIVATOR );
            bundleClasspath = parseHeader( getProperty( BUNDLE_CLASSPATH ) );

            classspace = analyzeBundleClasspath(
                dot,
                bundleClasspath,
                contained,
                referred,
                uses
            );

            referred.keySet().removeAll( contained.keySet() );

            Map<String, Map<String, String>> exportInstructions = parseHeader( getProperty( EXPORT_PACKAGE ) );
            Map<String, Map<String, String>> importInstructions = parseHeader( getProperty( IMPORT_PACKAGE ) );
            Map<String, Map<String, String>> dynamicImports = parseHeader( getProperty( DYNAMICIMPORT_PACKAGE ) );

            if( dynamicImports != null )
            {
                // Remove any dynamic imports from the referred set.
                referred.keySet().removeAll( dynamicImports.keySet() );
            }

            exports = merge( "export-package", exportInstructions, contained );
            imports = merge( "import-package", importInstructions, referred );

            // See what information we can find to augment the
            // imports. I.e. look on the classpath
            augmentImports();

            // Add all exports that do not have an -noimport: directive
            // to the imports.
            addExportsToImports();

            // Add the uses clause to the exports
            doUses( exports, uses );
        }
    }

    /**
     * For each import, find the exporter and see what you can learn from it.
     */
    static Pattern versionPattern = Pattern.compile( "(\\d+\\.\\d+)\\.\\d+" );

    private void augmentImports()
    {
        for( String packageName : imports.keySet() )
        {
            Map<String, String> currentAttributes = imports.get( packageName );

            Map<String, String> exporter = cpExports.get( packageName );
            if( exporter != null )
            {
                // See if we can borrow te version
                String version = (String) exporter.get( "version" );
                if( version == null )
                {
                    version = (String) exporter.get( "specification-version" );
                }
                if( version != null )
                {
                    // We remove the micro part of the version
                    // to a bit more lenient
                    Matcher m = versionPattern.matcher( version );
                    if( m.matches() )
                    {
                        version = m.group( 1 );
                    }
                    currentAttributes.put( "version", version );
                }

                // If we use an import with mandatory
                // attributes we better all use them
                String mandatory = (String) exporter.get( "mandatory:" );
                if( mandatory != null )
                {
                    String[] attrs = mandatory.split( "\\w*,\\w*" );
                    for( int i = 0; i < attrs.length; i++ )
                    {
                        currentAttributes.put( attrs[ i ], exporter.get( attrs[ i ] ) );
                    }
                }
            }
        }
    }

    /**
     * We will add all exports to the imports unless there is a -noimport
     * directive specified on an export. This directive is skipped for the
     * manifest.
     */
    private void addExportsToImports()
    {
        for( Map.Entry<String, Map<String, String>> entry : exports.entrySet() )
        {
            String packageName = entry.getKey();
            Map<String, String> parameters = entry.getValue();
            String noimport = parameters.get( "-noimport:" );
            if( noimport == null || !"true".equalsIgnoreCase( noimport ) )
            {
                Map<String, String> importParameters = imports.get( packageName );
                if( importParameters == null )
                {
                    imports.put( packageName, parameters );
                }
            }
        }
    }

    public Manifest calcManifest()
        throws IOException
    {
        analyze();
        Manifest manifest = new Manifest();
        Attributes main = manifest.getMainAttributes();

        main.putValue( BUNDLE_MANIFESTVERSION, "2" );

        String exportHeader = printClauses( exports, "uses:|include:|exclude:" );

        if( exportHeader.length() > 0 )
        {
            main.putValue( EXPORT_PACKAGE, exportHeader );
        }
        else
        {
            main.remove( EXPORT_PACKAGE );
        }

        Map<String, Map<String, String>> temp = removeKeys( imports, "java." );
        if( !temp.isEmpty() )
        {
            main.putValue( IMPORT_PACKAGE, printClauses( temp, "resolution:" ) );
        }
        else
        {
            main.remove( IMPORT_PACKAGE );
        }

        temp = new TreeMap<String, Map<String, String>>( contained );
        temp.keySet().removeAll( exports.keySet() );

        if( !temp.isEmpty() )
        {
            main.putValue( PRIVATE_PACKAGE, printClauses( temp, "" ) );
        }
        else
        {
            main.remove( PRIVATE_PACKAGE );
        }

        if( bundleClasspath != null && !bundleClasspath.isEmpty() )
        {
            main.putValue( BUNDLE_CLASSPATH, printClauses( bundleClasspath, "" ) );
        }
        else
        {
            main.remove( BUNDLE_CLASSPATH );
        }

        Map<String, Map<String, String>> l = doServiceComponent( getProperty( SERVICE_COMPONENT ) );
        if( !l.isEmpty() )
        {
            main.putValue( SERVICE_COMPONENT, printClauses( l, "" ) );
        }
        else
        {
            main.remove( SERVICE_COMPONENT );
        }

        for( Iterator h = properties.keySet().iterator(); h.hasNext(); )
        {
            String header = (String) h.next();
            if( !Character.isUpperCase( header.charAt( 0 ) ) )
            {
                continue;
            }

            if( header.equals( BUNDLE_CLASSPATH )
                || header.equals( EXPORT_PACKAGE )
                || header.equals( IMPORT_PACKAGE ) )
            {
                continue;
            }

            String value = getProperty( header );
            if( value != null && main.getValue( header ) == null )
            {
                main.putValue( header, value );
            }
        }
        main.put( Attributes.Name.MANIFEST_VERSION, "1" );

        // Copy old values into new manifest, when they
        // exist in the old one, but not in the new one
        merge( manifest );

        // Check for some defaults
        String p = getProperty( "project" );
        if( p != null )
        {
            if( main.getValue( BUNDLE_SYMBOLICNAME ) == null )
            {
                main.putValue( BUNDLE_SYMBOLICNAME, p );
            }
            if( main.getValue( BUNDLE_NAME ) == null )
            {
                main.putValue( BUNDLE_NAME, p );
            }
        }
        if( main.getValue( BUNDLE_VERSION ) == null )
        {
            main.putValue( BUNDLE_VERSION, "0" );
        }

        dot.setManifest( manifest );
        return manifest;
    }

    /**
     * @param manifest The manifest to merge
     *
     * @throws IOException if an IO problem has occured.
     */
    private void merge( Manifest manifest )
        throws IOException
    {
        Manifest old = dot.getManifest();
        if( old != null )
        {
            Attributes attributes = old.getMainAttributes();
            for( Map.Entry<Object, Object> entry : attributes.entrySet() )
            {
                Attributes.Name name = (Attributes.Name) entry.getKey();
                String value = (String) entry.getValue();
                if( !manifest.getMainAttributes().containsKey( name ) )
                {
                    manifest.getMainAttributes().put( name, value );
                }
            }
        }
    }

    /**
     * Add the uses clauses
     *
     * @param exports a Map of exported packages
     * @param uses    a Map of the uses clauses to be filled.
     */
    void doUses( Map<String, Map<String, String>> exports, Map<String, Map<String, String>> uses )
    {
        for( Object o : exports.keySet() )
        {
            String packageName = (String) o;
            Map<String, String> clause = exports.get( packageName );

            Set t = (Set) uses.get( packageName );
            if( t != null && !t.isEmpty() )
            {
                StringBuffer sb = new StringBuffer();
                String del = "";
                for( Object aT : t )
                {
                    String usedPackage = (String) aT;
                    if( !usedPackage.equals( packageName )
                        && !usedPackage.startsWith( "java." ) )
                    {
                        sb.append( del );
                        sb.append( usedPackage );
                        del = ",";
                    }
                }
                String s = sb.toString();
                if( s.length() > 0 )
                {
                    clause.put( "uses:", sb.toString() );
                }
            }
        }
    }

    /**
     * Print a standard Map based OSGi header.
     *
     * @param exports           map { name => Map { attribute|directive => value } }
     * @param allowedDirectives
     *
     * @return the clauses
     */
    String printClauses( Map<String, Map<String, String>> exports, String allowedDirectives )
    {
        StringBuffer sb = new StringBuffer();
        String del = "";
        for( Iterator i = exports.keySet().iterator(); i.hasNext(); )
        {
            String name = (String) i.next();
            Map<String, String> map = exports.get( name );
            sb.append( del );
            sb.append( name );

            for( Iterator j = map.keySet().iterator(); j.hasNext(); )
            {
                String key = (String) j.next();

                // Skip directives we do not recognize
                if( key.endsWith( ":" ) && !allowedDirectives.contains( key ) )
                {
                    continue;
                }

                String value = (String) map.get( key );
                sb.append( ";" );
                sb.append( key );
                sb.append( "=" );
                boolean dirty = value.indexOf( ',' ) >= 0
                                || value.indexOf( ';' ) >= 0;
                if( dirty )
                {
                    sb.append( "\"" );
                }
                sb.append( value );
                if( dirty )
                {
                    sb.append( "\"" );
                }
            }
            del = ", ";
        }
        return sb.toString();
    }

    /**
     * Merge the attributes of two maps, where the first map can contain
     * wildcarded names. The idea is that the first map contains patterns (for
     * example *) with a set of attributes. These patterns are matched against
     * the found packages in actual. If they match, the result is set with the
     * merged set of attributes. It is expected that the instructions are
     * ordered so that the instructor can define which pattern matches first.
     * Attributes in the instructions override any attributes from the actual.<br/>
     *
     * A pattern is a modified regexp so it looks like globbing. The * becomes a .*
     * just like the ? becomes a .?. '.' are replaced with \\. Additionally, if
     * the pattern starts with an exclamation mark, it will remove that matches
     * for that pattern (- the !) from the working set. So the following
     * patterns should work:
     * <ul>
     * <li>com.foo.bar</li>
     * <li>com.foo.*</li>
     * <li>com.foo.???</li>
     * <li>com.*.[^b][^a][^r]</li>
     * <li>!com.foo.* (throws away any match for com.foo.*)</li>
     * </ul>
     * Enough rope to hang the average developer I would say.
     *
     * @param instructions the instructions with patterns. A
     * @param actual       the actual found packages
     * @param type
     * @return
     */

    Map<String, Map<String, String>> merge( String type,
                                            Map<String, Map<String, String>> instructions,
                                            Map<String, Map<String, String>> actual )
    {
        actual = new HashMap<String, Map<String, String>>( actual ); // we do not want to ruin our original
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        Set superfluous = new TreeSet( instructions.keySet() );
        for( String instruction : instructions.keySet() )
        {
            Map<String, String> instructedAttributes = instructions.get( instruction );

            boolean negate = false;
            if( instruction.startsWith( "!" ) )
            {
                negate = true;
                instruction = instruction.substring( 1 );
            }
            else if( instruction.startsWith( "=" ) )
            {
                result.put( instruction.substring( 1 ), instructedAttributes );
                superfluous.remove( instruction );
                continue;
            }

            Pattern pattern = getPattern( instruction );

            for( Iterator p = actual.keySet().iterator(); p.hasNext(); )
            {
                String packageName = (String) p.next();

                if( pattern.matcher( packageName ).matches() )
                {
                    superfluous.remove( instruction );
                    if( !negate )
                    {
                        Map<String, String> newAttributes = new HashMap<String, String>();
                        newAttributes.putAll( actual.get( packageName ) );
                        newAttributes.putAll( instructedAttributes );
                        result.put( packageName, newAttributes );
                    }
                    p.remove(); // Can never match again for another pattern
                }
            }

        }
        if( !superfluous.isEmpty() )
        {
            warnings.add( "Superfluous " + type + " instructions: " + superfluous );
        }
        return result;
    }

    /**
     * @param string the regular expression to compile.
     *
     * @return a compiled regular expression pattern
     */
    public static Pattern getPattern( String string )
    {
        StringBuffer sb = new StringBuffer();
        for( int c = 0; c < string.length(); c++ )
        {
            switch( string.charAt( c ) )
            {
                case'.':
                    sb.append( "\\." );
                    break;
                case'*':
                    sb.append( ".*" );
                    break;
                case'?':
                    sb.append( ".?" );
                    break;
                default:
                    sb.append( string.charAt( c ) );
                    break;
            }
        }
        string = sb.toString();
        if( string.endsWith( "\\..*" ) )
        {
            sb.append( "|" );
            sb.append( string.replace( "\\..*", "" ) );
        }
        return Pattern.compile( sb.toString() );
    }

    /**
     * @return the bundle classpath
     */
    public Map<String, Map<String, String>> getBundleClasspath()
    {
        return bundleClasspath;
    }

    /**
     *
     * @return
     */
    public Map<String, Map<String, String>> getContained()
    {
        return contained;
    }

    /**
     * @return the exported packages
     */
    public Map<String, Map<String, String>> getExports()
    {
        return exports;
    }

    /**
     * @return the imported packages
     */
    public Map<String, Map<String, String>> getImports()
    {
        return imports;
    }

    /**
     *
     * @return
     */
    public Map<String, Map<String, String>> getReferred()
    {
        return referred;
    }

    /**
     *
     * @return
     */
    public Map<String, Map<String, String>> getUses()
    {
        return uses;
    }

    /**
     * Return the set of unreachable code depending on exports and the bundle
     * activator.
     *
     * @return the set of unreachable code depending on exports and the bundle activator.
     */
    public Set getUnreachable()
    {
        Set unreachable = new HashSet( uses.keySet() ); // all
        for( Object o : exports.keySet() )
        {
            String packageName = (String) o;
            removeTransitive( packageName, unreachable );
        }
        if( activator != null )
        {
            String pack = activator.substring( 0, activator.lastIndexOf( '.' ) );
            removeTransitive( pack, unreachable );
        }
        return unreachable;
    }

    void removeTransitive( String name, Set unreachable )
    {
        unreachable.remove( name );
        if( !unreachable.contains( name ) )
        {
            return;
        }

        Set ref = (Set) uses.get( name );
        if( ref != null )
        {
            for( Iterator r = ref.iterator(); r.hasNext(); )
            {
                String element = (String) r.next();
                removeTransitive( element, unreachable );
            }
        }
    }

    public Jar getJar()
    {
        return dot;
    }

    public Properties getProperties()
    {
        return properties;
    }

    /**
     * Check if a service component header is actually referring to a class. If
     * so, replace the reference with an XML file reference. This makes it
     * easier to create and use components.
     *
     * @param serviceComponent the service component to be checked.
     * @return
     */
    public Map<String, Map<String, String>> doServiceComponent( String serviceComponent )
    {
        Map<String, Map<String, String>> list = new LinkedHashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> sc = parseHeader( serviceComponent );
        if( !sc.isEmpty() )
        {
            for( Map.Entry<String, Map<String, String>> entry : sc.entrySet() )
            {
                String name = entry.getKey();
                Map<String, String> info = entry.getValue();
                if( name == null )
                {
                    error( "No name in Service-Component header: " + info );
                    continue;
                }
                if( dot.exists( name ) )
                {
                    // Normal service component
                    list.put( name, info );
                }
                else
                {
                    if( !checkClass( name ) )
                    {
                        error( "Not found Service-Component header: " + name );
                    }
                    else
                    {
                        // We have a definition, so make an XML resources
                        Resource resource = createComponentResource( name, info );
                        dot.putResource( "OSGI-INF/" + name + ".xml", resource );
                        list.put( "OSGI-INF/" + name + ".xml", new HashMap<String, String>() );
                    }
                }
            }
        }
        return list;
    }

    /**
     * @param name the name of the resource to create
     * @param info the info associated with the resource
     *
     * @return the created resource
     */
    private Resource createComponentResource( String name, Map<String, String> info )
    {

//        <?xml version="1.0" ?>
//        <descriptor xmlns="http://www.osgi.org/xmlns/app/v1.0.0">
//          <application class="com.acme.app.SampleMidlet">
//            <reference name="log"interface="org.osgi.service.log.LogService"/>
//          </application>
//        </descriptor>

        ByteArrayOutputStream out = null;
        PrintWriter pw = null;
        try
        {
            out = new ByteArrayOutputStream();
            pw = new PrintWriter( new OutputStreamWriter( out, "UTF-8" ) );
            pw.println( "<?xml version='1.0' encoding='utf-8'?>" );
            pw.println( "<component name='" + name + "'>" );
            pw.println( "  <implementation class='" + name + "'/>" );
            String provides = info.get( "provide:" );
            provides( pw, provides );
            properties( pw, info );
            reference( pw, info );
            pw.println( "</component>" );
        } catch( UnsupportedEncodingException e )
        {
            e.printStackTrace();  //can not happen.
        } finally
        {
            pw.close();
        }
        return new EmbeddedResource( out.toByteArray() );
    }

    /**
     * @param pw   the output destination
     * @param info the info map containing the properties.
     */
    private void properties( PrintWriter pw, Map<String, String> info )
    {
        Set properties = getMatchSet( (String) info.get( "properties:" ) );
        for( Object property : properties )
        {
            String clause = (String) property;
            int n = clause.indexOf( '=' );
            if( n <= 0 )
            {
                error( "Not a valid property in service component: " + clause );
            }
            else
            {
                String name = clause.substring( 0, n );
                String value = clause.substring( n + 1 );
                // TODO verify validity of name and value
                pw.print( "<property name='" );
                pw.print( name );
                pw.print( "'>" );
                pw.print( value.replaceAll( "\\n", "\n" ) );
                pw.println( "</property>" );
            }
        }
    }

    /**
     * @param pw   the output destination
     * @param info the info map containing 'dynamic:', 'optional:' and 'multiple:'
     */
    private void reference( PrintWriter pw, Map<String, String> info )
    {
        Set dynamic = getMatchSet( (String) info.get( "dynamic:" ) );
        Set optional = getMatchSet( (String) info.get( "optional:" ) );
        Set multiple = getMatchSet( (String) info.get( "multiple:" ) );

        for( Map.Entry<String, String> ref : info.entrySet() )
        {
            String referenceName = (String) ref.getKey();
            String interfaceName = (String) ref.getValue();
            // TODO check if the interface is contained or imported

            if( referenceName.endsWith( ":" ) )
            {
                continue;
            }

            if( !checkClass( interfaceName ) )
            {
                String s = "Component definition refers to a class that is neither imported nor contained: ";
                error( s + interfaceName );
            }

            pw.print( "  <reference name='" + referenceName + "' interface='" + interfaceName + "'" );

            String cardinality = optional.contains( referenceName ) ? "0" : "1";
            cardinality += "..";
            cardinality += multiple.contains( referenceName ) ? "n" : "1";
            if( !"1..1".equals( cardinality ) )
            {
                pw.print( " cardinality='" + cardinality + "'" );
            }

            if( Character.isLowerCase( referenceName.charAt( 0 ) ) )
            {
                String z = referenceName.substring( 0, 1 ).toUpperCase()
                           + referenceName.substring( 1 );
                pw.print( " bind='set" + z + "'" );
                // TODO Verify that the methods exist

                // TODO ProSyst requires both a bind and unbind :-(
                //if ( dynamic.contains(referenceName) )
                pw.print( " unbind='unset" + z + "'" );
                // TODO Verify that the methods exist
            }
            if( dynamic.contains( referenceName ) )
            {
                pw.print( " policy='dynamic'" );
            }
            pw.println( "/>" );
        }
    }

    /**
     * @param pw       the output destination
     * @param provides the interface that is provided
     */
    private void provides( PrintWriter pw, String provides )
    {
        if( provides != null )
        {
            pw.println( "  <service>" );
            StringTokenizer st = new StringTokenizer( provides, "," );
            while( st.hasMoreTokens() )
            {
                String interfaceName = st.nextToken();
                pw.println( "    <provide interface='" + interfaceName + "'/>" );
                if( !checkClass( interfaceName ) )
                {
                    String s = "Component definition provides a class that is neither imported nor contained: ";
                    error( s + interfaceName );
                }
            }
            pw.println( "  </service>" );
        }
    }

    private boolean checkClass( String interfaceName )
    {
        String path = interfaceName.replace( '.', '/' ) + ".class";
        if( classspace.containsKey( path ) )
        {
            return true;
        }

        String pack = interfaceName;
        int n = pack.lastIndexOf( '.' );
        if( n > 0 )
        {
            pack = pack.substring( 0, n );
        }
        else
        {
            pack = ".";
        }

        return imports.containsKey( pack );
    }

    private Set getMatchSet( String list )
    {
        if( list == null )
        {
            return new HashSet();
        }

        String[] parts = list.split( "\\s*,\\s*" );
        return new HashSet( Arrays.asList( parts ) );
    }
}
