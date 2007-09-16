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

import java.util.Dictionary;
import java.util.Hashtable;
import org.ops4j.pax.runner.commons.Assert;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Bundle activator for classpath: protocol handler.
 *
 * @author Alin Dreghiciu
 * @since August 07, 2007
 */
public final class Activator
    implements BundleActivator
{

    /**
     * Registers Handler as a classpath: protocol stream handler service.
     *
     * @param bc the bundle context.
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start( final BundleContext bc )
        throws Exception
    {
        Assert.notNull( "Bundle context", bc );
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put( URLConstants.URL_HANDLER_PROTOCOL, new String[]{ Connection.PROTOCOL } );
        bc.registerService( URLStreamHandlerService.class.getName(), new Handler( bc ), props );
    }

    /**
     * Does nothing.
     *
     * @param bc the bundle context
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop( final BundleContext bc )
        throws Exception
    {
        Assert.notNull( "Bundle context", bc );
    }

}
