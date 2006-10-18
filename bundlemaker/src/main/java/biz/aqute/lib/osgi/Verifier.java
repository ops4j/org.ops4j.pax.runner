package biz.aqute.lib.osgi;

import biz.aqute.qtokens.QuotedTokenizer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public class Verifier extends Processor
{

    private Jar dot;
    private Manifest manifest;
    private Map<String, Map<String, String>> referred = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> contained = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> uses = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> mimports;
    private Map<String, Map<String, String>> mexports;

    private List<Jar> bundleClassPath;
    private Map<String, Clazz> classSpace;
    private boolean r3;
    private boolean usesRequire;
    private boolean fragment;
    private Attributes main;

    private final static Pattern EENAME = Pattern.compile( "CDC-1\\.0/Foundation-1\\.0"
                                                           + "|OSGi/Minimum-1\\.1"
                                                           + "|JRE-1\\.1"
                                                           + "|J2SE-1\\.2"
                                                           + "|J2SE-1\\.3"
                                                           + "|J2SE-1\\.4"
                                                           + "|J2SE-1\\.5"
                                                           + "|PersonalJava-1\\.1"
                                                           + "|PersonalJava-1\\.2"
                                                           + "|CDC-1\\.0/PersonalBasis-1\\.0"
                                                           + "|CDC-1\\.0/PersonalJava-1\\.0"
    );

    private final static Pattern BUNDLEMANIFESTVERSION = Pattern.compile( "2" );
    private final static Pattern SYMBOLICNAME = Pattern.compile( "[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*" );

    private final static String version = "[0-9]+(\\.[0-9]+(\\.[0-9]+(\\.[0-9A-Za-z_-]+)?)?)?";
    private final static Pattern VERSION = Pattern.compile( version );
    private final static Pattern FILTEROP = Pattern.compile( "=|<=|>=|~=" );
    private final static Pattern VERSIONRANGE =
        Pattern.compile( "((\\(|\\[)" + version + "," + version + "(\\]|\\)))|" + version );
    private final static Pattern FILE = Pattern.compile( "/?[^/\"\n\r\u0000]+(/[^/\"\n\r\u0000]+)*" );
    private final static Pattern WILDCARDPACKAGE =
        Pattern.compile( "((\\p{Alnum}|_)+(\\.(\\p{Alnum}|_)+)*(\\.\\*)?)|\\*" );
    private final static Pattern ISO639 = Pattern.compile( "[A-Z][A-Z]" );

    public Verifier( Jar jar )
        throws Exception
    {
        this.dot = jar;
        this.manifest = jar.getManifest();
        main = this.manifest.getMainAttributes();

        r3 = getHeader( "Bundle-ManifestVersion" ) == null;
        usesRequire = getHeader( "Require-Bundle" ) != null;
        fragment = getHeader( "Fragment-Host" ) != null;

        bundleClassPath = getBundleClassPath();
        mimports = parseHeader( manifest.getMainAttributes().getValue( "Import-Package" ) );
        mexports = parseHeader( manifest.getMainAttributes().getValue( "Export-Package" ) );
    }

    private List<Jar> getBundleClassPath()
    {
        List<Jar> list = new ArrayList<Jar>();
        String bcp = getHeader( "Bundle-Classpath" );
        if( bcp == null )
        {
            list.add( dot );
        }
        else
        {
            Map<String, Map<String, String>> entries = parseHeader( bcp );
            for( String jarOrDir : entries.keySet() )
            {
                if( ".".equals( jarOrDir ) )
                {
                    list.add( dot );
                }
                else
                {
                    if( "/".equals( jarOrDir ) )
                    {
                        jarOrDir = "";
                    }
                    if( jarOrDir.endsWith( "/" ) )
                    {
                        error( "Bundle-Classpath directory must not end with a slash: " + jarOrDir );
                        jarOrDir = jarOrDir.substring( 0, jarOrDir.length() - 1 );
                    }

                    Resource resource = dot.getResource( jarOrDir );
                    if( resource != null )
                    {
                        try
                        {
                            Jar sub = new Jar( jarOrDir );
                            EmbeddedResource.build( sub, resource.openInputStream() );
                            if( !jarOrDir.endsWith( ".jar" ) )
                            {
                                String message = "Valid JAR file on Bundle-Classpath does not have .jar extension: ";
                                warning( message + jarOrDir );
                            }
                            list.add( sub );
                        }
                        catch( IOException e )
                        {
                            error( "Invalid embedded JAR file on Bundle-Classpath: " + jarOrDir + ", " + e );
                        }
                    }
                    else if( dot.getDirectories().containsKey( jarOrDir ) )
                    {
                        if( r3 )
                        {
                            error( "R3 bundles do not support directories on the Bundle-ClassPath: " + jarOrDir );
                        }

                        String s = "The aQute code was not type-safe at this point. No workaround found for now.";
                        InternalError error = new InternalError( s );
                        throw error;
//                        list.add( jarOrDir );
                    }
                    else
                    {
                        error( "Cannot find a file or directory for Bundle-Classpath entry: " + jarOrDir );
                    }
                }
            }
        }
        return list;
    }

    /*
      * Bundle-NativeCode ::= nativecode ( ',' nativecode )* ( ’,’ optional) ?
      * nativecode ::= path ( ';' path )* // See 1.4.2 ( ';' parameter )+
      * optional ::= ’*’
      */
    private void verifyNative()
    {
        String nc = getHeader( "Bundle-NativeCode" );
        if( nc != null )
        {
            QuotedTokenizer qt = new QuotedTokenizer( nc, ",;=", true );
            char del;
            do
            {
                do
                {
                    String name = qt.nextToken();
                    del = qt.getSeparator();
                    if( del == ';' )
                    {
                        if( !dot.exists( name ) )
                        {
                            error( "Native library not found in JAR: " + name );
                        }
                    }
                    else
                    {
                        String value = qt.nextToken();
                        String key = name.toLowerCase();
                        if( "osname".equals( key ) )
                        {
                            // ...
                        }
                        else if( "osversion".equals( key ) )
                        {
                            // verify version range
                            verify( value, VERSIONRANGE );
                        }
                        else if( "lanuage".equals( key ) )
                        {
                            verify( value, ISO639 );
                        }
                        else if( "selection-filter".equals( key ) )
                        {
                            // verify syntax filter
                            verifyFilter( value, 0 );
                        }
                        else
                        {
                            warning( "Unknown attribute in native code: " + name + "=" + value );
                        }
                        del = qt.getSeparator();
                    }
                } while( del == ';' );
            } while( del == ',' );
        }
    }

    private void verifyActivator()
    {
        String bactivator = getHeader( "Bundle-Activator" );
        if( bactivator != null )
        {
            Clazz cl = loadClass( bactivator );
            if( cl == null )
            {
                error( "Bundle-Activator not found on the bundle class path or imports: "
                       + bactivator
                );
            }
        }
    }

    private Clazz loadClass( String className )
    {
        String path = className.replace( '.', '/' ) + ".class";
        return classSpace.get( path );
    }

    private void verifyComponent()
    {
        String serviceComponent = getHeader( "Service-Component" );
        if( serviceComponent != null )
        {
            Map<String, Map<String, String>> map = parseHeader( serviceComponent );
            for( String component : map.keySet() )
            {
                if( !dot.exists( component ) )
                {
                    error( "Service-Component entry can not be located in JAR: " + component );
                }
                else
                {
                    // validate component ...
                }
            }
        }
    }

    public void info()
    {
        System.out.println( "Refers                           : " + referred );
        System.out.println( "Contains                         : " + contained );
        System.out.println( "Manifest Imports                 : " + mimports );
        System.out.println( "Manifest Exports                 : " + mexports );
    }

    /**
     * Invalid exports are exports mentioned in the manifest but not found on
     * the classpath. This can be calculated with: exports - contains.
     */
    private void verifyInvalidExports()
    {
        Set<String> invalidExport = new HashSet<String>( mexports.keySet() );
        invalidExport.removeAll( contained.keySet() );
        if( !invalidExport.isEmpty() )
        {
            error( "Exporting packages that are not on the Bundle-Classpath"
                   + bundleClassPath + ": " + invalidExport
            );
        }
    }

    /**
     * Invalid imports are imports that we never refer to. They can be
     * calculated by removing the refered packages from the imported packages.
     * This leaves packages that the manifest imported but that we never use.
     */
    private void verifyInvalidImports()
    {
        Set<String> invalidImport = new TreeSet<String>( mimports.keySet() );
        invalidImport.removeAll( referred.keySet() );
        if( !invalidImport.isEmpty() )
        {
            error( "Importing packages that are never refered to by any class on the Bundle-Classpath"
                   + bundleClassPath + ": " + invalidImport
            );
        }
    }

    /**
     * Check for unresolved imports. These are referals that are not imported by
     * the manifest and that are not part of our bundle classpath. The are
     * calculated by removing all the imported packages and contained from the
     * refered packages.
     */
    private void verifyUnresolvedReferences()
    {
        Set<String> unresolvedReferences = new TreeSet<String>( referred.keySet() );
        unresolvedReferences.removeAll( mimports.keySet() );
        unresolvedReferences.removeAll( contained.keySet() );

        // Remove any java.** packages.
        Iterator<String> p = unresolvedReferences.iterator();
        while( p.hasNext() )
        {
            String pack = p.next();
            if( pack.startsWith( "java." ) )
            {
                p.remove();
            }
        }

        if( !unresolvedReferences.isEmpty() )
        {
            // Now we want to know the
            // classes that are the culprits
            Set<String> culprits = new HashSet<String>();
            for( Clazz clazz : classSpace.values() )
            {
                if( hasOverlap( unresolvedReferences, clazz.imports.keySet() ) )
                {
                    culprits.add( clazz.getPath() );
                }
            }

            error( "Unresolved references to " + unresolvedReferences
                   + " by class(es) on the Bundle-Classpath" + bundleClassPath + ": " + culprits
            );
        }
    }

    private boolean hasOverlap( Set<String> a, Set<String> b )
    {
        for( String aStrings : a )
        {
            if( b.contains( aStrings ) )
            {
                return true;
            }
        }
        return false;
    }

    public void verify()
        throws IOException
    {
        classSpace = analyzeBundleClasspath( dot, parseHeader( getHeader( Analyzer.BUNDLE_CLASSPATH ) ), contained,
                                             referred, uses
        );
        verifyManifestFirst();
        verifyActivator();
        verifyComponent();
        verifyNative();
        verifyInvalidExports();
        verifyInvalidImports();
        verifyUnresolvedReferences();
        verifySymbolicName();
        verifyListHeader( "Bundle-RequiredExecutionEnvironment", EENAME, false );
        verifyHeader( "Bundle-ManifestVersion", BUNDLEMANIFESTVERSION, false );
        verifyHeader( "Bundle-Version", VERSION, true );
        verifyListHeader( "Bundle-Classpath", FILE, false );
        verifyDynamicImportPackage();
        if( usesRequire )
        {
            if( !errors.isEmpty() )
            {
                String mess =
                    "Bundle uses Require Bundle, this can generate false errors because then not enough information is available without the required bundles";
                warnings.add( 0, mess );
            }
        }
    }

    /**
     * <pre>
     *   DynamicImport-Package ::= dynamic-description
     *       ( ',' dynamic-description )*
     *
     *   dynamic-description::= wildcard-names ( ';' parameter )*
     *   wildcard-names ::= wildcard-name ( ';' wildcard-name )*
     *   wildcard-name ::= package-name
     *                  | ( package-name '.*' ) // See 1.4.2
     *                  | '*'
     * </pre>
     */
    private void verifyDynamicImportPackage()
    {
        verifyListHeader( "DynamicImport-Package", WILDCARDPACKAGE, true );
        String dynamicImportPackage = getHeader( "DynamicImport-Package" );
        if( dynamicImportPackage == null )
        {
            return;
        }

        Map<String, Map<String, String>> map = parseHeader( dynamicImportPackage );
        for( String name : map.keySet() )
        {
            name = name.trim();
            if( !verify( name, WILDCARDPACKAGE ) )
            {
                error( "DynamicImport-Package header contains an invalid package name: " + name );
            }

            Map<String, String> sub = map.get( name );
            if( r3 && sub.size() != 0 )
            {
                error( "DynamicPackage-Import has attributes on import: " + name
                       + ". This is however, an <=R3 bundle and attributes on this header were introduced in R4. "
                );
            }
        }
    }

    private void verifyManifestFirst()
    {
        if( !dot.doManifestFirst() )
        {
            String s =
                "Invalid JAR stream: Manifest should come first to be compatible with JarInputStream, it was not";
            errors.add( s );
        }
    }

    private void verifySymbolicName()
    {
        Map<String, Map<String, String>> bsn = parseHeader( getHeader( "Bundle-SymbolicName" ) );
        if( !bsn.isEmpty() )
        {
            if( bsn.size() > 1 )
            {
                errors.add( "More than one BSN specified " + bsn );
            }

            String name = bsn.keySet().iterator().next();
            if( !SYMBOLICNAME.matcher( name ).matches() )
            {
                errors.add( "Symbolic Name has invalid format: " + name );
            }
        }
    }

    /**
     * <pre>
     *  filter ::= ’(’ filter-comp ’)’
     *  filter-comp ::= and | or | not | operation
     *  and ::= ’&amp;’ filter-list
     *  or ::= ’|’ filter-list
     *  not ::= ’!’ filter
     *  filter-list ::= filter | filter filter-list
     *  operation ::= simple | present | substring
     *  simple ::= attr filter-type value
     *  filter-type ::= equal | approx | greater | less
     *  equal ::= ’=’
     *  approx ::= ’&tilde;=’
     *  greater ::= ’&gt;=’
     *  less ::= ’&lt;=’
     *  present ::= attr ’=*’
     *  substring ::= attr ’=’ initial any final
     *  inital ::= () | value
     *  any ::= ’*’ star-value
     *  star-value ::= () | value ’*’ star-value
     *  final ::= () | value
     *  value ::= &lt;see text&gt;
     * </pre>
     *
     * @param expr
     * @param index
     */

    private int verifyFilter( String expr, int index )
    {
        try
        {
            while( Character.isWhitespace( expr.charAt( index ) ) )
            {
                index++;
            }

            if( expr.charAt( index ) != '(' )
            {
                throw new IllegalArgumentException(
                    "Filter mismatch: expected ( at position " + index
                    + " : " + expr
                );
            }

            while( Character.isWhitespace( expr.charAt( index ) ) )
            {
                index++;
            }

            switch( expr.charAt( index ) )
            {
                case'!':
                case'&':
                case'|':
                    return verifyFilterSubExpression( expr, index ) + 1;

                default:
                    return verifyFilterOperation( expr, index ) + 1;
            }
        }
        catch( IndexOutOfBoundsException e )
        {
            throw new IllegalArgumentException(
                "Filter mismatch: early EOF from " + index
            );
        }
    }

    private int verifyFilterOperation( String expr, int index )
    {
        StringBuffer sb = new StringBuffer();
        while( "=><~()".indexOf( expr.charAt( index ) ) < 0 )
        {
            sb.append( expr.charAt( index++ ) );
        }
        String attr = sb.toString().trim();
        if( attr.length() == 0 )
        {
            throw new IllegalArgumentException(
                "Filter mismatch: attr at index " + index + " is 0"
            );
        }
        sb = new StringBuffer();
        while( "=><~".indexOf( expr.charAt( index ) ) >= 0 )
        {
            sb.append( expr.charAt( index++ ) );
        }
        String operator = sb.toString();
        if( !verify( operator, FILTEROP ) )
        {
            throw new IllegalArgumentException(
                "Filter error, illegal operator " + operator + " at index "
                + index
            );
        }

        sb = new StringBuffer();
        while( ")".indexOf( expr.charAt( index ) ) < 0 )
        {
            switch( expr.charAt( index ) )
            {
                case'\\':
                    if( expr.charAt( index + 1 ) == '*'
                        || expr.charAt( index + 1 ) == ')' )
                    {
                        index++;
                    }
                    else
                    {
                        throw new IllegalArgumentException(
                            "Filter error, illegal use of backslash at index "
                            + index
                            + ". Backslash may only be used before * or ("
                        );
                    }
            }
            sb.append( expr.charAt( index++ ) );
        }
        return index;
    }

    private int verifyFilterSubExpression( String expr, int index )
    {
        do
        {
            index = verifyFilter( expr, index + 1 );
            while( Character.isWhitespace( expr.charAt( index ) ) )
            {
                index++;
            }
            if( expr.charAt( index ) != ')' )
            {
                throw new IllegalArgumentException(
                    "Filter mismatch: expected ) at position " + index
                    + " : " + expr
                );
            }
            index++;
        } while( expr.charAt( index ) == '(' );
        return index;
    }

    private String getHeader( String string )
    {
        return main.getValue( string );
    }

    private boolean verifyHeader( String name, Pattern regex, boolean error )
    {
        String value = manifest.getMainAttributes().getValue( name );
        if( value == null )
        {
            return false;
        }

        QuotedTokenizer st = new QuotedTokenizer( value.trim(), "," );
        for( String token : st.getTokenList() )
        {
            if( !verify( token, regex ) )
            {
                ( error ? errors : warnings ).add( "Invalid value for " + name
                                                   + ", " + value + " does not match " + regex.pattern()
                );
            }
        }
        return true;
    }

    private boolean verify( String value, Pattern regex )
    {
        return regex.matcher( value ).matches();
    }

    private boolean verifyListHeader( String name, Pattern regex, boolean error )
    {
        String value = manifest.getMainAttributes().getValue( name );
        if( value == null )
        {
            return false;
        }

        QuotedTokenizer st = new QuotedTokenizer( value.trim(), "," );
        for( String token : st.getTokenList() )
        {
            if( !regex.matcher( token ).matches() )
            {
                String message = "Invalid value for " + name + ", " + value + " does not match " + regex.pattern();
                if( error )
                {
                    errors.add( message );
                }
                else
                {
                    warnings.add( message );
                }
            }
        }
        return true;
    }


}
