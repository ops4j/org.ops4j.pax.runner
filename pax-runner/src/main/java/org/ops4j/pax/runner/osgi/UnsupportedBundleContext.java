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

import java.io.File;
import java.io.InputStream;
import java.util.Dictionary;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class UnsupportedBundleContext
    implements BundleContext
{

    public String getProperty( String string )
    {
        throw new UnsupportedOperationException();
    }

    public Bundle getBundle()
    {
        throw new UnsupportedOperationException();
    }

    public Bundle installBundle( String string )
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public Bundle installBundle( String string, InputStream inputStream )
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public Bundle getBundle( long l )
    {
        throw new UnsupportedOperationException();
    }

    public Bundle[] getBundles()
    {
        throw new UnsupportedOperationException();
    }

    public void addServiceListener( ServiceListener serviceListener, String string )
        throws InvalidSyntaxException
    {
        throw new UnsupportedOperationException();
    }

    public void addServiceListener( ServiceListener serviceListener )
    {
        throw new UnsupportedOperationException();
    }

    public void removeServiceListener( ServiceListener serviceListener )
    {
        throw new UnsupportedOperationException();
    }

    public void addBundleListener( BundleListener bundleListener )
    {
        throw new UnsupportedOperationException();
    }

    public void removeBundleListener( BundleListener bundleListener )
    {
        throw new UnsupportedOperationException();
    }

    public void addFrameworkListener( FrameworkListener frameworkListener )
    {
        throw new UnsupportedOperationException();
    }

    public void removeFrameworkListener( FrameworkListener frameworkListener )
    {
        throw new UnsupportedOperationException();
    }

    public ServiceRegistration registerService( String[] strings, Object object, Dictionary dictionary )
    {
        throw new UnsupportedOperationException();
    }

    public ServiceRegistration registerService( String string, Object object, Dictionary dictionary )
    {
        throw new UnsupportedOperationException();
    }

    public ServiceReference[] getServiceReferences( String string, String string1 )
        throws InvalidSyntaxException
    {
        throw new UnsupportedOperationException();
    }

    public ServiceReference[] getAllServiceReferences( String string, String string1 )
        throws InvalidSyntaxException
    {
        throw new UnsupportedOperationException();
    }

    public ServiceReference getServiceReference( String string )
    {
        throw new UnsupportedOperationException();
    }

    public Object getService( ServiceReference serviceReference )
    {
        throw new UnsupportedOperationException();
    }

    public boolean ungetService( ServiceReference serviceReference )
    {
        throw new UnsupportedOperationException();
    }

    public File getDataFile( String string )
    {
        throw new UnsupportedOperationException();
    }

    public Filter createFilter( String string )
        throws InvalidSyntaxException
    {
        throw new UnsupportedOperationException();
    }

}
