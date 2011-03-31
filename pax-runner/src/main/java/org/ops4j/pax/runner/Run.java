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

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.LogLevel;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.util.EventDispatcher;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import static org.ops4j.pax.runner.CommandLine.*;
import org.ops4j.pax.runner.commons.Info;
import org.ops4j.pax.runner.osgi.RunnerBundle;
import org.ops4j.pax.runner.osgi.RunnerBundleContext;
import org.ops4j.pax.runner.osgi.RunnerStartLevel;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.BundleReferenceBean;
import org.ops4j.pax.runner.platform.InProcessJavaRunner;
import org.ops4j.pax.runner.platform.InitDScriptRunner;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.ScriptJavaRunner;
import org.ops4j.pax.runner.platform.SystemFileReference;
import org.ops4j.pax.runner.platform.SystemFileReferenceBean;
import org.ops4j.pax.runner.platform.ZipJavaRunner;
import org.ops4j.pax.scanner.MalformedSpecificationException;
import org.ops4j.pax.scanner.ProvisionService;
import org.ops4j.pax.scanner.ScannedBundle;
import org.ops4j.pax.scanner.ScannerException;
import org.ops4j.pax.scanner.UnsupportedSchemaException;

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
     * Handler service configuration property name.
     */
    private static final String HANDLER_SERVICE = "handler.service";
    /**
     * Provision service configuration property name.
     */
    private static final String PROVISION_SERVICE = "provision.service";
    /**
     * Platform extender configuration property name.
     */
    private static final String PLATFORM_SERVICE = "platform.service";
    /**
     * Clean start configuration property name.
     */
    private static final String CLEAN_START = "clean";
    /**
     * Working directory configuration property name.
     */
    private static final String WORKING_DIRECTORY = "workingDirectory";

    /**
     * Creates a new runner.
     */
    public Run()
    {
        if( LOGGER == null )
        {
            createLogger();
        }
    }

    /**
     * {@inheritDoc}
     */
    public static void main( final String... args )
    {
        try
        {
            main( null, args );
        }
        catch( Throwable t )
        {
            showError( t );
            System.exit( 1 );
        }
    }

    /**
     * Start OSGi framework based on command-line arguments, using external Java runner service.
     *
     * @param runner java runner service
     * @param args   command-line arguments
     */
    public static void main( final JavaRunner runner, final String... args )
    {
        final CommandLine commandLine = new CommandLineImpl( args );

        boolean disableLogo = Boolean.valueOf( commandLine.getOption( OPTION_NOLOGO ) );
        if ( !disableLogo )
        {
            showLogo();
        }

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
            new OptionResolverImpl( commandLine, config ),
            runner
        );
    }

    public static Log getLogger()
    {
        createLogger();
        return LOGGER;
    }

    /**
     * Starts runner.
     *
     * @param runner java runner service
     * @param args   command-line arguments
     */
    public static void start( final JavaRunner runner, final String... args )
    {
        final CommandLine commandLine = new CommandLineImpl( args );
        String configURL = commandLine.getOption( OPTION_CONFIG );
        if( configURL == null )
        {
            configURL = "classpath:META-INF/runner.properties";
        }
        final Configuration config = new ConfigurationImpl( configURL );
        new Run().start(
            commandLine,
            config,
            new OptionResolverImpl( commandLine, config ),
            runner
        );
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
        start( commandLine, config, resolver, null );
    }

    /**
     * Starts runner with a java runner.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     * @param runner      java runner service
     */
    public void start( final CommandLine commandLine, final Configuration config, final OptionResolver resolver,
                       final JavaRunner runner )
    {
        final Context context = createContext( commandLine, config, resolver );
        LOGGER.info( commandLine );
        // cleanup if requested
        cleanup( resolver );
        // install aditional services
        installServices( context );
        // install aditional handlers
        installHandlers( context );
        // install provisioning and bundles
        installBundles( installScanners( context ), new ExtensionBasedProvisionSchemaResolver(), context );
        // stop the dispatcher as there are no longer events around
        EventDispatcher.shutdown();
        // install platform and start it up
        startPlatform( installPlatform( context ), context, runner == null ? createJavaRunner( resolver ) : runner );
    }

    /**
     * Removes the working directory if option specified.
     *
     * @param resolver option resolver
     */
    void cleanup( final OptionResolver resolver )
    {
        final boolean cleanStart = Boolean.valueOf( resolver.get( CLEAN_START ) );
        if( cleanStart )
        {
            final File workingDir = new File( resolver.getMandatory( WORKING_DIRECTORY ) );
            LOGGER.debug( "Removing working directory [" + workingDir.getAbsolutePath() + "]" );
            FileUtils.delete( workingDir );
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
        NullArgumentException.validateNotNull( commandLine, "Command line" );
        NullArgumentException.validateNotNull( config, "Configuration" );
        NullArgumentException.validateNotNull( resolver, "PropertyResolver" );

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
     * Creates a Java runner based on "runner" option.
     *
     * @param resolver an option resolver
     *
     * @return a java runner
     */
    JavaRunner createJavaRunner( final OptionResolver resolver )
    {
        NullArgumentException.validateNotNull( resolver, "PropertyResolver" );

        LOGGER.debug( "Creating Java Runner" );
        final String executor = resolver.get( OPTION_EXECUTOR );
        if( executor == null || executor.trim().length() == 0 )
        {
            LOGGER.debug( "Using default executor" );
            return null;
        }
        if( "noop".equalsIgnoreCase( executor ) )
        {
            LOGGER.debug( "Using noop executor" );
            return new NoopJavaRunner();
        }
        if( "script".equalsIgnoreCase( executor ) )
        {
            LOGGER.debug( "Using script executor" );
            return new ScriptJavaRunner();
        }
        if( "zip".equalsIgnoreCase( executor ) )
        {
            LOGGER.debug( "Using zip executor" );
            return new ZipJavaRunner();
        }
        if( "inProcess".equalsIgnoreCase( executor ) )
        {
            LOGGER.debug( "Using in process executor" );
            return new InProcessJavaRunner();
        }
        if( executor.startsWith("init.d" ))
        {
            String[] data    = executor.split(",");
            String   appName = (data.length > 1 && data[1].length() > 0) ? data[1] : null;
            return new InitDScriptRunner(appName);
        }
        try
        {
            final JavaRunner javaRunner = (JavaRunner) getClass().getClassLoader().loadClass( executor ).newInstance();
            LOGGER.debug( "Using " + executor + " executor" );
            return javaRunner;
        }
        catch( Exception ignore )
        {
            LOGGER.debug( "Connot load executor: " + executor + " reason: " + ignore.getMessage() );
        }
        throw new ConfigurationException( "Executor [" + executor + "] is not supported" );
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
                NullArgumentException.validateNotEmpty( segment, "Handler entry" );
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
            NullArgumentException.validateNotEmpty( segment, "Scanner entry" );
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
     * Installs additional services.
     *
     * @param context the running context
     */
    void installServices( final Context context )
    {
        LOGGER.debug( "Installing additional services" );
        final String option = context.getOptionResolver().get( OPTION_SERVICES );
        if( option != null )
        {
            final Configuration config = context.getConfiguration();
            final String[] segments = option.split( "," );
            for( String segment : segments )
            {
                NullArgumentException.validateNotEmpty( segment, "Service entry" );
                LOGGER.debug( "Installing service [" + segment + "]" );
                final String activatorName = config.getProperty( segment );
                if( activatorName == null || activatorName.trim().length() == 0 )
                {
                    throw new ConfigurationException( "Service [" + segment + "] is not supported" );
                }
                createActivator( segment, activatorName, context );
            }
        }
    }

    /**
     * By using provision service it installs provisioned bundles.
     *
     * @param provisionService installed provision service
     * @param schemaResolver   a provision schema resolver
     * @param context          the running context
     */
    void installBundles( final ProvisionService provisionService,
                         final ProvisionSchemaResolver schemaResolver,
                         final Context context )
    {
        if( provisionService == null )
        {
            throw new RuntimeException( "Could not resolve a provision service" );
        }
        // build list of provisioning specs out of command line arguments and profiles
        final List<String> provisionSpecs = new ArrayList<String>();
        provisionSpecs.addAll( context.getCommandLine().getArguments() );
        provisionSpecs.addAll( transformProfilesToProvisionSpecs( context ) );

        // backup properties and replace them with audited properties
        final Properties sysPropsBackup = System.getProperties();
        try
        {
            context.setSystemProperties( new AuditedProperties( sysPropsBackup ) );
            System.setProperties( context.getSystemProperties() );

            final Set<ScannedBundle> scannedBundles = new HashSet<ScannedBundle>();
            // then scan those url's
            for( String provisionSpec : provisionSpecs )
            {
                try
                {
                    try
                    {
                        provisionService.wrap(
                            filterUnique( scannedBundles, provisionService.scan( provisionSpec ) )
                        ).install();
                    }
                    catch( UnsupportedSchemaException e )
                    {
                        final String resolvedProvisionURL = schemaResolver.resolve( provisionSpec );
                        if( resolvedProvisionURL != null && !resolvedProvisionURL.equals( provisionSpec ) )
                        {
                            provisionService.wrap(
                                filterUnique( scannedBundles, provisionService.scan( resolvedProvisionURL ) )
                            ).install();
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
     * Transforms requested profiles (--profiles option) to provisioning specs (scan-composite).
     *
     * @param context runner context
     *
     * @return list of transformed provisioning specs or an empty list if there are no profiles.
     */
    private List<String> transformProfilesToProvisionSpecs( final Context context )
    {
        final List<String> provisionSpecs = new ArrayList<String>();

        final String profilesOption = context.getOptionResolver().get( OPTION_PROFILES );
        if( profilesOption != null && profilesOption.trim().length() > 0 )
        {
            final String profilesGroup = context.getOptionResolver().get( OPTION_PROFILES_GROUPID );
            final String[] profiles = profilesOption.split( ":" );
            for( String profile : profiles )
            {
                // TODO Maybe a nice/safe parsing of profile name into group/artifact/version ?
                final int startOfOptions = profile.indexOf( org.ops4j.pax.scanner.ServiceConstants.SEPARATOR_OPTION );
                String options = null;
                if( startOfOptions > 0 )
                {
                    options = profile.substring( startOfOptions );
                    profile = profile.substring( 0, startOfOptions );
                }
                final String[] parts = profile.split( "/" );
                provisionSpecs.add(
                    new StringBuilder()
                        .append( org.ops4j.pax.scanner.composite.ServiceConstants.SCHEMA )
                        .append( org.ops4j.pax.scanner.ServiceConstants.SEPARATOR_SCHEME )
                        .append( org.ops4j.pax.url.mvn.ServiceConstants.PROTOCOL )
                        .append( ":" )
                        .append( parts.length < 3 ? profilesGroup + "/" : "" )
                        .append( profile )
                        .append( parts.length < 2 ? "/" : "" )
                        .append( "/composite" )
                        .append( options != null ? options : "" )
                        .toString()
                );
            }
        }
        return provisionSpecs;
    }

    /**
     * Filter all scanned bundles that already exists in the provided set and add the unique ones to the set.
     *
     * @param alreadyScanned set of already scanned bundles
     * @param scannedBundles to be filtered
     *
     * @return unique list of scanned bundles (that were not already present in the provided set)
     */
    private List<ScannedBundle> filterUnique( final Set<ScannedBundle> alreadyScanned,
                                              final List<ScannedBundle> scannedBundles )
    {
        final Set<ScannedBundle> unique = new LinkedHashSet<ScannedBundle>( scannedBundles );
        unique.removeAll( alreadyScanned );
        alreadyScanned.addAll( unique );
        return new ArrayList<ScannedBundle>( unique );
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
        String version;
        if( Boolean.parseBoolean( context.getOptionResolver().get( OPTION_PLATFORM_VERSION_SNAPSHOT ) ) )
        {
            version = PLATFORM_VERSION_SNAPSHOT;
        }
        else
        {
            version = context.getOptionResolver().get( OPTION_PLATFORM_VERSION );
        }
        if( version == null )
        {
            version = context.getOptionResolver().get( platform + "." + OPTION_PLATFORM_VERSION );
            if( version == null )
            {
                throw new ConfigurationException( "Could not resolve a version for platform [" + platform + "]" );
            }
        }
        version = version.toUpperCase();
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
        final ServiceReference[] references;
        try
        {
            references = bundleContext.getServiceReferences( Platform.class.getName(), "(version=" + version + ")" );
        }
        catch( InvalidSyntaxException ignore )
        {
            // this should never happen
            throw new ConfigurationException( "Platform [" + platform + " " + version + "] is not supported" );
        }
        if( references == null || references.length == 0 )
        {
            throw new RuntimeException( "Could not resolve a platform" );
        }
        final ServiceReference reference = references[ 0 ];
        return (Platform) bundleContext.getService( reference );
    }

    /**
     * Starts the installed platform.
     *
     * @param context  the running context
     * @param platform installed platform
     * @param runner   Java runner service
     */
    private void startPlatform( final Platform platform, final Context context, final JavaRunner runner )
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
                LOGGER.info( "Provision bundle [" + bundle + "]" );
                references.add(
                    new BundleReferenceBean(
                        bundle.getLocationAsURL().toExternalForm(),
                        bundle.getLocationAsURL(),
                        bundle.getStartLevel(),
                        bundle.shouldStart(),
                        bundle.shouldUpdate()
                    )
                );
            }
        }
        try
        {
            platform.start( determineSystemFiles( context ), references, context.getSystemProperties(), null, runner );
        }
        catch( PlatformException e )
        {
            throw new RuntimeException( e );
        }
    }

    List<SystemFileReference> determineSystemFiles( final Context context )
    {
        final List<SystemFileReference> systemFiles = new ArrayList<SystemFileReference>();
        try
        {
            final String[] bcppUrls = context.getOptionResolver().getMultiple( CommandLine.OPTION_BOOT_CP_PREPEND );
            if( bcppUrls.length > 0 )
            {
                for( String url : bcppUrls )
                {
                    systemFiles.add( new SystemFileReferenceBean( url, new URL( url ), true ) );
                }
            }
            final String[] bcpaUrls = context.getOptionResolver().getMultiple( CommandLine.OPTION_BOOT_CP_APPEND );
            if( bcpaUrls.length > 0 )
            {
                for( String url : bcpaUrls )
                {
                    systemFiles.add( new SystemFileReferenceBean( url, new URL( url ), false ) );
                }
            }

        }
        catch( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }
        return systemFiles;
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
        System.out.println( "__________                 __________                                 " );
        System.out.println( "\\______   \\_____  ___  ___ \\______   \\__ __  ____   ____   ___________" );
        System.out.println( "|     ___/\\__  \\ \\  \\/  /  |       _/  |  \\/    \\ /    \\_/ __ \\_  __ \\" );
        System.out.println( "|    |     / __ \\_>    <   |    |   \\  |  /   |  \\   |  \\  ___/|  | \\/" );
        System.out.println( "|____|    (____  /__/\\_ \\  |____|_  /____/|___|  /___|  /\\___  >__|   " );
        System.out.println( "               \\/      \\/         \\/           \\/     \\/     \\/       " );
        System.out.println();
        final String logo = "Pax Runner " + getVersion() + "from OPS4J - http://www.ops4j.org";
        System.out.println( logo );
        System.out.println(
            "--------------------------------------------------------------------------------------------------------"
                .substring( 0, logo.length() )
        );
        System.out.println();
    }

    /**
     * Discovers the Pax Runner version. If version cannot be determined returns an empty string.
     *
     * @return pax runner version
     */
    private static String getVersion()
    {
        try
        {
            final InputStream is = Run.class.getClassLoader().getResourceAsStream( "META-INF/runner.version" );
            if( is != null )
            {
                final Properties properties = new Properties();
                properties.load( is );
                final String version = properties.getProperty( "version" );
                if( version != null )
                {
                    return "(" + version + ") ";
                }
                return "";
            }
            return "";
        }
        catch( Exception ignore )
        {
            return "";
        }
    }

    /**
     * Show execution problem to console.
     *
     * @param t the problem
     */
    private static void showError( Throwable t )
    {
        Info.println();
        String message = t.getMessage();
        String debugInfo = "";

        if( LOGGER != null && !LOGGER.isDebugEnabled() )
        {
            debugInfo = "Use --" + OPTION_LOG + "=debug to see details.";
        }

        System.out.println( "         ___" );
        System.out.println( "        /  /" );
        System.out.println( "       /  / Oops, there has been a problem!" );
        System.out.println( "      /  /  " );
        System.out.println( "     /__/   " + message );
        System.out.println( "    ___" );
        System.out.println( "   /__/     " + debugInfo );
        System.out.println();

        if( LOGGER == null )
        {
            // show error even when LOGGER was not initialised
            System.out.println( "Exception caught during execution:" );
            t.printStackTrace();
        }
        else
        {
            if( LOGGER.isDebugEnabled() )
            {
                LOGGER.error( "Exception caught during execution:", t );
            }
        }

    }

    /**
     * Initialize the logger based on option "debug".
     *
     * @param commandLine command lin ein use
     */
    private static void initializeLogger( final CommandLine commandLine )
    {
        String debug = commandLine.getOption( OPTION_LOG );
        if( debug != null )
        {
            try
            {
                createLogger( LogLevel.valueOf( debug.toUpperCase() ) );
            }
            catch( Exception ignore )
            {
                createLogger( LogLevel.INFO );
                LOGGER.warn( "Unknown debug option [" + debug + "], switching to INFO" );
            }
        }
        else
        {
            createLogger( LogLevel.INFO );
        }
    }

    /**
     * Creates the logger to use at the specified log level. The log level is only supported by the "special" JCL
     * implementation embedded into Pax Runner. In case that the JCL in the classpath in snot the embedded one it will
     * fallback to standard JCL usage.
     *
     * @param logLevel log level to use
     */
    private static void createLogger( final LogLevel logLevel )
    {
        try
        {
            LOGGER = LogFactory.getLog( Run.class, logLevel );
        }
        catch( NoSuchMethodError ignore )
        {
            // fall back to standard JCL
            LOGGER = LogFactory.getLog( Run.class );
        }
    }

    /**
     * Creates a default logger at INFo level.
     */
    private static void createLogger()
    {
        try
        {
            createLogger( LogLevel.INFO );
        }
        catch( NoClassDefFoundError ignore )
        {
            // fall back to standard JCL
            LOGGER = LogFactory.getLog( Run.class );
        }
    }
}
