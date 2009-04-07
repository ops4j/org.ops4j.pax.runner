/*
 * Copyright 2009 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.scanner.feature.internal;

import org.apache.servicemix.kernel.gshell.features.Feature;
import org.apache.servicemix.kernel.gshell.features.FeaturesRegistry;
import org.apache.servicemix.kernel.gshell.features.Repository;
import org.apache.servicemix.kernel.gshell.features.internal.FeaturesServiceImpl;

/**
 * TODO Add JavaDoc.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.18.0, April 07, 2009
 */
class FeaturesServiceWrapper
    extends FeaturesServiceImpl
{

    /**
     * Constructor.
     * Initilaize features registry with a fake impl that does nothing. Usualy that is done by Spring vis DI.
     */
    FeaturesServiceWrapper()
    {
        setFeaturesServiceRegistry( new FakeFeaturesRegistry() );
    }

    /**
     * Override method to make it public (from protected).
     * {@inheritDoc}
     */
    @Override
    public Feature getFeature( final String name,
                               final String version )
        throws Exception
    {
        return super.getFeature( name, version );
    }

    /**
     * We do not need to save state.
     */
    @Override
    protected void saveState()
    {
        // do nothing (instead of saving state to preferences service
    }

    private static class FakeFeaturesRegistry
        implements FeaturesRegistry
    {

        public void register( final Feature feature )
        {
            // does nothing
        }

        public void unregister( Feature feature )
        {
            // does nothing
        }

        public void registerInstalled( Feature feature )
        {
            // does nothing
        }

        public void unregisterInstalled( Feature feature )
        {
            // does nothing
        }

        public void register( Repository repository )
        {
            // does nothing
        }

        public void unregister( Repository repository )
        {
            // does nothing
        }
    }

}
