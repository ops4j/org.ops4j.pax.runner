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
package org.ops4j.pax.runner.provision.console.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.ScannerException;
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
        ServiceReference reference = bundleContext.getServiceReference( ProvisionService.class.getName() );
        if( reference != null )
        {
            ProvisionService service = (ProvisionService) bundleContext.getService( reference );
            if( service != null )
            {
                startConsole( service );
                bundleContext.ungetService( reference );
            }
        }
    }

    private void startConsole( ProvisionService service )
    {
        System.out.println();
        BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
        while( true )
        {
            try
            {
                System.out.print( "provision> " );
                String input = stdin.readLine();
                if( "exit".equalsIgnoreCase( input.trim() ) )
                {
                    return;
                }
                if( input.trim().length() > 0 )
                {
                    service.scan( input ).install();
                }
            }
            catch( MalformedSpecificationException e )
            {
                e.printStackTrace( System.out );
            }
            catch( IOException ignore )
            {
                return;
            }
            catch( ScannerException e )
            {
                e.printStackTrace( System.out );
            }
            catch( BundleException e )
            {
                e.printStackTrace( System.out );
            }
        }
    }

    public void stop( final BundleContext bundleContext )
    {
    }

}

