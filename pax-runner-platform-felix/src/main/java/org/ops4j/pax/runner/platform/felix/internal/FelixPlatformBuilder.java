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
package org.ops4j.pax.runner.platform.felix.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.file.FileUtils;
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
 * Platform builder for felix platform.
 *
 * @author Alin Dreghiciu
 * @since September 01, 2007
 */
public class FelixPlatformBuilder
    implements PlatformBuilder
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( FelixPlatformBuilder.class );
    /**
     * Name of the main class from Felix.
     */
    private static final String MAIN_CLASS_NAME = "org.apache.felix.main.Main";
    /**
     * The directory name where the configuration will be stored.
     */
    private static final String CONFIG_DIRECTORY = "felix";
    /**
     * Configuration file name.
     */
    private static final String CONFIG_INI = "config.ini";
    /**
     * Caching directory.
     */
    private static final String CACHE_DIRECTORY = "cache";
    /**
     * Profile name to be used when console should be started.
     */
    private static final String CONSOLE_PROFILE = "tui";
    /**
     * Separator for properties (bundles)
     */
    private static final String SEPARATOR = " ";
    /**
     * Current bundle context.
     */
    private final BundleContext m_bundleContext;

    /**
     * Create a new equinux platform builder.
     *
     * @param bundleContext a bundle context
     */
    public FelixPlatformBuilder( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
    }

    /**
     * Creates a config.ini file under the working directory/felix directory.
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
            final File workingDirectory = context.getWorkingDirectory();
            // make sure the configuration directory exists
            final File configDirectory = new File( workingDirectory, CONFIG_DIRECTORY );
            configDirectory.mkdirs();
            // make sure that the cache file exists
            //new File( configDirectory, CACHE_DIRECTORY ).mkdirs();
            // create the configuration file
            final File configFile = new File( configDirectory, CONFIG_INI );
            configFile.createNewFile();
            LOGGER.debug( "Create felix configuration ini file [" + configFile + "]" );
            final Configuration configuration = context.getConfiguration();

            os = new FileOutputStream( configFile );
            final PropertiesWriter writer = new PropertiesWriter( os, SEPARATOR );

            writeHeader( writer );

            writer.append( "#############################" );
            writer.append( " Felix settings" );
            writer.append( "#############################" );
            // framework start level
            final Integer startLevel = configuration.getStartLevel();
            if( startLevel != null )
            {
                writer.append( "felix.startlevel.framework", startLevel.toString() );
            }
            // bundle start level
            final Integer bundleStartLevel = configuration.getBundleStartLevel();
            if( bundleStartLevel != null )
            {
                writer.append( "felix.startlevel.bundle", bundleStartLevel.toString() );
            }
            // default profile
            final String profile = configuration.getFrameworkProfile();
            if( profile != null )
            {
                writer.append( "felix.cache.profile", profile );
                final Boolean usePersistedState = configuration.usePersistedState();
                if( usePersistedState != null && !usePersistedState )
                {
                    final File profileDirectory =
                        new File( configDirectory, CACHE_DIRECTORY + File.separator + profile );
                    LOGGER.trace( "Cleaning profile folder [" + profileDirectory + "]" );
                    FileUtils.delete( profileDirectory );
                }
            }
            // system packages
            writer.append( Constants.FRAMEWORK_SYSTEMPACKAGES, context.getSystemPackages() );

            if( bundles != null && bundles.size() > 0 )
            {
                writer.append();
                writer.append( "#############################" );
                writer.append( " Client bundles to install" );
                writer.append( "#############################" );
                appendBundles( writer, bundles, bundleStartLevel );
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
            throw new PlatformException( "Could not create felix configuration file", e );
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
                    throw new PlatformException( "Could not create felix configuration file", e );
                }
            }
        }

    }

    /**
     * Writes properties to configuration file.
     *
     * @param writer     a property writer
     * @param properties properties to be written; can be null
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
     * @param writer            a property writer
     * @param bundles           bundles to write
     * @param defaultStartlevel default start level for bundles. used if no start level is set on bundles.
     *
     * @throws java.net.MalformedURLException re-thrown from getting the file url
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *                                        if one of the bundles does not have a file
     */
    private void appendBundles( final PropertiesWriter writer, final List<LocalBundle> bundles,
                                final Integer defaultStartlevel )
        throws MalformedURLException, PlatformException
    {
        for( LocalBundle bundle : bundles )
        {
            File bundleFile = bundle.getFile();
            if( bundleFile == null )
            {
                throw new PlatformException( "The file from bundle to install cannot be null" );
            }
            final StringBuilder propertyName = new StringBuilder()
                .append( "felix.auto" );

            final BundleReference reference = bundle.getBundleReference();
            final Boolean shouldStart = reference.shouldStart();
            if( shouldStart != null && shouldStart )
            {
                propertyName.append( "." ).append( "start" );
            }
            else
            {
                propertyName.append( "." ).append( "install" );
            }
            Integer startLevel = reference.getStartLevel();
            if( startLevel == null )
            {
                startLevel = defaultStartlevel;
            }
            if( startLevel != null )
            {
                propertyName.append( "." ).append( startLevel );
            }
            // PAXRUNNER-41
            // url of the file must be quoted otherwise will be considered as two separated files by Felix
            writer.append( propertyName.toString(), "\"" + bundleFile.toURL().toExternalForm() + "\"" );
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
        // there are no arguments to pass to Main for felix
        return null;
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #getVMOptions(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String[] getVMOptions( final PlatformContext context )
    {
        Assert.notNull( "Platform context", context );
        final File workingDirectory = context.getWorkingDirectory();
        return new String[]{
            "-Dfelix.config.properties=" + workingDirectory.toURI()
            + "/" + CONFIG_DIRECTORY
            + "/" + CONFIG_INI,
            "-Dfelix.cache.dir=" + workingDirectory.getAbsolutePath()
            + File.separator + CONFIG_DIRECTORY
            + File.separator + CACHE_DIRECTORY
        };
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder#getDefinition()
     */
    public InputStream getDefinition()
        throws IOException
    {
        // TODO implement platform versioning
        final URL url = m_bundleContext.getBundle().getResource( "META-INF/platform-felix/definition-1.0.0.xml" );
        if( url == null )
        {
            throw new FileNotFoundException( "META-INF/platform-felix/definition-1.0.0.xml could not be found" );
        }
        return url.openStream();
    }

    /**
     * If the console option is set then it will return the tui profile otherwise will return null.
     *
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #getRequiredProfile(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String getRequiredProfile( final PlatformContext context )
    {
        final Boolean console = context.getConfiguration().startConsole();
        if( console == null || !console )
        {
            return null;
        }
        else
        {
            return CONSOLE_PROFILE;
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return "Felix";
    }


}
