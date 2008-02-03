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
package org.ops4j.pax.runner;

import java.util.ArrayList;
import java.util.List;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.provision.InstallableBundles;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.UnsupportedSchemaException;

public class RunTest
{

    private CommandLine m_commandLine;
    private Configuration m_config;
    private OptionResolver m_resolver;
    private Recorder m_recorder;
    private BundleContext m_bundleContext;
    private ProvisionService m_provisionService;
    private Platform m_platform;

    @Before
    public void setUp()
    {
        m_commandLine = createMock( CommandLine.class );
        m_config = createMock( Configuration.class );
        m_resolver = createMock( OptionResolver.class );
        m_recorder = createMock( Recorder.class );
        m_bundleContext = createMock( BundleContext.class );
        m_provisionService = createMock( ProvisionService.class );
        m_platform = createMock( Platform.class );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullCommandLine()
    {
        new Run().start( null, createMock( Configuration.class ), createMock( OptionResolver.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullConfiguration()
    {
        new Run().start( createMock( CommandLine.class ), null, createMock( OptionResolver.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullResolver()
    {
        new Run().start( createMock( CommandLine.class ), createMock( Configuration.class ), null );
    }

    // test runner flow
    @Test
    public void startFlow()
    {
        m_recorder.record( "installServices()" );
        m_recorder.record( "installHandlers()" );
        m_recorder.record( "installScanners()" );
        m_recorder.record( "installBundles()" );
        m_recorder.record( "installPlatform()" );
        replay( m_commandLine, m_config, m_recorder, m_resolver, m_bundleContext );
        new Run()
        {
            @Override
            void installHandlers( final Context context )
            {
                m_recorder.record( "installHandlers()" );
            }

            @Override
            ProvisionService installScanners( final Context context )
            {
                m_recorder.record( "installScanners()" );
                return m_provisionService;
            }

            @Override
            Platform installPlatform( final Context context )
            {
                m_recorder.record( "installPlatform()" );
                return m_platform;
            }

            @Override
            void installBundles( final ProvisionService provisionService, final ProvisionSchemaResolver schemaResolver,
                                 final Context context )
            {
                m_recorder.record( "installBundles()" );
            }

            @Override
            void installServices( final Context context )
            {
                m_recorder.record( "installServices()" );
            }

        }.start( m_commandLine, m_config, m_resolver );
        verify( m_commandLine, m_config, m_recorder, m_resolver, m_bundleContext );
    }

    // if there are no handlers just go one as one may choose to use only the default ones from JVM
    @Test
    public void startWithNoHandlers()
    {
        Run run = new Run()
        {
            // override this method just to be sure that is not called
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                fail( "Not expected to be called" );
                return null;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        expect( m_resolver.get( "handlers" ) ).andReturn( null );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        run.installHandlers( context );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    // test that if we have valid handlers then the handler service + handlers are started
    @Test
    public void startWithValidHandlers()
    {
        Run run = new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                m_recorder.record( activatorName );
                return m_bundleContext;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        expect( m_resolver.get( "handlers" ) ).andReturn( "handler.1,handler.2" );
        expect( m_config.getProperty( "handler.service" ) ).andReturn( "handler.service.Activator" );
        expect( m_config.getProperty( "handler.1" ) ).andReturn( "handler.1.Activator" );
        expect( m_config.getProperty( "handler.2" ) ).andReturn( "handler.2.Activator" );

        m_recorder.record( "handler.service.Activator" );
        m_recorder.record( "handler.1.Activator" );
        m_recorder.record( "handler.2.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        run.installHandlers( context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithInvalidHandlers()
    {
        expect( m_resolver.get( "services" ) ).andReturn( null );
        expect( m_resolver.get( "handlers" ) ).andReturn( "handler.1" );
        expect( m_config.getProperty( "handler.service" ) ).andReturn( "handler.service.Activator" );
        expect( m_config.getProperty( "handler.1" ) ).andReturn( null );

        m_recorder.record( "handler.service.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                m_recorder.record( activatorName );
                return m_bundleContext;
            }
        }.start( m_commandLine, m_config, m_resolver );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithNotConfiguredHandlerService()
    {
        Run run = new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                m_recorder.record( activatorName );
                return m_bundleContext;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        expect( m_resolver.get( "handlers" ) ).andReturn( "handler.1" );
        expect( m_config.getProperty( "handler.1" ) ).andReturn( "handler.1.Activator" );
        expect( m_config.getProperty( "handler.service" ) ).andReturn( null );

        m_recorder.record( "handler.1.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        run.installHandlers( context );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    // verify that if there are no scanners we should just crash with an MissingOptionException
    @Test( expected = MissingOptionException.class )
    public void startWithNoScanners()
    {
        Run run = new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                m_recorder.record( activatorName );
                return m_bundleContext;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        expect( m_resolver.getMandatory( "scanners" ) ).andThrow( new MissingOptionException( "scanners" ) );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        run.installScanners( context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithNotConfiguredProvisionService()
    {
        Run run = new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                m_recorder.record( activatorName );
                return m_bundleContext;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        expect( m_resolver.getMandatory( "scanners" ) ).andReturn( "scanner.1" );
        expect( m_config.getProperty( "scanner.1" ) ).andReturn( "scanner.1.Activator" );
        expect( m_config.getProperty( "provision.service" ) ).andReturn( null );

        m_recorder.record( "scanner.1.Activator" );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        run.installScanners( context );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    @Test( expected = ConfigurationException.class )
    public void startWithInvalidScanners()
    {
        Run run = new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                fail( "Not expected to be called" );
                return null;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        expect( m_resolver.getMandatory( "scanners" ) ).andReturn( "scanner.1" );
        expect( m_config.getProperty( "scanner.1" ) ).andReturn( null );

        replay( m_commandLine, m_config, m_resolver, m_bundleContext );
        run.installScanners( context );
        verify( m_commandLine, m_config, m_resolver, m_bundleContext );
    }

    // test that if we have valid scanners then the provision service + handlers are started
    @Test
    public void startWithValidScanners()
    {
        Run run = new Run()
        {
            @Override
            BundleContext createActivator( final String handlerName, final String activatorName, final Context context )
            {
                m_recorder.record( activatorName );
                return m_bundleContext;
            }
        };
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

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
        run.installScanners( context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    // test that we getOption a runtime exception not a NullPointerException
    @Test( expected = RuntimeException.class )
    public void installBundlesWithNullProvisionService()
    {
        Run run = new Run();
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
        run.installBundles( null, null, context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext );
    }

    // test bundles installation from arguments
    @Test
    public void installBundlesFromArguments()
        throws ScannerException, MalformedSpecificationException, BundleException
    {
        Run run = new Run();
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        ProvisionService provisionService = createMock( ProvisionService.class );
        InstallableBundles installables = createMock( InstallableBundles.class );

        List<String> args = new ArrayList<String>();
        args.add( "scan-file:file:bundles1.txt" );
        args.add( "scan-file:file:bundles2.txt" );
        expect( m_commandLine.getArguments() ).andReturn( args );
        expect( provisionService.scan( "scan-file:file:bundles1.txt" ) ).andReturn( installables );
        expect( installables.install() ).andReturn( installables );
        expect( provisionService.scan( "scan-file:file:bundles2.txt" ) ).andReturn( installables );
        expect( installables.install() ).andReturn( installables );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
                installables
        );
        run.installBundles( provisionService, null, context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
                installables
        );
    }

    // test bundles installation from arguments
    @Test
    public void installBundlesWithNoSchema()
        throws ScannerException, MalformedSpecificationException, BundleException
    {
        Run run = new Run();
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        ProvisionService provisionService = createMock( ProvisionService.class );
        InstallableBundles installables = createMock( InstallableBundles.class );
        ProvisionSchemaResolver schemaResolver = createMock( ProvisionSchemaResolver.class );

        List<String> args = new ArrayList<String>();
        args.add( "bundles.txt" );
        expect( m_commandLine.getArguments() ).andReturn( args );
        expect( provisionService.scan( "bundles.txt" ) ).andThrow( new UnsupportedSchemaException( "test" ) );
        expect( schemaResolver.resolve( "bundles.txt" ) ).andReturn( "scan-file:file:bundles.txt" );
        expect( provisionService.scan( "scan-file:file:bundles.txt" ) ).andReturn( installables );
        expect( installables.install() ).andReturn( installables );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
                installables, schemaResolver
        );
        run.installBundles( provisionService, schemaResolver, context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService,
                installables, schemaResolver
        );
    }

    // test bundles installation with no arguments and no default configuration
    // expected to just pass and do nothing
    public void installBundlesWithNoArgumentsAndNoDefault()
    {
        Run run = new Run();
        Context context = run.createContext( m_commandLine, m_config, m_resolver );

        ProvisionService provisionService = createMock( ProvisionService.class );

        expect( m_commandLine.getArguments() ).andReturn( null );
        expect( m_config.getProperty( "default.provision.url" ) ).andReturn( null );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService );
        run.installBundles( provisionService, null, context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService );
    }

}
