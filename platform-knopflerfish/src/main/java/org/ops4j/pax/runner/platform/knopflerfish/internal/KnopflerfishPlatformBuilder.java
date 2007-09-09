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
package org.ops4j.pax.runner.platform.knopflerfish.internal;

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
 * Platform builder for knopflerfish platform.
 *
 * @author Alin Dreghiciu
 * @since September 09, 2007
 */
public class KnopflerfishPlatformBuilder
    implements PlatformBuilder
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( KnopflerfishPlatformBuilder.class );
    /**
     * Name of the main class from Knopflerfish.
     */
    private static final String MAIN_CLASS_NAME = "org.knopflerfish.framework.Main";
    /**
     * The directory name where the configuration will be stored.
     */
    private static final String CONFIG_DIRECTORY = "knopflerfish";
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
    public KnopflerfishPlatformBuilder( final BundleContext bundleContext )
    {
        Assert.notNull( "Bundle context", bundleContext );
        m_bundleContext = bundleContext;
    }

    /**
     * Creates a config.ini file under the working directory/knopflerfish directory.
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
            LOGGER.debug( "Create knopflerfish configuration ini file [" + configFile + "]" );
            final Configuration configuration = context.getConfiguration();

            os = new FileOutputStream( configFile );
            final PropertiesWriter writer = new PropertiesWriter( os, SEPARATOR );

            writeHeader( writer );

            writer.append( "#############################" );
            writer.append( " Knopflerfish settings" );
            writer.append( "#############################" );
            // varios settings
            writer
                .append( "-Dorg.osgi.provisioning.spid", "knopflerfish" )
                .append( "-Dorg.knopflerfish.verbosity", "0" )
                .append( "-Doscar.repository.url", "http://www.knopflerfish.org/repo/repository.xml" )
                .append( "-Dorg.knopflerfish.framework.debug.packages", "false" )
                .append( "-Dorg.knopflerfish.framework.debug.errors", "true" )
                .append( "-Dorg.knopflerfish.framework.debug.classloader", "false" )
                .append( "-Dorg.knopflerfish.framework.debug.startlevel", "false" )
                .append( "-Dorg.knopflerfish.framework.debug.ldap", "false" )
                .append( "-Dorg.knopflerfish.startlevel.use", "true" )
                .append( "-D" + Constants.FRAMEWORK_SYSTEMPACKAGES, context.getSystemPackages() )
                .append();
           
            // framework start level
            final Integer startLevel = configuration.getStartLevel();
            if ( startLevel != null )
            {
                writer.appendRaw( "-startlevel " + startLevel.toString() );
            }
            // bundle start level
            final Integer bundleStartLevel = configuration.getBundleStartLevel();
            if ( bundleStartLevel != null )
            {
                writer.appendRaw( "-initlevel " + bundleStartLevel.toString() );
            }

            if ( bundles != null && bundles.size() > 0 )
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
        catch ( IOException e )
        {
            throw new PlatformException( "Could not create knopflerfish configuration file", e );
        }
        finally
        {
            if ( os != null )
            {
                try
                {
                    os.close();
                }
                catch ( IOException e )
                {
                    throw new PlatformException( "Could not create knopflerfish configuration file", e );
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
        if ( properties != null )
        {
            final Enumeration enumeration = properties.propertyNames();
            while ( enumeration.hasMoreElements() )
            {
                final String key = (String) enumeration.nextElement();
                writer.append( "-D" + key, properties.getProperty( key ) );
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
        for ( LocalBundle bundle : bundles )
        {
            File bundleFile = bundle.getFile();
            if ( bundleFile == null )
            {
                throw new PlatformException( "The file from bundle to install cannot be null" );
            }

            String propertyName = null;

            final BundleReference reference = bundle.getBundleReference();
            final Boolean shouldStart = reference.shouldStart();
            if ( shouldStart != null && shouldStart )
            {
                propertyName = "-istart " ;
            }
            else
            {
                propertyName = "-install ";
            }
            // TODO knopflerfish does not support start level per bundle. Workaround ?
            writer.appendRaw( propertyName + bundleFile.toURL().toExternalForm() );
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
        try
        {
            return new String[]{
                "-xargs",
                new File( context.getWorkingDirectory(), CONFIG_DIRECTORY + File.separator + CONFIG_INI
                ).getAbsoluteFile()
                    .toURL().toExternalForm(),
            };
        }
        catch ( MalformedURLException e )
        {
            // TODO shall a platform exception be thrown instead of RuntimeException?
            throw new RuntimeException( e );
        }
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder
     *      #getVMOptions(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String[] getVMOptions( final PlatformContext context )
    {
        Assert.notNull( "Platform context", context );
        return new String[]{
            "-Dorg.knopflerfish.framework.usingwrapperscript=false",
            "-Dorg.knopflerfish.framework.exitonshutdown=true",
        };
    }

    /**
     * @see org.ops4j.pax.runner.platform.PlatformBuilder#getDefinition()
     */
    public InputStream getDefinition()
        throws IOException
    {
        // TODO implement platform versioning
        final URL url =
            m_bundleContext.getBundle().getResource( "META-INF/platform-knopflerfish/definition-2.0.0.xml" );
        if ( url == null )
        {
            throw new FileNotFoundException( "META-INF/platform-knopflerfish/definition-2.0.0.xml could not be found" );
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
        if ( console == null || !console )
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
        return "Knopflerfish";
    }


}
