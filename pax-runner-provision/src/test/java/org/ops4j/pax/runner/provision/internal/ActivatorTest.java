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
package org.ops4j.pax.runner.provision.internal;

import java.util.Dictionary;
import static org.easymock.EasyMock.*;
import org.junit.Test;
import org.ops4j.pax.runner.provision.ProvisionService;
import org.ops4j.pax.runner.provision.Scanner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceListener;
import org.osgi.service.startlevel.StartLevel;

public class ActivatorTest
{

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullBundleContext()
        throws Exception
    {
        new Activator().start( null );
    }

    @Test
    public void start()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        expect( context.registerService(
            eq( ProvisionService.class.getName() ),
            isA( ProvisionServiceImpl.class ),
            (Dictionary) isNull()
        )
        ).andReturn( null );
        // service tracker for start level service
        expect( context.createFilter( "(objectClass=" + StartLevel.class.getName() + ")" )
        ).andReturn( createMock( Filter.class ) );
        context.addServiceListener(
            (ServiceListener) notNull(),
            eq( "(objectClass=" + StartLevel.class.getName() + ")" )
        );
        expect( context.getServiceReferences(
            eq( StartLevel.class.getName() ),
            (String) isNull()
        )
        ).andReturn( null );        
        // service tracker for scanners
        expect( context.createFilter( "(objectClass=" + Scanner.class.getName() + ")" )
        ).andReturn( createMock( Filter.class ) );
        context.addServiceListener(
            (ServiceListener) notNull(),
            eq( "(objectClass=" + Scanner.class.getName() + ")" )
        );
        expect( context.getServiceReferences(
            eq( Scanner.class.getName() ),
            (String) isNull()
        )
        ).andReturn( null );

        replay( context );
        new Activator().start( context );
        verify( context );
    }

    @Test( expected = IllegalArgumentException.class )
    public void stopWithNullBundleContext()
        throws Exception
    {
        new Activator().stop( null );
    }

    @Test
    public void stop()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        replay( context );
        new Activator().stop( context );
        verify( context );
    }

}
