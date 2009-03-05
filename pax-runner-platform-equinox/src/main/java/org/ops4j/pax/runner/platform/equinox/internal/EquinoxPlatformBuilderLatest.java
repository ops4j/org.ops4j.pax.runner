/*
 * Copyright 2009 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.platform.equinox.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.osgi.framework.BundleContext;
import org.ops4j.net.URLUtils;

/**
 * Platform builder for equinox platform that uses the latest and greatest Equinox.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 03 05, 2009
 */
public class EquinoxPlatformBuilderLatest
    extends EquinoxPlatformBuilder
{

    /**
     * Create a new equinux platform builder.
     *
     * @param bundleContext a bundle context
     */
    public EquinoxPlatformBuilderLatest( final BundleContext bundleContext )
    {
        super( bundleContext, "LATEST" );
    }

    @Override
    public InputStream getDefinition()
        throws IOException
    {
        return
            URLUtils.prepareInputStream(
                new URL(
                    "https://scm.ops4j.org/repos/ops4j/laboratory/users/adreghiciu/pax/runner/"
                    + "platform-equinox/definition-LATEST.xml"
                ),
                true
            );
    }

}
