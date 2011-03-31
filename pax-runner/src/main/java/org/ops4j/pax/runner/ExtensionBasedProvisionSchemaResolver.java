package org.ops4j.pax.runner;

import java.io.File;
import java.net.MalformedURLException;

/**
 * Extension based provision schema resolver:<br/>
 * * if starts with scan- returns the same value<br/>
 * * if extension is pom -> scan-pom<br/>
 * * if extension is jar or bundle -> scan-jar<br/>
 * * if extension is zip -> scan-zip<br/>
 * * if any other extension -> scan-file<br/>
 * * if no extension or ends with slash or backslash -> scan-dir<br/>
 * <br/>
 * It also adds a file protocol if is a local file.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class ExtensionBasedProvisionSchemaResolver
    implements ProvisionSchemaResolver
{

    /**
     * {@inheritDoc}
     */
    public String resolve( final String toResolve )
    {
        if( toResolve == null || toResolve.trim().length() == 0 )
        {
            return null;
        }
        if( toResolve.matches( "scan-.*:.*" ) )
        {
            return toResolve;
        }
        String options = "";
        String resolve = toResolve;
        if( toResolve.contains( "@" ) )
        {
            final int startOfOption = toResolve.indexOf( "@" );
            options = toResolve.substring( startOfOption );
            resolve = toResolve.substring( 0, startOfOption );
        }
        // first resolve schema
        String schema = org.ops4j.pax.scanner.dir.ServiceConstants.SCHEMA;
        if( !resolve.endsWith( "/" ) && !resolve.endsWith( "\\" ) && !resolve.contains( "!/" ) )
        {
            // check if is a pom using mvn protocol
            if( resolve.startsWith( org.ops4j.pax.url.mvn.ServiceConstants.PROTOCOL )
                && resolve.endsWith( "pom" ) )
            {
                schema = org.ops4j.pax.scanner.pom.ServiceConstants.SCHEMA;
            }
            // check if starts with mvn / wrap / war / obr, because most common it will be a bundle
            else if( resolve.startsWith( org.ops4j.pax.url.mvn.ServiceConstants.PROTOCOL )
                     || resolve.startsWith( org.ops4j.pax.url.wrap.ServiceConstants.PROTOCOL )
                     || resolve.startsWith( org.ops4j.pax.url.war.ServiceConstants.PROTOCOL_WAR )
                     || resolve.startsWith( org.ops4j.pax.url.war.ServiceConstants.PROTOCOL_WAR_INSTRUCTIONS )
                     || resolve.startsWith( org.ops4j.pax.url.war.ServiceConstants.PROTOCOL_WAR_REFERENCE )
                     || resolve.startsWith( org.ops4j.pax.url.war.ServiceConstants.PROTOCOL_WEB_BUNDLE )
                     || resolve.startsWith( org.ops4j.pax.url.obr.ServiceConstants.PROTOCOL )
                     || resolve.startsWith( org.ops4j.pax.url.assembly.ServiceConstants.PROTOCOL )
                     || resolve.startsWith( org.ops4j.pax.url.assembly.ServiceConstants.PROTOCOL_REFERENCE )
                     || resolve.startsWith( org.ops4j.pax.url.dir.ServiceConstants.PROTOCOL ) )
            {
                schema = org.ops4j.pax.scanner.bundle.ServiceConstants.SCHEMA;
            }
            else
            {
                int indexOfSlash = resolve.lastIndexOf( "/" );
                if( indexOfSlash == -1 )
                {
                    indexOfSlash = resolve.lastIndexOf( "\\" );
                }
                final int indexOfDot = resolve.lastIndexOf( "." );
                if( indexOfDot > indexOfSlash )
                {
                    schema = org.ops4j.pax.scanner.file.ServiceConstants.SCHEMA;
                    if( indexOfDot < resolve.length() - 1 )
                    {
                        final String extension = resolve.substring( indexOfDot + 1 ).toUpperCase();
                        if( "XML".equals( extension ) )
                        {
                            schema = org.ops4j.pax.scanner.pom.ServiceConstants.SCHEMA;
                        }
                        else if( "ZIP".equals( extension ) )
                        {
                            schema = org.ops4j.pax.scanner.dir.ServiceConstants.SCHEMA;
                        }
                        else if( "JAR".equals( extension ) || "BUNDLE".equals( extension ) )
                        {
                            schema = org.ops4j.pax.scanner.bundle.ServiceConstants.SCHEMA;
                        }
                        else if( "OBR".equals( extension ) )
                        {
                            schema = org.ops4j.pax.scanner.obr.ServiceConstants.SCHEMA;
                        }
                        else if( "COMPOSITE".equals( extension ) || "PROFILE".equals( extension ) )
                        {
                            schema = org.ops4j.pax.scanner.composite.ServiceConstants.SCHEMA;
                        }
                    }
                }
            }
        }
        // then check out if is a local file
        final File file = new File( resolve );
        String resolved = resolve;
        if( file.exists() )
        {
            try
            {
                resolved = file.toURL().toExternalForm();
            }
            catch( MalformedURLException ignore )
            {
                // ignore as this should not happen if the file exists
            }
        }
        return schema + org.ops4j.pax.scanner.ServiceConstants.SEPARATOR_SCHEME + resolved + options;
    }

}
