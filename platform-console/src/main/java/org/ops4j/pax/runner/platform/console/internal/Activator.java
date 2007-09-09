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
package org.ops4j.pax.runner.platform.console.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.BundleReferenceBean;
import org.ops4j.pax.runner.platform.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public final class Activator
    implements BundleActivator
{

    public void start( final BundleContext bundleContext )
        throws BundleException
    {
        ServiceReference reference = bundleContext.getServiceReference( Platform.class.getName() );
        if ( reference != null )
        {
            Platform service = (Platform) bundleContext.getService( reference );
            if ( service != null )
            {
                startConsole( service );
                bundleContext.ungetService( reference );
            }
        }
    }

    private void startConsole( Platform service )
    {
        System.out.println();
        BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true )
        {
            try
            {
                System.out.print( "platform> " );
                String input = stdin.readLine().trim();
                if ( "exit".equalsIgnoreCase( input ) )
                {
                    return;
                }
                if ( input.length() > 0 )
                {
                    List<BundleReference> bundles = null;
                    if ( !"none".equalsIgnoreCase( input ) )
                    {
                        final String[] urls = input.split( "," );
                        bundles = new ArrayList<BundleReference>();
                        for ( String url : urls )
                        {
                            bundles.add( new BundleReferenceBean( new URL( url ) ) );
                        }
                    }
                    service.start( bundles, null, null );
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace( System.out );
            }
        }
    }

    public void stop( final BundleContext bundleContext )
    {
    }

}

