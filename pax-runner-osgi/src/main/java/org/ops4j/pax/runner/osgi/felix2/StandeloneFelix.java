package org.ops4j.pax.runner.osgi.felix2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.ops4j.pax.runner.osgi.CreateActivator;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class StandeloneFelix extends Felix implements CreateActivator {
	MvnResolver mvnResolver;
	
	public StandeloneFelix() {
		super(defaultConfiguration());
	}
	
	private static Map defaultConfiguration() {
		return null;
	}

	public StandeloneFelix(Map configMap) {
		super(configMap);
	}

	public Bundle installBundle(String url) throws BundleException {
		Bundle b = getBundleContext().installBundle(url, getInptutStream(url));
		return b;
	}
	
	public Bundle startBundle(String url) throws BundleException {
		Bundle b = installBundle(url);
		b.start();
		return b;
	}
	
	 private InputStream getInptutStream(String url) throws BundleException {
		try {
			URL urlObj = new URL(url);
			return urlObj.openStream();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		try {
			return getMvnResolver().resolve(url);
		} catch (IOException e) {
			throw new BundleException("Cannot open url: "+url,e);
		}
	}

	

	private MvnResolver getMvnResolver() throws BundleException {
		if (mvnResolver == null) {
			PropertyResolver resolver= new BundleContextPropertyResolver(getBundleContext());
			try {
				mvnResolver = new MvnResolver(resolver);
			} catch (MalformedURLException e) {
				throw new BundleException("Cannot initialize mvn resolver",e);
			}
		}
		return mvnResolver;
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
    public BundleContext createActivator( final String bundleName, final String activatorClazz )
    {
        try
        {
            final BundleActivator activator = (BundleActivator) Class.forName( activatorClazz ).newInstance();
            activator.start( super.getBundleContext() );
            return super.getBundleContext();
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Could not create [" + bundleName + "]", e );
        }
    }

}
