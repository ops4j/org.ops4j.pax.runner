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
package org.ops4j.pax.runner.platform.felix.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.util.collections.PropertiesWriter;

/**
 * Platform builder for felix platform before 1.4.0.
 *
 * @author Alin Dreghiciu
 * @since 0.16.0, November 05, 2008
 */
public class FelixPlatformBuilderF140
    extends AbstractFelixPlatformBuilder
{

    /**
     * Create a new felix platform builder.
     *
     * @param bundleContext a bundle context
     * @param version       supported version
     */
    public FelixPlatformBuilderF140( final BundleContext bundleContext, final String version )
    {
        super( bundleContext, version );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getStartLevelProperty()
    {
        return "org.osgi.framework.startlevel";
    }

}