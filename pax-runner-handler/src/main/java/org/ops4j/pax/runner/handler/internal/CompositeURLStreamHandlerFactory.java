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
package org.ops4j.pax.runner.handler.internal;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import org.ops4j.lang.NullArgumentException;

/**
 * An composite URLStreamHandlerFactory that used it's internal list of registred URLStreamHandlerFactories to find out
 * if they can handle the requestd protocol. First one that canhandle (does not return null) will be returned.
 *
 * @author Alin Dreghiciu
 * @see java.net.URLStreamHandlerFactory
 * @since 0.5.6, December 13, 2007
 */
public class CompositeURLStreamHandlerFactory
    implements URLStreamHandlerFactory
{

    /**
     * List of URLStreamHandlerFactories to delegate to.
     */
    private final List<URLStreamHandlerFactory> m_factories;

    /**
     * Creates a new composite url stream handler factory.
     */
    public CompositeURLStreamHandlerFactory()
    {
        m_factories = new ArrayList<URLStreamHandlerFactory>();
    }

    /**
     * Searches the registered factories for one that can handle the protocol. First that can (does not return null)
     * will be returned.
     *
     * @see java.net.URLStreamHandlerFactory
     */
    public URLStreamHandler createURLStreamHandler( final String protocol )
    {
        for( URLStreamHandlerFactory factory : m_factories )
        {
            final URLStreamHandler handler = factory.createURLStreamHandler( protocol );
            if( handler != null )
            {
                return handler;
            }
        }
        return null;
    }

    /**
     * Registeres a factory with the composite.
     *
     * @param factory to be registered. Cannot be null.
     *
     * @return self, for fluent api use
     */
    public CompositeURLStreamHandlerFactory registerFactory( final URLStreamHandlerFactory factory )
    {
        NullArgumentException.validateNotNull( factory, "Registered factory" );
        m_factories.add( factory );
        return this;
    }

    /**
     * Unregisteres a factory with the composite. If the factory was not registered before the method will return
     * silently.
     *
     * @param factory to be unregistered. Cannot be null.
     *
     * @return self, for fluent api use
     */
    public CompositeURLStreamHandlerFactory unregisterFactory( final URLStreamHandlerFactory factory )
    {
        NullArgumentException.validateNotNull( factory, "Unregistered factory" );
        m_factories.remove( factory );
        return this;
    }

}
