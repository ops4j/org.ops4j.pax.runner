package org.ops4j.pax.runner.platform.felix.internal;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.PlatformException;
import org.osgi.framework.Constants;

/**
 *  Utility class to inspect bundle manifests.
 */
public class BundleManifestInspectorImpl implements BundleManifestInspector {
    /**
     * Default constructor.
     */
    public BundleManifestInspectorImpl(){
    }

    @Override
    public String getFragmentHost(BundleReference bundle) throws PlatformException {
        return this.getAttributeValue(bundle, Constants.FRAGMENT_HOST);
    }

    private String getAttributeValue(BundleReference bundle, String attributeName) throws PlatformException {
        Manifest manifest = this.getBundleManifest(bundle);
        return manifest.getMainAttributes().getValue(new Attributes.Name(attributeName));
    }

    /**
     * Returns the manifest file of the bundle specified by the given reference.
     *
     * @param bundle BundleReference referencing the bundle to be inspected.
     * @return Manifest referencing the manifest extracted from the specified bundle.
     * @throws PlatformException Thrown when the specified bundle is not valid.
     */
    private Manifest getBundleManifest(BundleReference bundle) throws PlatformException {
        URL url = bundle.getURL();
        try {
            JarFile jar = new JarFile(url.getFile(), false);
            return jar.getManifest();
        } catch (IOException e) {
            throw new PlatformException("[" + url + "] is not a valid bundle", e);
        }
    }

}
