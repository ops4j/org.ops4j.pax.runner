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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.util.EventDispatcher;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import static org.ops4j.pax.runner.CommandLine.*;

import org.ops4j.pax.runner.osgi.CreateActivator;
import org.ops4j.pax.runner.osgi.RunnerStandeloneFramework;
import org.ops4j.pax.runner.osgi.felix.Context;
import org.ops4j.pax.runner.osgi.felix.ContextImpl;
import org.ops4j.pax.runner.osgi.felix.RunnerBundle;
import org.ops4j.pax.runner.osgi.felix.StandeloneFramework;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.platform.SystemFileReference;
import org.ops4j.pax.scanner.InstallableBundles;
import org.ops4j.pax.scanner.MalformedSpecificationException;
import org.ops4j.pax.scanner.ProvisionService;
import org.ops4j.pax.scanner.ScannedBundle;
import org.ops4j.pax.scanner.ScannerException;
import org.ops4j.pax.scanner.UnsupportedSchemaException;

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
    public void startWithNullCommandLine() throws BundleException
    {
        new Run().start( null, createMock( Configuration.class ), createMock( OptionResolver.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullConfiguration() throws BundleException
    {
        new Run().start( createMock( CommandLine.class ), null, createMock( OptionResolver.class ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullResolver() throws BundleException
    {
        new Run().start( createMock( CommandLine.class ), createMock( Configuration.class ), null );
    }

    // test runner flow
    @Test
    public void startFlow() throws BundleException
    {
    	m_recorder.record( "startOsgiFrammework()" );
    	m_recorder.record( "initOsgiFramework()" );
        m_recorder.record( "cleanup()" );
        m_recorder.record( "installServices()" );
        m_recorder.record( "installHandlers()" );
        m_recorder.record( "installScanners()" );
        m_recorder.record( "installBundles()" );
        m_recorder.record( "createJavaRunner()" );
        m_recorder.record( "installPlatform()" );
        m_recorder.record( "determineSystemFiles()" );
        replay( m_commandLine, m_config, m_recorder, m_resolver, m_bundleContext );
        final Context context = new ContextImpl();
		
		final RunnerStandeloneFramework r = new RunnerStandeloneFramework(m_commandLine, m_config, m_resolver) {
			@Override
			public void installHandlers(Configuration config,
					OptionResolver optionResolver, CreateActivator ca) {
				 m_recorder.record( "installHandlers()" );
			}
			@Override
			public ProvisionService installScanners(Configuration config,
					OptionResolver optionResolver, CreateActivator ca) {
				m_recorder.record( "installScanners()" );
				return null;
			}
			@Override
			public void installServices(Configuration config,
					OptionResolver optionResolver, CreateActivator ca) {
				 m_recorder.record( "installServices()" );
			}
			@Override
			public void cleanup(OptionResolver resolver) {
				 m_recorder.record( "cleanup()" );
			}
		};
        new Run()
        {
        	Context startOsgiFrammework(CommandLine commandLine, Configuration config, OptionResolver resolver) throws BundleException {
        		m_recorder.record( "startOsgiFrammework()" );
        		return context;
        	};
        	
        	org.ops4j.pax.runner.osgi.RunnerStandeloneFramework initOsgiFramework(CommandLine commandLine, Configuration config, OptionResolver resolver) {
        		m_recorder.record( "initOsgiFramework()" );
        		return r;
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
            JavaRunner createJavaRunner( final OptionResolver resolver )
            {
                m_recorder.record( "createJavaRunner()" );
                return null;
            }

            @Override
            List<SystemFileReference> determineSystemFiles( Context context )
            {
                m_recorder.record( "determineSystemFiles()" );
                return Collections.emptyList();
            }
        }.start( m_commandLine, m_config, m_resolver, null );
        verify( m_commandLine, m_config, m_recorder, m_resolver, m_bundleContext );
    }

    

    // test that we getOption a runtime exception not a NullPointerException
    @Test( expected = RuntimeException.class )
    public void installBundlesWithNullProvisionService() throws BundleException
    {
        Run run = new Run();
        Context context = createContext( m_commandLine, m_config, m_resolver );

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
        Context context = createContext( m_commandLine, m_config, m_resolver );

        ProvisionService provisionService = createMock( ProvisionService.class );
        InstallableBundles installables = createMock( InstallableBundles.class );
        List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();

        expect( m_resolver.get( OPTION_PROFILES ) ).andReturn( null );
        List<String> args = new ArrayList<String>();
        args.add( "scan-file:file:bundles1.txt" );
        args.add( "scan-file:file:bundles2.txt" );
        expect( m_commandLine.getArguments() ).andReturn( args );
        expect( provisionService.scan( "scan-file:file:bundles1.txt" ) ).andReturn( scannedBundles );
        expect( provisionService.wrap( scannedBundles ) ).andReturn( installables );
        expect( installables.install() ).andReturn( installables );
        expect( provisionService.scan( "scan-file:file:bundles2.txt" ) ).andReturn( scannedBundles );
        expect( provisionService.wrap( scannedBundles ) ).andReturn( installables );
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
        Context context = createContext( m_commandLine, m_config, m_resolver );

        ProvisionService provisionService = createMock( ProvisionService.class );
        InstallableBundles installables = createMock( InstallableBundles.class );
        List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
        ProvisionSchemaResolver schemaResolver = createMock( ProvisionSchemaResolver.class );

        expect( m_resolver.get( OPTION_PROFILES ) ).andReturn( null );
        List<String> args = new ArrayList<String>();
        args.add( "bundles.txt" );
        expect( m_commandLine.getArguments() ).andReturn( args );
        expect( provisionService.scan( "bundles.txt" ) ).andThrow( new UnsupportedSchemaException( "test" ) );
        expect( schemaResolver.resolve( "bundles.txt" ) ).andReturn( "scan-file:file:bundles.txt" );
        expect( provisionService.scan( "scan-file:file:bundles.txt" ) ).andReturn( scannedBundles );
        expect( provisionService.wrap( scannedBundles ) ).andReturn( installables );
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
    public void installBundlesWithNoArgumentsAndNoDefault() throws BundleException
    {
        Run run = new Run();
        Context context = createContext( m_commandLine, m_config, m_resolver );

        ProvisionService provisionService = createMock( ProvisionService.class );

        expect( m_commandLine.getArguments() ).andReturn( null );
        expect( m_config.getProperty( "default.provision.url" ) ).andReturn( null );

        replay( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService );
        run.installBundles( provisionService, null, context );
        verify( m_commandLine, m_config, m_resolver, m_recorder, m_bundleContext, provisionService );
    }

	private Context createContext(CommandLine m_commandLine2,
			Configuration m_config2, OptionResolver optionResolver) throws BundleException {
		StandeloneFramework s = new StandeloneFramework(optionResolver);
		s.start();
		return s.getContext().setCommandLine(m_commandLine2).setConfiguration(m_config2);
	}

}
