package org.ops4j.pax.runner.osgi.felix2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.pax.url.mvn.internal.AetherBasedResolver;
import org.ops4j.pax.url.mvn.internal.Parser;
import org.ops4j.util.property.PropertyResolver;

public class MvnResolver extends AetherBasedResolver {

	public MvnResolver(PropertyResolver resolver) throws MalformedURLException {
		super(initConf(resolver, "org.ops4j.pax.url"));
	}

	private static MavenConfiguration initConf(PropertyResolver resolver, String pid) {
		MavenConfigurationImpl ret = new MavenConfigurationImpl(resolver, pid);
		ret.setSettings(new MavenSettingsImpl(ret.getSettingsFileUrl()));
		return ret;
	}
	
	public InputStream resolve(String url)
			throws IOException {
		int i = url.indexOf(":");
		url = url.substring(i+1);
		Parser p = new Parser(url);
		return super.resolve(p.getGroup(), p.getArtifact(), p.getClassifier(), 
				p.getType(), p.getVersion());
	}
	

}
