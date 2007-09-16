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
package org.ops4j.pax.runner.handler.classpath.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * OSGi URLStreamHandlerService implementation that handles classpath protocol.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.classpath.internal.Connection
 * @since August 07, 2007
 */
public class Handler
    extends AbstractURLStreamHandlerService
{

    /**
     * The current bundle context.
     */
    private final BundleContext m_bundleContext;

    /**
     * Creates a new Handler.
     *
     * @param bundleContext the bundle context
     */
    public Handler( final BundleContext bundleContext )
    {
        m_bundleContext = bundleContext;
    }

    /**
     * @see org.osgi.service.url.URLStreamHandlerService#openConnection(java.net.URL)
     */
    @Override
    public URLConnection openConnection( final URL url )
        throws IOException
    {
        return new Connection( url, m_bundleContext );
    }

}
