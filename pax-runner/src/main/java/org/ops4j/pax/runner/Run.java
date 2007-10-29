/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2007 Alin Dreghiciu.
 * Copyright 2007 David Leangen.
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
package org.ops4j.pax.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.LogLevel;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.util.EventDispatcher;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import static org.ops4j.pax.runner.CommandLine.*;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.osgi.RunnerBundle;
import org.ops4j.pax.runner.osgi.RunnerBundleContext;
import org.ops4j.pax.runner.osgi.RunnerStartLevel;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.BundleReferenceBean;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.UnsupportedSchemaException;

/**
 * Main runner class. Does all the work.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class Run
{

    /**
     * Logger.
     */
    private static Log LOGGER;
    /**
     * Handler service onfiguration property name.
     */
    private static final String HANDLER_SERVICE = "handler.service";
    /**
     * Provision service onfiguration property name.
     */
    private static final String PROVISION_SERVICE = "provision.service";
    /**
     * Default provision url (in no argument) onfiguration property name.
     */
    private static final String PROVISION_DEFAULT_URL = "default.provision.url";
    /**
     * Platform extender configuration property name.
     */
    private static final String PLATFORM_SERVICE = "platform.service";

    /**
     * Creates a new runner.
     */
    public Run()
    {
        if( LOGGER == null )
        {
            LOGGER = LogFactory.getLog( Run.class, LogLevel.INFO );
        }
    }

    /**
     * Creates and initialize the context.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     *
     * @return the created context
     */
    Context createContext( final CommandLine commandLine, final Configuration config, final OptionResolver resolver )
    {
        Assert.notNull( "Command line", commandLine );
        Assert.notNull( "Configuration", config );
        Assert.notNull( "Resolver", resolver );

        final ServiceRegistry serviceRegistry = new ServiceRegistry( null );
        final EventDispatcher dispatcher = EventDispatcher.start( new Logger( Logger.LOG_DEBUG ) );
        serviceRegistry.addServiceListener( new ServiceListener()
        {
            public void serviceChanged( ServiceEvent event )
            {
                dispatcher.fireServiceEvent( event );
            }
        }
        );

        return new ContextImpl()
            .setCommandLine( commandLine )
            .setConfiguration( config )
            .setOptionResolver( resolver )
            .setServiceRegistry( serviceRegistry )
            .setEventDispatcher( dispatcher );
    }

    /**
     * Starts runner.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     */
    public void start( final CommandLine commandLine, final Configuration config, final OptionResolver resolver )
    {
        final Context context = createContext( commandLine, config, resolver );
        // install aditional handlers
        installHandlers( context );
        // install provisioning and bundles
        installBundles( installScanners( context ), new ExtensionBasedProvisionSchemaResolver(), context );
        // stop the dispatcher as there are no longer events around
        EventDispatcher.shutdown();
        // install platform and start it up
        startPlatform( installPlatform( context ), context );
    }

    /**
     * Installs url handler service configured handlers.
     *
     * @param context the running context
     */
    void installHandlers( final Context context )
    {
        LOGGER.debug( "Installing handlers" );
        final String option = context.getOptionResolver().get( OPTION_HANDLERS );
        if( option != null )
        {
            // first install each handler
            final Configuration config = context.getConfiguration();
            final String[] segments = option.split( "," );
            for( String segment : segments )
            {
                Assert.notEmpty( "Handler entry", segment );
                LOGGER.debug( "Handler [" + segment + "]" );
                final String activatorName = config.getProperty( segment );
                if( activatorName == null || activatorName.trim().length() == 0 )
                {
                    throw new ConfigurationException( "Handler [" + segment + "] is not supported" );
                }
                createActivator( segment, activatorName, context );
            }
            // then install the handler service
            // maintain this order as in this way the bundle context will be easier to respond to getServiceListeners
            final String serviceActivatorName = config.getProperty( HANDLER_SERVICE );
            if( serviceActivatorName == null || serviceActivatorName.trim().length() == 0 )
            {
                throw new ConfigurationException( "Handler Service must be configured [" + HANDLER_SERVICE + "]" );
            }
            createActivator( HANDLER_SERVICE, serviceActivatorName, context );
        }
    }

    /**
     * Installs provisioning service and configured scanners.
     *
     * @param context the running context
     *
     * @return installed provision service
     */
    ProvisionService installScanners( final Context context )
    {
        LOGGER.debug( "Installing provisioning" );
        final String option = context.getOptionResolver().getMandatory( OPTION_SCANNERS );
        // first install a dummy start level service that will record the start level set by scanners
        RunnerStartLevel.install( context.getServiceRegistry() );
        // then install each scanner
        final String[] segments = option.split( "," );
        for( String segment : segments )
        {
            Assert.notEmpty( "Scanner entry", segment );
            LOGGER.debug( "Scanner [" + segment + "]" );
            final String activatorName = context.getConfiguration().getProperty( segment );
            if( activatorName == null || activatorName.trim().length() == 0 )
            {
                throw new ConfigurationException( "Scanner [" + segment + "] is not supported" );
            }
            createActivator( segment, activatorName, context );
        }
        // then install the provisioning service
        // maintain this order as in this way the bundle context will be easier to respond to getServiceListeners
        final String serviceActivatorName = context.getConfiguration().getProperty( PROVISION_SERVICE );
        if( serviceActivatorName == null || serviceActivatorName.trim().length() == 0 )
        {
            throw new ConfigurationException( "Provision Service must be configured [" + PROVISION_SERVICE + "]" );
        }
        final BundleContext bundleContext = createActivator( PROVISION_SERVICE, serviceActivatorName, context );
        // sanity check
        if( bundleContext == null )
        {
            throw new RuntimeException( "Could not create bundle context for provision service" );
        }
        final ServiceReference reference = bundleContext.getServiceReference( ProvisionService.class.getName() );
        if( reference == null )
        {
            throw new RuntimeException( "Could not resolve a provision service" );
        }
        return (ProvisionService) bundleContext.getService( reference );
    }

    /**
     * By using provision service it installs provisioned bundles.
     *
     * @param provisionService installed provision service
     * @param schemaResolver   a provision schema resolver
     * @param context          the running context
     */
    void installBundles( final ProvisionService provisionService, final ProvisionSchemaResolver schemaResolver,
                         final Context context )
    {
        LOGGER.debug( "Installing bundles" );
        if( provisionService == null )
        {
            throw new RuntimeException( "Could not resolve a provision service" );
        }
        List<String> arguments = context.getCommandLine().getArguments();
        if( arguments == null || arguments.size() == 0 )
        {
            final String defaultProvisionURL = context.getConfiguration().getProperty( PROVISION_DEFAULT_URL );
            Assert.notNull( "Provision url", defaultProvisionURL );
            arguments = new ArrayList<String>();
            final String[] urls = defaultProvisionURL.split( "," );
            for( String url : urls )
            {
                arguments.add( url );
            }
        }
        // backup properties and replace them with audited properties
        final Properties sysPropsBackup = System.getProperties();
        try
        {
            context.setSystemProperties( new AuditedProperties( sysPropsBackup ) );
            System.setProperties( context.getSystemProperties() );
            // then scan those url's
            for( String provisionURL : arguments )
            {
                try
                {
                    try
                    {
                        provisionService.scan( provisionURL ).install();
                    }
                    catch( UnsupportedSchemaException e )
                    {
                        final String resolvedProvisionURL = schemaResolver.resolve( provisionURL );
                        if( resolvedProvisionURL != null && !resolvedProvisionURL.equals( provisionURL ) )
                        {
                            provisionService.scan( resolvedProvisionURL ).install();
                        }
                        else
                        {
                            throw e;
                        }
                    }
                }
                catch( MalformedSpecificationException e )
                {
                    throw new RuntimeException( e );
                }
                catch( ScannerException e )
                {
                    throw new RuntimeException( e );
                }
                catch( BundleException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
        finally
        {
            // restore the backup-ed properties
            System.setProperties( sysPropsBackup );
        }
    }

    /**
     * Installs platform extender and configured platform.
     *
     * @param context the running context
     *
     * @return installed platform
     */
    Platform installPlatform( final Context context )
    {
        LOGGER.debug( "Installing platform" );
        // first install platform
        final String platform = context.getOptionResolver().getMandatory( OPTION_PLATFORM );
        String version = context.getOptionResolver().get( OPTION_PLATFORM_VERSION );
        if( version == null )
        {
            version = context.getOptionResolver().get( platform + "." + OPTION_PLATFORM_VERSION );
            if( version == null )
            {
                throw new ConfigurationException( "Could not resolve a version for platform [" + platform + "]" );
            }
        }
        final String activatorName = context.getConfiguration().getProperty( platform + "." + version );
        if( activatorName == null || activatorName.trim().length() == 0 )
        {
            throw new ConfigurationException( "Platform [" + platform + " " + version + "] is not supported" );
        }
        createActivator( platform, activatorName, context );
        // then install platform service
        final String serviceActivatorName = context.getConfiguration().getProperty( PLATFORM_SERVICE );
        if( serviceActivatorName == null || serviceActivatorName.trim().length() == 0 )
        {
            throw new ConfigurationException( "Platform Service must be configured [" + PLATFORM_SERVICE + "]" );
        }
        final BundleContext bundleContext = createActivator( PLATFORM_SERVICE, serviceActivatorName, context );
        // sanity check
        if( bundleContext == null )
        {
            throw new RuntimeException( "Could not create bundle context for platform service" );
        }
        final ServiceReference reference = bundleContext.getServiceReference( Platform.class.getName() );
        if( reference == null )
        {
            throw new RuntimeException( "Could not resolve a platform" );
        }
        return (Platform) bundleContext.getService( reference );
    }

    /**
     * Startes the installed platform.
     *
     * @param context  the running context
     * @param platform installed platform
     */
    private void startPlatform( final Platform platform, final Context context )
    {
        LOGGER.debug( "Starting platform" );
        if( platform == null )
        {
            throw new RuntimeException( "Could not resolve a platform" );
        }
        final List<RunnerBundle> installedBundles = context.getBundles();
        final List<BundleReference> references = new ArrayList<BundleReference>();
        if( installedBundles != null )
        {
            for( RunnerBundle bundle : installedBundles )
            {
                references.add(
                    new BundleReferenceBean(
                        bundle.getLocationAsURL().toExternalForm(),
                        bundle.getLocationAsURL(),
                        bundle.getStartLevel(),
                        bundle.shouldStart()
                    )
                );
            }
        }
        try
        {
            platform.start( references, context.getSystemProperties(), null );
        }
        catch( PlatformException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Activator factory method.
     *
     * @param bundleName     name of the bundle to be created
     * @param activatorClazz class name of the activator
     * @param context        the running context
     *
     * @return activator related bundle context
     */
    BundleContext createActivator( final String bundleName, final String activatorClazz, final Context context )
    {
        try
        {
            final BundleActivator activator = (BundleActivator) Class.forName( activatorClazz ).newInstance();
            final BundleContext bundleContext = new RunnerBundleContext( context );
            activator.start( bundleContext );
            return bundleContext;
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Could not create [" + bundleName + "]", e );
        }
    }

    /**
     * Display ops4j logo to console.
     */
    private static void showLogo()
    {
        System.out.println( "    ______  ________  __  __" );
        System.out.println( "   / __  / /  __   / / / / /" );
        System.out.println( "  /  ___/ /  __   / _\\ \\ _/" );
        System.out.println( " /  /    /  / /  / / _\\ \\" );
        System.out.println( "/__/    /__/ /__/ /_/ /_/" );
        System.out.println();
        System.out.println( "Pax Runner from OPS4J - http://www.ops4j.org" );
        System.out.println( "--------------------------------------------" );
        System.out.println();
    }

    /**
     * Show execution problem to console.
     *
     * @param t the problem
     */
    private static void showError( Throwable t )
    {
        String message = "";
        String debugInfo = "";
        if( LOGGER != null && !LOGGER.isErrorEnabled() )
        {
            message = t.getMessage();
            debugInfo = "Use --" + OPTION_DEBUG + "=ERROR to see details.";
        }
        System.out.println( "         ___" );
        System.out.println( "        /  /" );
        System.out.println( "       /  / Ops, there has been a problem!" );
        System.out.println( "      /  /  " );
        System.out.println( "     /__/   " + message );
        System.out.println( "    ___" );
        System.out.println( "   /__/     " + debugInfo );
        System.out.println();

        if( LOGGER == null || LOGGER.isErrorEnabled() )
        {
            if( LOGGER != null )
            {
                LOGGER.error( t );
            }
            t.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    public static void main( final String... args )
    {
        try
        {
            showLogo();

            final CommandLine commandLine = new CommandLineImpl( args );
            initializeLogger( commandLine );
            String configURL = commandLine.getOption( OPTION_CONFIG );
            if( configURL == null )
            {
                configURL = "classpath:META-INF/runner.properties";
            }
            final Configuration config = new ConfigurationImpl( configURL );
            new Run().start(
                commandLine,
                config,
                new OptionResolverImpl( commandLine, config )
            );
        }
        catch( Throwable t )
        {
            showError( t );
            // TODO eliminate system exit as in this case it should runner should be shutdown nicely by stopping the running services
            System.exit( 1 );
        }
    }

    /**
     * Initialize the logger based on option "debug".
     *
     * @param commandLine command lin ein use
     */
    private static void initializeLogger( final CommandLine commandLine )
    {
        String debug = commandLine.getOption( OPTION_DEBUG );
        if( debug != null )
        {
            try
            {
                LOGGER = LogFactory.getLog( Run.class, LogLevel.valueOf( debug.toUpperCase() ) );
            }
            catch( Exception ignore )
            {
                LOGGER = LogFactory.getLog( Run.class, LogLevel.INFO );
                LOGGER.warn( "Unknown debug option [" + debug + "], switching to INFO" );
            }
        }
        else
        {
            LOGGER = LogFactory.getLog( Run.class, LogLevel.INFO );
        }
    }

}
