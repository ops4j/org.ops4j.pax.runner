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

import java.util.ArrayList;
import java.util.Collection;
import org.apache.servicemix.kernel.gshell.features.internal.FeatureImpl;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.scanner.feature.ServiceConstants;

/**
 * Feature filter.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.18.0, March 07, 2009
 */
class FeaturesFilter
{

    private final String m_name;
    private final String m_version;

    FeaturesFilter( final String name )
    {
        this( name, null );
    }

    FeaturesFilter( final String name,
                   final String version )
    {
        m_name = name;
        if( version == null )
        {
            m_version = FeatureImpl.DEFAULT_VERSION;
        }
        else
        {
            m_version = version;
        }
    }

    String getName()
    {
        return m_name;
    }

    String getVersion()
    {
        return m_version;
    }

    static Collection<FeaturesFilter> fromProvisionSpec( final ProvisionSpec provisionSpec )
        throws ScannerException
    {
        final String rawFeatures = provisionSpec.getFilter();
        if( rawFeatures == null || rawFeatures.length() == 0 )
        {
            throw new ScannerException( "Feature name is mandatory" );
        }
        final Collection<FeaturesFilter> filters = new ArrayList<FeaturesFilter>();
        final String[] features = rawFeatures.split( ServiceConstants.FEATURE_SEPARATOR );
        for( String feature : features )
        {
            final String[] segments = feature.split( "/" );
            if( segments.length > 1 )
            {
                filters.add( new FeaturesFilter( segments[ 0 ], segments[ 1 ] ) );
            }
            else
            {
                filters.add( new FeaturesFilter( segments[ 0 ] ) );
            }
        }
        return filters;
    }

}
