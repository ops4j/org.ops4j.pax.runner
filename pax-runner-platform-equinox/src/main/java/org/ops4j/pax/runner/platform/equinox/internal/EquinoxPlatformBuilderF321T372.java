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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.*;
import org.ops4j.util.collections.PropertiesWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Platform builder for equinox platform.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public class EquinoxPlatformBuilderF321T372
    implements PlatformBuilder
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( EquinoxPlatformBuilderF321T372.class );
    /**
     * Provider name to be used in registration.
     */
    private static final String PROVIDER_NAME = "equinox";
    /**
     * Configuration directory argument name.
     */
    private static final String ARG_CONFIGURATION = "-configuration";
    /**
     * Console argument name.
     */
    private static final String ARG_CONSOLE = "-console";
    /**
     * Debug options file argument name.
     */
    private static final String ARG_DEBUG = "-debug";
    /**
     * System property specifying the eclipse install area.
     */
    private static final String PROP_INSTALL_AREA = "osgi.install.area";
    /**
     * System property specifying the parent directory of equinox jar.
     */
    private static final String PROP_SYSPATH = "osgi.syspath";
    /**
     * Name of the main class from Equinox.
     */
    private static final String MAIN_CLASS_NAME = "org.eclipse.core.runtime.adaptor.EclipseStarter";
    /**
     * The directory name where the configuration will be stored.
     */
    private static final String CONFIG_DIRECTORY = "equinox";
    /**
     * Debug options file name.
     */
    private static final String OPTIONS = ".options";
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
     * Supported version.
     */
    private final String m_version;
    /**
     * System property specifying the eclipse product.
     */
    private static final String ECLIPSE_PRODUCT = "eclipse.product";
    /**
     * System property specifying the eclipse application.
     */
    private static final String ECLIPSE_APPLICATION = "eclipse.application";
    /**
     * System property specifying whether equinox should ignore the specified product or application.
     */
    private static final String ECLIPSE_IGNORE_APP = "eclipse.ignoreApp";

    /**
     * Create a new equinux platform builder.
     *
     * @param bundleContext a bundle context
     * @param version       supported version
     */
    public EquinoxPlatformBuilderF321T372(final BundleContext bundleContext, final String version)
    {
        NullArgumentException.validateNotNull( bundleContext, "Bundle context" );
        NullArgumentException.validateNotNull( version, "Version" );
        m_bundleContext = bundleContext;
        m_version = version;
    }

    /**
     * Creates a config.ini file under the working directory/equinox directory.
     *
     * @see PlatformBuilder
     *      #prepare(org.ops4j.pax.runner.platform.PlatformContext)
     */
    public void prepare( final PlatformContext context )
        throws PlatformException
    {
        NullArgumentException.validateNotNull( context, "Platform context" );
        createConfigIniFile( context );
        createOptionsFile( context );
    }

    private void createConfigIniFile( final PlatformContext context )
        throws PlatformException
    {
        final List<BundleReference> bundles = context.getBundles();
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
            final String[] vmOptions = context.getConfiguration().getVMOptions();


            if ( !contains(vmOptions, ECLIPSE_PRODUCT) &&
                    !contains(vmOptions, ECLIPSE_APPLICATION) &&
                    !contains(vmOptions, ECLIPSE_IGNORE_APP) )
            {
            //     there is no aclipse application
                writer.append(ECLIPSE_IGNORE_APP, "true" );
            }
            // Set "osgi.syspath" property = System property specifying the parent directory of equinox jar
            // It is not supposed to be set by user (pax runner) but if this is not set relative urls used for bundles
            // to be installed are calculated relative to "runner/bundles" instead of user dir = "runner"
            {
                writer.append(
                    PROP_SYSPATH,
                    context.getFilePathStrategy().normalizeAsPath( context.getWorkingDirectory() )
                );
            }
            // avoid automatic exit of framework in case that there is no console and not non daemon thrad is active
            {
                final Boolean startConsole = context.getConfiguration().startConsole();
                if( startConsole == null || !startConsole )
                {
                    writer.append( "osgi.framework.activeThreadType", "normal" );
                }
            }
            // use persisted state
            {
                final Boolean usePersistedState = configuration.usePersistedState();
                if( usePersistedState != null && !usePersistedState )
                {
                    writer.append( "osgi.clean", "true" );
                }
            }
            // framework start level
            {
                final Integer startLevel = configuration.getStartLevel();
                if( startLevel != null )
                {
                    writer.append( "osgi.startLevel", startLevel.toString() );
                }
            }
            // bundle start level
            {
                final Integer bundleStartLevel = configuration.getBundleStartLevel();
                if( bundleStartLevel != null )
                {
                    writer.append( "osgi.bundles.defaultStartLevel", bundleStartLevel.toString() );
                }
            }
            // execution environments
            {
                writer.append( Constants.FRAMEWORK_EXECUTIONENVIRONMENT, context.getExecutionEnvironment() );
            }
            // boot delegation
            {
                final StringBuilder bootDelegation = new StringBuilder();
                String bootDelegationOption = context.getConfiguration().getBootDelegation();
                if( bootDelegationOption != null )
                {
                    bootDelegation.append( bootDelegationOption ).append( "," );
                }
                bootDelegation.append( BOOT_DELEGATION_PACKAGES );
                writer.append( Constants.FRAMEWORK_BOOTDELEGATION, bootDelegation.toString() );
            }
            // system packages
            {
                writer.append( Constants.FRAMEWORK_SYSTEMPACKAGES, context.getSystemPackages() );
            }

            if( bundles != null && bundles.size() > 0 )
            {
                writer.append();
                writer.append( "#############################" );
                writer.append( " Client bundles to install" );
                writer.append( "#############################" );
                appendBundles( writer, bundles, context );
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

    private void createOptionsFile( final PlatformContext context )
        throws PlatformException
    {
        OutputStream os = null;
        try
        {
            // make sure the directory exists
            final File configDirectory = new File( context.getWorkingDirectory(), CONFIG_DIRECTORY );
            configDirectory.mkdirs();
            // create the configuration file
            final File configFile = new File( configDirectory, OPTIONS );
            configFile.createNewFile();
            final Configuration configuration = context.getConfiguration();
            if( isOptionsFileNeeded( configuration ) )
            {
                LOGGER.debug( "Create equinox options file [" + configFile + "]" );

                os = new FileOutputStream( configFile );
                final PropertiesWriter writer = new PropertiesWriter( os );

                writeHeader( writer );
                Properties props = new Properties();
                writer.append( "#############################" );
                writer.append( " Equinox debug options" );
                writer.append( "#############################" );
                props.setProperty( "org.eclipse.osgi/trace/filename", "runtime.traces" );
                props.setProperty( "org.eclipse.osgi/debug/bundleTime", "false" );
                props.setProperty( "org.eclipse.osgi/defaultprofile/logsynchronously", "false" );
                props.setProperty( "org.eclipse.osgi/resolver/requires", "false" );
                props.setProperty( "org.eclipse.osgi/debug", "false" );
                props.setProperty( "org.eclipse.osgi/profile/benchmark", "false" );
                props.setProperty( "org.eclipse.osgi/defaultprofile/buffersize", "256" );
                props.setProperty( "org.eclipse.osgi/trace/activation", "false" );
                props.setProperty( "org.eclipse.osgi/debug/security", "false" );
                props.setProperty( "org.eclipse.osgi/resolver/wiring", "false" );
                props.setProperty( "org.eclipse.osgi/eclipseadaptor/debug/platformadmin/resolver", "false" );
                props.setProperty( "org.eclipse.osgi/debug/loader", "true" );
                props.setProperty( "org.eclipse.osgi/eclipseadaptor/debug", "false" );
                props.setProperty( "org.eclipse.osgi/debug/messageBundles", "false" );
                props.setProperty( "org.eclipse.osgi/debug/events", "false" );
                props.setProperty( "org.eclipse.osgi/resolver/debug", "false" );
                props.setProperty( "org.eclipse.osgi/profile/startup", "false" );
                props.setProperty( "org.eclipse.osgi/debug/startlevel", "false" );
                props.setProperty( "org.eclipse.osgi/debug/packageadmin", "false" );
                props.setProperty( "org.eclipse.osgi/resolver/grouping", "false" );
                props.setProperty( "org.eclipse.osgi/trace/classLoading", "false" );
                props.setProperty( "org.eclipse.osgi/eclipseadaptor/debug/platformadmin", "false" );
                props.setProperty( "org.eclipse.osgi/debug/filter", "false" );
                props.setProperty( "org.eclipse.osgi/monitor/activation", "false" );
                props.setProperty( "org.eclipse.osgi/resolver/generics", "false" );
                props.setProperty( "org.eclipse.osgi/debug/manifest", "false" );
                props.setProperty( "org.eclipse.osgi/debug/services", "false" );
                props.setProperty( "org.eclipse.osgi/eclipseadaptor/debug/location", "false" );
                props.setProperty( "org.eclipse.osgi/profile/impl",
                                   "org.eclipse.osgi.internal.profile.DefaultProfileLogger"
                );
                props.setProperty( "org.eclipse.osgi/eclipseadaptor/converter/debug", "false" );
                props.setProperty( "org.eclipse.osgi/profile/debug", "false" );
                props.setProperty( "org.eclipse.osgi/monitor/classes", "false" );
                props.setProperty( "org.eclipse.osgi/trace/filters", "trace.properties" );
                props.setProperty( "org.eclipse.osgi/resolver/cycles", "false" );
                props.setProperty( "org.eclipse.osgi/defaultprofile/logfilename", "" );
                props.setProperty( "org.eclipse.osgi/resolver/imports", "false" );
                props.setProperty( "org.eclipse.osgi/monitor/resources", "false" );
                appendProperties( writer, props );
                writer.write();
            }
        }
        catch( IOException e )
        {
            throw new PlatformException( "Could not create equinox debug options file", e );
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
                    throw new PlatformException( "Could not create equinox debug options file", e );
                }
            }
        }
    }

    private Boolean isOptionsFileNeeded( final Configuration configuration )
    {
        return configuration.isDebugClassLoading();
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
     * @param context platform context
     *
     * @throws java.net.MalformedURLException re-thrown from getting the file url
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *                                        if one of the bundles does not have a file
     */
    private void appendBundles( final PropertiesWriter writer,
                                final List<BundleReference> bundles,
                                final PlatformContext context )
        throws MalformedURLException, PlatformException
    {
        for( BundleReference reference : bundles )
        {
            URL url = reference.getURL();
            if( url == null )
            {
                throw new PlatformException( "The file from bundle to install cannot be null" );
            }
            final StringBuilder builder = new StringBuilder();
            if( "file".equals( url.getProtocol() ) )
            {
                builder.append( "reference:" );
            }
            builder.append( context.getFilePathStrategy().normalizeAsUrl( url ) );

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
        NullArgumentException.validateNotNull( context, "Platform context" );

        final File workingDirectory = context.getWorkingDirectory();
        final List<String> arguments = new ArrayList<String>();
        final Boolean startConsole = context.getConfiguration().startConsole();
        if( startConsole != null && startConsole )
        {
            arguments.add( ARG_CONSOLE );
        }
        arguments.add( ARG_CONFIGURATION );
        arguments.add(
            context.getFilePathStrategy().normalizeAsPath( new File( workingDirectory, CONFIG_DIRECTORY ) )
        );
        if( isOptionsFileNeeded( context.getConfiguration() ) )
        {
            arguments.add( ARG_DEBUG );
            arguments.add(
                context.getFilePathStrategy().normalizeAsPath(
                    new File( new File( workingDirectory, CONFIG_DIRECTORY ), OPTIONS )
                )
            );
        }
        return arguments.toArray( new String[arguments.size()] );
    }

    /**
     * return snull as there are no additional virtual machien arguments.
     *
     * @see PlatformBuilder#getVMOptions(PlatformContext)
     */
    public String[] getVMOptions( final PlatformContext context )
    {
        NullArgumentException.validateNotNull( context, "Platform context" );

        final Collection<String> vmOptions = new ArrayList<String>();
        vmOptions.add(
            "-D" + PROP_INSTALL_AREA + "="
            + context.getFilePathStrategy().normalizeAsPath(
                new File( context.getWorkingDirectory(), CONFIG_DIRECTORY )
            )
        );
        return vmOptions.toArray( new String[vmOptions.size()] );
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getDefinition( final Configuration configuration )
        throws IOException
    {
        final String definitionFile = "META-INF/platform-equinox/definition-" + m_version + ".xml";
        final URL url = m_bundleContext.getBundle().getResource( definitionFile );
        if( url == null )
        {
            throw new FileNotFoundException( definitionFile + " could not be found" );
        }
        return url.openStream();
    }

    /**
     * Return null as there is no profile required by equinox.
     *
     * @see PlatformBuilder
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
        return "Equinox " + m_version;
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


    /**
     * Checks if the needle is contained in any of the strings in haystack.
     * @param haystack the strings to be searched in
     * @param needle the string to be found.
     * @return true if the string could be found.
     */
    private boolean contains(String[] haystack, String needle) {
        if( haystack != null )
        {
            for (String s : haystack)
            {
                if ( s.contains(needle) )
                {
                    return true;
                }
            }
        }
        return false;
    }
}
