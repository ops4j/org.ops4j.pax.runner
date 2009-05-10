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
package org.ops4j.pax.runner.platform.concierge.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.util.collections.PropertiesWriter;

/**
 * Platform builder for concierge platform.
 *
 * @author Alin Dreghiciu
 * @since November 22, 2007
 */
public class ConciergePlatformBuilder
    implements PlatformBuilder
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ConciergePlatformBuilder.class );
    /**
     * Provider name to be used in registration.
     */
    private static final String PROVIDER_NAME = "concierge";
    /**
     * Name of the main class from Knopflerfish.
     */
    private static final String MAIN_CLASS_NAME = "ch.ethz.iks.concierge.framework.Framework";
    /**
     * The directory name where the configuration will be stored.
     */
    private static final String CONFIG_DIRECTORY = "concierge";
    /**
     * Configuration file name.
     */
    private static final String CONFIG_INI = "config.ini";
    /**
     * Profile name to be used when console should be started.
     */
    private static final String CONSOLE_PROFILE = "tui";
    /**
     * Current bundle context.
     */
    private final BundleContext m_bundleContext;
    /**
     * Supported version.
     */
    private final String m_version;

    /**
     * Create a new equinux platform builder.
     *
     * @param bundleContext a bundle context
     * @param version       supported version
     */
    public ConciergePlatformBuilder( final BundleContext bundleContext, final String version )
    {
        NullArgumentException.validateNotNull( bundleContext, "Bundle context" );
        NullArgumentException.validateNotNull( version, "Version" );
        m_bundleContext = bundleContext;
        m_version = version;
    }

    /**
     * Creates a config.ini file under the working directory/knopflerfish directory.
     *
     * @see PlatformBuilder#prepare(PlatformContext)
     */
    public void prepare( final PlatformContext context )
        throws PlatformException
    {
        NullArgumentException.validateNotNull( context, "Platform context" );
        final List<BundleReference> bundles = context.getBundles();
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
            LOGGER.debug( "Create concierge configuration ini file [" + configFile + "]" );
            final Configuration configuration = context.getConfiguration();

            os = new FileOutputStream( configFile );
            final PropertiesWriter writer = new PropertiesWriter( os );

            writeHeader( writer );

            writer.append( "#############################" );
            writer.append( " Concierge settings" );
            writer.append( "#############################" );
            writer
                .append( "-D" + Constants.FRAMEWORK_SYSTEMPACKAGES, context.getSystemPackages() )
                .append(
                    "-Dch.ethz.iks.concierge.basedir",
                    context.getFilePathStrategy().normalizeAsPath( configDirectory )
                );
            // clean start ?
            final Boolean usePersistedState = configuration.usePersistedState();
            if( usePersistedState != null && !usePersistedState )
            {
                writer.appendRaw( "-init" );
            }
            // framework start level
            final Integer startLevel = configuration.getStartLevel();
            if( startLevel != null )
            {
                writer.appendRaw( "-startlevel " + startLevel.toString() );
            }
            // bundle start level
            final Integer bundleStartLevel = configuration.getBundleStartLevel();
            if( bundles != null && bundles.size() > 0 )
            {
                writer.append();
                writer.append( "#############################" );
                writer.append( " Client bundles to install" );
                writer.append( "#############################" );
                appendBundles( writer, bundles, context, bundleStartLevel );
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
            throw new PlatformException( "Could not create concierge configuration file", e );
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
                    throw new PlatformException( "Could not create concierge configuration file", e );
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
                writer.append( "-D" + key, properties.getProperty( key ) );
            }
        }
    }

    /**
     * Writes bundles to configuration file.
     *
     * @param writer            a property writer
     * @param bundles           bundles to write
     * @param context           platform context
     * @param defaultStartlevel default start level for bundles. used if no start level is set on bundles.
     *
     * @throws java.net.MalformedURLException re-thrown from getting the file url
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *                                        if one of the bundles does not have a file
     */
    private void appendBundles( final PropertiesWriter writer,
                                final List<BundleReference> bundles,
                                final PlatformContext context,
                                final Integer defaultStartlevel )
        throws MalformedURLException, PlatformException
    {
        Map<Integer, List<String>> bundlesPerStartlevel = new Hashtable<Integer, List<String>>();
        for( BundleReference reference : bundles )
        {
            URL bundleFile = reference.getURL();
            if( bundleFile == null )
            {
                throw new PlatformException( "The file from bundle to install cannot be null" );
            }

            String propertyName = "-install ";
            final Boolean shouldStart = reference.shouldStart();
            if( shouldStart != null && shouldStart )
            {
                propertyName = "-istart ";
            }
            Integer startLevel = reference.getStartLevel();
            if( startLevel == null )
            {
                startLevel = defaultStartlevel;
            }
            List<String> bundlesAsStrings = bundlesPerStartlevel.get( startLevel );
            if( bundlesAsStrings == null )
            {
                bundlesAsStrings = new ArrayList<String>();
                bundlesPerStartlevel.put( startLevel, bundlesAsStrings );
            }
            bundlesAsStrings.add( propertyName + context.getFilePathStrategy().normalizeAsUrl( bundleFile ) );
        }
        for( Map.Entry<Integer, List<String>> entry : bundlesPerStartlevel.entrySet() )
        {
            writer.appendRaw( "-initlevel " + entry.getKey().toString() );
            for( String bundle : entry.getValue() )
            {
                writer.appendRaw( bundle );
            }
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
     * @see PlatformBuilder#getMainClassName()
     */
    public String getMainClassName()
    {
        return MAIN_CLASS_NAME;
    }

    /**
     * @see PlatformBuilder
     *      #getArguments(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String[] getArguments( final PlatformContext context )
    {
        return null;
    }

    /**
     * @see PlatformBuilder
     *      #getVMOptions(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public String[] getVMOptions( final PlatformContext context )
    {
        NullArgumentException.validateNotNull( context, "Platform context" );

        final Collection<String> vmOptions = new ArrayList<String>();
        final File workingDirectory = context.getWorkingDirectory();
        // TODO Check if the following is solved in newer version of concierge
        // the property osgi.maxLevel is a workarround on the fact that concierge will not start/install the bundles if
        // there is a gap between the start level of the bundles. So, we force him to iterate to all start level
        // Actually the value should be the highest bundle start level + 1 but we take the risk for the moment
        vmOptions.add(
            "-Dosgi.maxLevel=100"
        );
        vmOptions.add(
            "-Dxargs="
            + context.getFilePathStrategy().normalizeAsPath(
                new File( new File( workingDirectory, CONFIG_DIRECTORY ), CONFIG_INI )
            )
        );
        final String bootDelegation = context.getConfiguration().getBootDelegation();
        if( bootDelegation != null )
        {
            vmOptions.add(
                "-D" + Constants.FRAMEWORK_BOOTDELEGATION + "=" + bootDelegation
            );
        }
        return vmOptions.toArray( new String[vmOptions.size()] );
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getDefinition( final Configuration configuration )
        throws IOException
    {
        final String definitionFile = "META-INF/platform-concierge/definition-" + m_version + ".xml";
        final URL url = m_bundleContext.getBundle().getResource( definitionFile );
        if( url == null )
        {
            throw new FileNotFoundException( definitionFile + " could not be found" );
        }
        return url.openStream();
    }

    /**
     * If the console option is set then it will return the tui profile otherwise will return null.
     *
     * @see PlatformBuilder
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
        return "Concierge " + m_version;
    }

    /**
     * @see PlatformBuilder#getProviderName()
     */
    public String getProviderName()
    {
        return PROVIDER_NAME;
    }

    /**
     * @see PlatformBuilder#getProviderVersion()
     */
    public String getProviderVersion()
    {
        return m_version;
    }

}
