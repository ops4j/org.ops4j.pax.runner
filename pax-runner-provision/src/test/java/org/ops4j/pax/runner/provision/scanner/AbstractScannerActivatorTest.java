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
package org.ops4j.pax.runner.provision.scanner;

import java.util.Dictionary;
import static org.easymock.EasyMock.*;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.util.property.PropertyResolver;

public class AbstractScannerActivatorTest
{

    @Test( expected = IllegalArgumentException.class )
    public void startWithNullBundleContext()
        throws Exception
    {
        new TestActivator().start( null );
    }

    @Test
    public void start()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        expect( context.registerService(
            eq( Scanner.class.getName() ),
            isA( Scanner.class ),
            (Dictionary) notNull()
        )
        ).andReturn( null );
        expect( context.registerService(
            eq( ManagedService.class.getName() ),
            notNull(),
            (Dictionary) notNull()
        )
        ).andReturn( null );
        replay( context );
        new TestActivator().start( context );
        verify( context );
    }

    @Test( expected = IllegalArgumentException.class )
    public void stopWithNullBundleContext()
        throws Exception
    {
        new TestActivator().stop( null );
    }

    @Test
    public void stop()
        throws Exception
    {
        BundleContext context = createMock( BundleContext.class );
        replay( context );
        new TestActivator().stop( context );
        verify( context );
    }

    private static class TestActivator extends AbstractScannerActivator<Scanner>
    {

        @Override
        protected Scanner createScanner( BundleContext bundleContext )
        {
            return createMock( Scanner.class );
        }

        @Override
        protected String getPID()
        {
            return "myPID";
        }

        @Override
        protected String getSchema()
        {
            return "scan-me";
        }

        @Override
        protected void setResolver( PropertyResolver propertyResolver )
        {
            // do nothing
        }
    }

}
