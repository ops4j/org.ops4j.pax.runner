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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleContext;

public class UnsupportedBundle
    implements Bundle
{

    public int getState()
    {
        throw new UnsupportedOperationException();
    }

    public void start()
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public void stop()
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public void update()
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public void update( InputStream inputStream )
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public void uninstall()
        throws BundleException
    {
        throw new UnsupportedOperationException();
    }

    public Dictionary getHeaders()
    {
        throw new UnsupportedOperationException();
    }

    public long getBundleId()
    {
        throw new UnsupportedOperationException();
    }

    public String getLocation()
    {
        throw new UnsupportedOperationException();
    }

    public ServiceReference[] getRegisteredServices()
    {
        throw new UnsupportedOperationException();
    }

    public ServiceReference[] getServicesInUse()
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission( Object object )
    {
        throw new UnsupportedOperationException();
    }

    public URL getResource( String string )
    {
        throw new UnsupportedOperationException();
    }

    public Dictionary getHeaders( String string )
    {
        throw new UnsupportedOperationException();
    }

    public String getSymbolicName()
    {
        throw new UnsupportedOperationException();
    }

    public Class loadClass( String string )
        throws ClassNotFoundException
    {
        throw new UnsupportedOperationException();
    }

    public Enumeration getResources( String string )
        throws IOException
    {
        throw new UnsupportedOperationException();
    }

    public Enumeration getEntryPaths( String string )
    {
        throw new UnsupportedOperationException();
    }

    public URL getEntry( String string )
    {
        throw new UnsupportedOperationException();
    }

    public long getLastModified()
    {
        throw new UnsupportedOperationException();
    }

    public Enumeration findEntries( String string, String string1, boolean b )
    {
        throw new UnsupportedOperationException();
    }

    public BundleContext getBundleContext()
    {
        throw new UnsupportedOperationException();
    }
}
