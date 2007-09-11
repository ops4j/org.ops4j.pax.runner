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
package org.ops4j.pax.runner.osgi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;
import org.apache.felix.framework.FilterImpl;
import org.ops4j.pax.runner.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class RunnerBundleContext
    extends UnsupportedBundleContext
{

    /**
     * Runner context to use.
     */
    private final Context m_context;
    /**
     * Bundle context related bundle.
     */
    private final Bundle m_bundle;

    /**
     * Creates a new bundle context.
     *
     * @param context the current runner context
     */
    public RunnerBundleContext( final Context context )
    {
        m_bundle = new RunnerBundle();
        m_context = context;
    }

    @Override
    public Filter createFilter( final String filter )
        throws InvalidSyntaxException
    {
        return new FilterImpl( filter );
    }

    @Override
    public void addServiceListener( final ServiceListener serviceListener, final String filter )
        throws InvalidSyntaxException
    {
        Filter osgiFilter = null;
        if( filter != null )
        {
            osgiFilter = createFilter( filter );
        }
        m_context.getEventDispatcher().addListener( m_bundle, ServiceListener.class, serviceListener, osgiFilter );
    }

    @Override
    public ServiceReference getServiceReference( final String clazz )
    {
        try
        {
            final ServiceReference[] references = getServiceReferences( clazz, null );
            if( references != null && references.length > 0 )
            {
                return references[ 0 ];
            }
            return null;
        }
        catch( InvalidSyntaxException ignore )
        {
            return null;
        }
    }

    @Override
    public ServiceReference[] getServiceReferences( final String clazz, final String filter )
        throws InvalidSyntaxException
    {
        Filter osgiFilter = null;
        if( filter != null )
        {
            osgiFilter = createFilter( filter );
        }
        //noinspection unchecked
        final List<ServiceReference> references =
            m_context.getServiceRegistry().getServiceReferences( clazz, osgiFilter );
        if( references != null )
        {
            return references.toArray( new ServiceReference[references.size()] );
        }
        else
        {
            return null;
        }
    }

    @Override
    public ServiceRegistration registerService( final String clazz, final Object service, final Dictionary properties )
    {
        return m_context.getServiceRegistry().registerService(
            new UnsupportedBundle(),
            new String[]{ clazz },
            service,
            properties
        );
    }

    @Override
    public Object getService( final ServiceReference serviceReference )
    {
        return m_context.getServiceRegistry().getService( m_bundle, serviceReference );
    }

    @Override
    public String getProperty( final String key )
    {
        return m_context.getOptionResolver().get( key );
    }

    @Override
    public Bundle installBundle( final String location )
        throws BundleException
    {
        final URL url;
        try
        {
            url = new URL( location );
        }
        catch( MalformedURLException e )
        {
            throw new BundleException( "Invalid location [" + location + "]" );
        }
        RunnerBundle bundle = new RunnerBundle( url );
        m_context.addBundle( bundle );
        return bundle;
    }

    @Override
    public Bundle getBundle()
    {
        return m_bundle;
    }


}
