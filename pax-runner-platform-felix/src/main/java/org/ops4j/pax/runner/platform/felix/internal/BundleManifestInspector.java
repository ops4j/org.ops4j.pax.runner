package org.ops4j.pax.runner.platform.felix.internal;

import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.PlatformException;

public interface BundleManifestInspector {
    String getFragmentHost(BundleReference bundle) throws PlatformException;
}
