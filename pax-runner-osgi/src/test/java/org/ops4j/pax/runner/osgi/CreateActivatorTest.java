package org.ops4j.pax.runner.osgi;

import java.util.Properties;

import org.junit.Test;
import org.ops4j.pax.runner.osgi.felix.StandeloneFramework;
import org.ops4j.pax.runner.osgi.felix2.StandeloneFelix;
import org.osgi.framework.BundleException;

public class CreateActivatorTest {

	
	@Test
	public void createOneActivatorFelix1() throws BundleException {
		RunnerStandeloneFramework r = new RunnerStandeloneFramework(new Properties());
		
		StandeloneFelix framework = new StandeloneFelix();
		framework.start();
		
        r.installHandlers(r.getConfiguration(), r.getOptionResolver(), framework);
		
		framework.stop();
	}
	
	@Test
	public void createOneActivatorFelix2() throws BundleException {
		RunnerStandeloneFramework r = new RunnerStandeloneFramework(new Properties());
		
        StandeloneFramework framework = new StandeloneFramework(r.getOptionResolver(), new Properties());
		framework.start();
		
		r.installHandlers(r.getConfiguration(), r.getOptionResolver(), framework);
		
		framework.stop();
	}
}
