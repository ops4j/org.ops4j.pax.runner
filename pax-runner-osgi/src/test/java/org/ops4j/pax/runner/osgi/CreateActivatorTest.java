package org.ops4j.pax.runner.osgi;

import java.util.Properties;

import org.junit.Test;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Configuration;
import org.ops4j.pax.runner.ConfigurationImpl;
import org.ops4j.pax.runner.OptionResolverImpl;
import org.ops4j.pax.runner.osgi.felix.StandeloneFramework;
import org.ops4j.pax.runner.osgi.felix2.StandeloneFelix;
import org.osgi.framework.BundleException;

public class CreateActivatorTest {

	
	@Test
	public void createOneActivatorFelix1() throws BundleException {
		StandeloneFelix framework = new StandeloneFelix();
		framework.start();
		RunnerStandeloneFramework r = new RunnerStandeloneFramework();
		final CommandLine commandLine = new CommandLineImpl();
        
        final Configuration configuration = new ConfigurationImpl(new Properties(), 
        		"classpath:META-INF/runner.properties");
       
        OptionResolverImpl optionResolver = new OptionResolverImpl( commandLine, configuration );
        
       
		r.installHandlers(configuration, optionResolver, framework);
		
		framework.stop();
	}
	
	@Test
	public void createOneActivatorFelix2() throws BundleException {
		RunnerStandeloneFramework r = new RunnerStandeloneFramework();
		final CommandLine commandLine = new CommandLineImpl();
        
        final Configuration configuration = new ConfigurationImpl(new Properties(), 
        		"classpath:META-INF/runner.properties");
       
        OptionResolverImpl optionResolver = new OptionResolverImpl( commandLine, configuration );
       
        StandeloneFramework framework = new StandeloneFramework(optionResolver, new Properties());
		framework.start();
		 
       
		r.installHandlers(configuration, optionResolver, framework);
		
		framework.stop();
	}
}
