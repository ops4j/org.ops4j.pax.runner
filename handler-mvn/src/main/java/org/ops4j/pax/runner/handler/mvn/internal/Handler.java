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
package org.ops4j.pax.runner.handler.mvn.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * OSGi URLStreamHandlerService implementation that handles mvn protocol.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.mvn.internal.Connection
 * @since August 10, 2007
 */
public class Handler
    extends AbstractURLStreamHandlerService
{

    /**
     * Property resolver to be used on resolving properties.
     */
    private Resolver m_resolver;

    /**
     * @see org.osgi.service.url.URLStreamHandlerService#openConnection(java.net.URL)
     */
    @Override
    public URLConnection openConnection( final URL url )
        throws IOException
    {
        ConfigurationImpl sc = new ConfigurationImpl( m_resolver );
        sc.setSettings( new SettingsImpl( sc.getSettings() ) );
        return new Connection( url, sc );
    }

    /**
     * Sets the resolver to use.
     *
     * @param resolver a resolver
     */
    public void setResolver( final Resolver resolver )
    {
        m_resolver = resolver;
    }

}
