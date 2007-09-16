/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.platform.equinox.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.properties.PropertiesWriter;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * Platform builder for equinox platform.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public class EquinoxPlatformBuilder
    implements PlatformBuilder
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( EquinoxPlatformBuilder.class );
    /**
     * Configuration directory argument name.
     */
    private static final String ARG_CONFIGURATION = "-configuration";
    /**
     * Installation directory argument name.
     */
    private static final String ARG_INSTALL = "-install";
    /**
     * Console argument name.
     */
    private static final String ARG_CONSOLE = "-console";
    /**
     * Name of the main class from Equinox.
     */
    private static final String MAIN_CLASS_NAME = "org.eclipse.core.runtime.adaptor.EclipseStarter";
    /**
     * The directory name where the configuration will be stored.
     */
    private static final String CONFIG_DIRECTORY = "equinox";
    /**
     * Configuration file name.
     */
    private static final String CONFIG_INI = "config.ini";
    /**
     * Boot delegation comma separated list of packages.
     */
    private static final String BOOT_DELEGATION_PACKAGES = "java.*";
    /**
     * Current bundle context.
     */
    private final BundleContext m_bundleContext;

    /**
     * Create a new equinux platform builder.
     *
     * @param bundleContext a bundle context
     */
    public EquinoxPlatformBuilder( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
    }

    /**
     * Creates a config.ini file under the working directory/equinox directory.
     *
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #prepare(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public void prepare( final PlatformContext context )
        throws PlatformException
    {
        Assert.notNull( "Platform context", context );
        final List<LocalBundle> bundles = context.getBundles();
        OutputStream os = null;
        try
        {
            // make sure the directory exists
            final File configDirectory = new File( context.getWorkingDirectory(), CONFIG_DIRECTORY );
            configDirectory.mkdirs();
            // create the configuration file
            final File configFile = new File( configDirectory, CONFIG_INI );
            configFile.createNewFile();
            LOGGER.debug( "Create equinox configuration ini file [" + configFile + "]" );
            final Configuration configuration = context.getConfiguration();

            os = new FileOutputStream( configFile );
            final PropertiesWriter writer = new PropertiesWriter( os );

            writeHeader( writer );

            writer.append( "#############################" );
            writer.append( " Equinox settings" );
            writer.append( "#############################" );
            writer.append( "eclipse.ignoreApp", "true" );
            final Boolean clean = configuration.shouldClean();
            if( clean != null && clean )
            {
                writer.append( "osgi.clean", "true" );
            }
            final Integer startLevel = configuration.getStartLevel();
            if( startLevel != null )
            {
                writer.append( "osgi.startLevel", startLevel.toString() );
            }
            final Integer bundleStartLevel = configuration.getBundleStartLevel();
            if( bundleStartLevel != null )
            {
                writer.append( "osgi.bundles.defaultStartLevel", bundleStartLevel.toString() );
            }

            if( bundles != null && bundles.size() > 0 )
            {
                writer.append();
                writer.append( "#############################" );
                writer.append( " Client bundles to install" );
                writer.append( "#############################" );
                appendBundles( writer, bundles );
            }

            writer.append();
            writer.append( "#############################" );
            writer.append( " System properties" );
            writer.append( "#############################" );
            appendProperties( writer, context.getProperties() );

            writer.write();
        }
        catch( IOException e )
        {
            throw new PlatformException( "Could not create equinox configuration file", e );
        }
        finally
        {
            if( os != null )
            {
                try
                {
                    os.close();
                }
                catch( IOException e )
                {
                    //noinspection ThrowFromFinallyBlock
                    throw new PlatformException( "Could not create equinox configuration file", e );
                }
            }
        }

    }

    /**
     * Writes properties to configuration file.
     *
     * @param writer     a property writer
     * @param properties properties toC be written; can be null
     */
    private void appendProperties( final PropertiesWriter writer, final Properties properties )
    {
        if( properties != null )
        {
            final Enumeration enumeration = properties.propertyNames();
            while( enumeration.hasMoreElements() )
            {
                final String key = (String) enumeration.nextElement();
                writer.append( key, properties.getProperty( key ) );
            }
        }
    }

    /**
     * Writes bundles to configuration file.
     *
     * @param writer  a property writer
     * @param bundles bundles to write
     *
     * @throws java.net.MalformedURLException re-thrown from getting the file url
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *                                        if one of the bundles does not have a file
     */
    private void appendBundles( final PropertiesWriter writer, final List<LocalBundle> bundles )
        throws MalformedURLException, PlatformException
    {
        for( LocalBundle bundle : bundles )
        {
            File bundleFile = bundle.getFile();
            if( bundleFile == null )
            {
                throw new PlatformException( "The file from bundle to install cannot be null" );
            }
            final StringBuilder builder = new StringBuilder()
                .append( "reference:" )
                .append( bundleFile.toURL().toExternalForm() );

            final BundleReference reference = bundle.getBundleReference();
            final Integer startLevel = reference.getStartLevel();
            if( startLevel != null )
            {
                builder.append( "@" ).append( startLevel );
            }
            final Boolean shouldStart = reference.shouldStart();
            if( shouldStart != null && shouldStart )
            {
                if( startLevel != null )
                {
                    builder.append( ":" );
                }
                else
                {
                    builder.append( "@" );
                }
                builder.append( "start" );
            }
            writer.append( "osgi.bundles", builder.toString() );
        }
    }

    /**
     * Writes OPS4j header.
     *
     * @param writer a property writer
     */
    private void writeHeader( final PropertiesWriter writer )
    {
        writer.append( "###############################################" );
        writer.append( "              ______  ________  __  __        #" );
        writer.append( "             / __  / /  __   / / / / /        #" );
        writer.append( "            /  ___/ /  __   / _\\ \\ _/         #" );
        writer.append( "           /  /    /  / /  / / _\\ \\           #" );
        writer.append( "          /__/    /__/ /__/ /_/ /_/           #" );
        writer.append( "                                              #" );
        writer.append( " Pax Runner from OPS4J - http://www.ops4j.org #" );
        writer.append( "###############################################" );
        writer.append();
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder#getMainClassName()
     */
    public String getMainClassName()
    {
        return MAIN_CLASS_NAME;
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #getArguments(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String[] getArguments( final PlatformContext context )
    {
        Assert.notNull( "Platform context", context );
        final String workingDirectory = context.getWorkingDirectory().getAbsolutePath();
        final List<String> arguments = new ArrayList<String>();
        final Boolean startConsole = context.getConfiguration().startConsole();
        if( startConsole != null && startConsole )
        {
            arguments.add( ARG_CONSOLE );
        }
        arguments.add( ARG_CONFIGURATION );
        arguments.add( workingDirectory + File.separator + CONFIG_DIRECTORY );
        arguments.add( ARG_INSTALL );
        arguments.add( workingDirectory );
        return arguments.toArray( new String[0] );
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #getVMOptions(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String[] getVMOptions( final PlatformContext context )
    {
        Assert.notNull( "Platform context", context );
        return new String[]{
            "-D" + Constants.FRAMEWORK_BOOTDELEGATION + "=" + BOOT_DELEGATION_PACKAGES,
            "-D" + Constants.FRAMEWORK_SYSTEMPACKAGES + "=" + context.getSystemPackages()
        };
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder#getDefinition()
     */
    public InputStream getDefinition()
        throws IOException
    {
        // TODO implement platform versioning
        final URL url = m_bundleContext.getBundle().getResource( "META-INF/platform-equinox/definition-3.2.1.xml" );
        if( url == null )
        {
            throw new FileNotFoundException( "META-INF/platform-equinox/definition-3.2.1.xml could not be found" );
        }
        return url.openStream();
    }

    /**
     * Return null as there is no profile required by equinox.
     *
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #getRequiredProfile(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String getRequiredProfile( final PlatformContext context )
    {
        return null;
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return "Equinox";
    }


}
