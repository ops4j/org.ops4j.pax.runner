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
package org.ops4j.pax.runner.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import static org.ops4j.pax.runner.CommandLine.*;

import org.ops4j.pax.runner.*;
import org.ops4j.pax.runner.osgi.felix.Context;
import org.ops4j.pax.runner.osgi.felix.StandeloneFramework;
import org.ops4j.pax.scanner.InstallableBundles;
import org.ops4j.pax.scanner.MalformedSpecificationException;
import org.ops4j.pax.scanner.ProvisionService;
import org.ops4j.pax.scanner.ScannedBundle;
import org.ops4j.pax.scanner.ScannerException;
import org.ops4j.pax.scanner.UnsupportedSchemaException;

public class RunnerStandeloneFrameworkTest
{

    private CommandLine m_commandLine;
    private Configuration m_config;
    private OptionResolver m_resolver;
    private Recorder m_recorder;
    private BundleContext m_bundleContext;
    private ProvisionService m_provisionService;
	private StandeloneFramework standeloneFramework;
	private RunnerStandeloneFramework runnerInit;

    @Before
    public void setUp()
    {
        m_commandLine = createMock( CommandLine.class );
        m_config = createMock( Configuration.class );
        m_resolver = createMock( OptionResolver.class );
        m_recorder = createMock( Recorder.class );
        m_bundleContext = createMock( BundleContext.class );
        m_provisionService = createMock( ProvisionService.class );
    }
   
    /**
     * Starts runner with a java runner.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     * @param runner      java runner service
     * @throws BundleException 
     */
    public void start(final CommandLine commandLine, final Configuration config, final OptionResolver resolver) throws BundleException
    {
    	start(new StandeloneFramework(resolver), commandLine, config, resolver);
	}

    /**
     * Starts runner with a java runner.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     * @param runner      java runner service
     * @throws BundleException 
     */
    public void start( StandeloneFramework f, final CommandLine commandLine, final Configuration config, final OptionResolver resolver) throws BundleException
    {
    	// start minimal osgi framework
    	standeloneFramework = f;
    	standeloneFramework.start();
    	Context context = standeloneFramework.getContext();
        context.setCommandLine(commandLine);
        context.setConfiguration(config);
        
        // install handler and service
    	runnerInit = 
        	new RunnerStandeloneFramework( commandLine, config, resolver);
        runnerInit.start(standeloneFramework);
    	
    }
    @Test( expected = IllegalArgumentException.class )
    public void startWithNullCommandLine() throws BundleException
    {
       start( null, createMock( Configuration.class ), createMock( OptionResolver.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullConfiguration() throws BundleException
    {
        start( createMock( CommandLine.class ), null, createMock( OptionResolver.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullResolver() throws BundleException
    {
        start( createMock( CommandLine.class ), createMock( Configuration.class ), null );
    }

    // test runner flow
    @Test
    public void startFlow() throws BundleException
    {
        m_recorder.record( "cleanup()" );
        m_recorder.record( "installServices()" );
        m_recorder.record( "installHandlers()" );
        replay( m_commandLine, m_config, m_recorder, m_resolver, m_bundleContext );
        new RunnerStandeloneFramework(m_commandLine, m_config, m_resolver )
        {
        	
            @Override
            public void cleanup( final OptionResolver resolver )
            {
                m_recorder.record( "cleanup()" );
            }

            @Override
            public void installHandlers(Configuration config, OptionResolver optionResolver, CreateActivator ca) 
            {
                m_recorder.record( "installHandlers()" );
            }

            @Override
            public ProvisionService installScanners(Configuration config, OptionResolver optionResolver, CreateActivator ca)
            {
                m_recorder.record( "installScanners()" );
                return m_provisionService;
            }

            @Override
            public void installServices(Configuration config, OptionResolver optionResolver, CreateActivator ca)
            {
                m_recorder.record( "installServices()" );
            }

        }.start( null );
        verify( m_commandLine, m_config, m_recorder, m_resolver, m_bundleContext );
    }

    // if there are no handlers just go one as one may choose to use only the default ones from JVM
    @Test
    public void startWithNoHandlers()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
                fail( "Not expected to be called" );
                return null;
            }
        };

        expect( m_resolver.get( "handlers" ) ).andReturn( null );

        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        run2.installHandlers( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    // test that if we have valid handlers then the handler service + handlers are started
    @Test
    public void startWithValidHandlers()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
        
        expect( m_resolver.get( "handlers" ) ).andReturn( "handler.1,handler.2" );
        expect( m_config.getProperty( "handler.service" ) ).andReturn( "handler.service.Activator" );
        expect( m_config.getProperty( "handler.1" ) ).andReturn( "handler.1.Activator" );
        expect( m_config.getProperty( "handler.2" ) ).andReturn( "handler.2.Activator" );

        m_recorder.record( "handler.service.Activator" );
        m_recorder.record( "handler.1.Activator" );
        m_recorder.record( "handler.2.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.installHandlers( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithInvalidHandlers() throws BundleException
    {
        expect( m_resolver.get( "clean" ) ).andReturn( null );
        expect( m_resolver.get( "executor" ) ).andReturn( null );
        expect( m_resolver.get( "services" ) ).andReturn( null );
        expect( m_resolver.get( "handlers" ) ).andReturn( "handler.1" );
        expect( m_config.getProperty( "handler.service" ) ).andReturn( "handler.service.Activator" );
        expect( m_config.getProperty( "handler.1" ) ).andReturn( null );

        m_recorder.record( "handler.service.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.start( run );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithNotConfiguredHandlerService()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
        
        expect( m_resolver.get( "handlers" ) ).andReturn( "handler.1" );
        expect( m_config.getProperty( "handler.1" ) ).andReturn( "handler.1.Activator" );
        expect( m_config.getProperty( "handler.service" ) ).andReturn( null );

        m_recorder.record( "handler.1.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.installHandlers( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    // verify that if there are no scanners we should just crash with an MissingOptionException
    @Test( expected = MissingOptionException.class )
    public void startWithNoScanners()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
        
        expect( m_resolver.getMandatory( "scanners" ) ).andThrow( new MissingOptionException( "scanners" ) );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.installScanners( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithNotConfiguredProvisionService()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
        
        expect( m_resolver.getMandatory( "scanners" ) ).andReturn( "scanner.1" );
        expect( m_config.getProperty( "scanner.1" ) ).andReturn( "scanner.1.Activator" );
        expect( m_config.getProperty( "provision.service" ) ).andReturn( null );

        m_recorder.record( "scanner.1.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.installScanners( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithInvalidScanners()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
       
        expect( m_resolver.getMandatory( "scanners" ) ).andReturn( "scanner.1" );
        expect( m_config.getProperty( "scanner.1" ) ).andReturn( null );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.installScanners( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    // test that if we have valid scanners then the provision service + handlers are started
    @Test
    public void startWithValidScanners()
    {
    	CreateActivator run = new CreateActivator()
        {
            // override this method just to be sure that is not called
            public BundleContext createActivator( final String handlerName, final String activatorName)
            {
            	 m_recorder.record( activatorName );
                 return m_bundleContext;
             }
        };
        
        expect( m_resolver.getMandatory( "scanners" ) ).andReturn( "scanner.1,scanner.2" );
        expect( m_config.getProperty( "provision.service" ) ).andReturn( "provision.service.Activator" );
        expect( m_config.getProperty( "scanner.1" ) ).andReturn( "scanner.1.Activator" );
        expect( m_config.getProperty( "scanner.2" ) ).andReturn( "scanner.2.Activator" );
        expect( m_bundleContext.getServiceReference( ProvisionService.class.getName() ) ).andReturn(
            createMock( ServiceReference.class )
        );
        expect( m_bundleContext.getService( (ServiceReference) notNull() ) ).andReturn( m_provisionService );

        m_recorder.record( "scanner.1.Activator" );
        m_recorder.record( "scanner.2.Activator" );
        m_recorder.record( "provision.service.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        RunnerStandeloneFramework run2 = new  RunnerStandeloneFramework(m_commandLine, m_config, m_resolver);
        run2.installScanners( m_config, m_resolver, run );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

//    // test that we getOption a runtime exception not a NullPointerException
//    @Test( expected = RuntimeException.class )
//    public void installBundlesWithNullProvisionService()
//    {
//        Run run = new Run();
//        Context context = run.createContext( m_commandLine, m_config, m_resolver );
//
//        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
//        run.installBundles( null, null, context );
//        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
//    }

//    // test bundles installation from arguments
//    @Test
//    public void installBundlesFromArguments()
//        throws ScannerException, MalformedSpecificationException, BundleException
//    {
//        Run run = new Run();
//        Context context = run.createContext( m_commandLine, m_config, m_resolver );
//
//        ProvisionService provisionService = createMock( ProvisionService.class );
//        InstallableBundles installables = createMock( InstallableBundles.class );
//        List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
//
//        expect( m_resolver.get( OPTION_PROFILES ) ).andReturn( null );
//        List<String> args = new ArrayList<String>();
//        args.add( "scan-file:file:bundles1.txt" );
//        args.add( "scan-file:file:bundles2.txt" );
//        expect( m_commandLine.getArguments() ).andReturn( args );
//        expect( provisionService.scan( "scan-file:file:bundles1.txt" ) ).andReturn( scannedBundles );
//        expect( provisionService.wrap( scannedBundles ) ).andReturn( installables );
//        expect( installables.install() ).andReturn( installables );
//        expect( provisionService.scan( "scan-file:file:bundles2.txt" ) ).andReturn( scannedBundles );
//        expect( provisionService.wrap( scannedBundles ) ).andReturn( installables );
//        expect( installables.install() ).andReturn( installables );
//
//        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
//                installables
//        );
//        run.installBundles( provisionService, null, context );
//        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
//                installables
//        );
//    }
//
//    // test bundles installation from arguments
//    @Test
//    public void installBundlesWithNoSchema()
//        throws ScannerException, MalformedSpecificationException, BundleException
//    {
//        Run run = new Run();
//        Context context = run.createContext( m_commandLine, m_config, m_resolver );
//
//        ProvisionService provisionService = createMock( ProvisionService.class );
//        InstallableBundles installables = createMock( InstallableBundles.class );
//        List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
//        ProvisionSchemaResolver schemaResolver = createMock( ProvisionSchemaResolver.class );
//
//        expect( m_resolver.get( OPTION_PROFILES ) ).andReturn( null );
//        List<String> args = new ArrayList<String>();
//        args.add( "bundles.txt" );
//        expect( m_commandLine.getArguments() ).andReturn( args );
//        expect( provisionService.scan( "bundles.txt" ) ).andThrow( new UnsupportedSchemaException( "test" ) );
//        expect( schemaResolver.resolve( "bundles.txt" ) ).andReturn( "scan-file:file:bundles.txt" );
//        expect( provisionService.scan( "scan-file:file:bundles.txt" ) ).andReturn( scannedBundles );
//        expect( provisionService.wrap( scannedBundles ) ).andReturn( installables );
//        expect( installables.install() ).andReturn( installables );
//
//        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
//                installables, schemaResolver
//        );
//        run.installBundles( provisionService, schemaResolver, context );
//        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
//                installables, schemaResolver
//        );
//    }
//
//    // test bundles installation with no arguments and no default configuration
//    // expected to just pass and do nothing
//    public void installBundlesWithNoArgumentsAndNoDefault()
//    {
//        Run run = new Run();
//        Context context = run.createContext( m_commandLine, m_config, m_resolver );
//
//        ProvisionService provisionService = createMock( ProvisionService.class );
//
//        expect( m_commandLine.getArguments() ).andReturn( null );
//        expect( m_config.getProperty( "default.provision.url" ) ).andReturn( null );
//
//        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService );
//        run.installBundles( provisionService, null, context );
//        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService );
//    }

}
