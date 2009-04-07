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
package org.ops4j.pax.runner.scanner.features.internal;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.servicemix.kernel.gshell.features.internal.FeatureImpl;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.scanner.features.ServiceConstants;

/**
 * Feature filter.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.18.0, March 07, 2009
 */
class FeaturesFilter
{

    /**
     * Feature name. Cannot be null or empty.
     */
    private final String m_name;
    /**
     * Feature version. Cannot be null or empty.
     */
    private final String m_version;

    /**
     * Constructor for default version.
     *
     * @param name feature name
     */
    FeaturesFilter( final String name )
    {
        this( name, null );
    }

    /**
     * Constructor.
     *
     * @param name    feature name; cannot be null or empty
     * @param version feature name; can be null or empty case when an default version will be used
     *
     * @throws IllegalArgumentException - if feature name is null or empty
     */
    FeaturesFilter( final String name,
                    final String version )
    {
        NullArgumentException.validateNotEmpty( name, true, "Feature name" );

        m_name = name;
        if( version == null || version.trim().length() == 0 )
        {
            m_version = FeatureImpl.DEFAULT_VERSION;
        }
        else
        {
            m_version = version;
        }
    }

    /**
     * Getter.
     *
     * @return feature name
     */
    String getName()
    {
        return m_name;
    }

    /**
     * Getter.
     *
     * @return feature version
     */
    String getVersion()
    {
        return m_version;
    }

    /**
     * Create a collection of features filters out of provision spec filter.
     *
     * @param provisionSpec provision spec
     *
     * @return collection of features filters
     *
     * @throws ScannerException - If there is no filter in provisioning spec (no !/)
     */
    static Collection<FeaturesFilter> fromProvisionSpec( final ProvisionSpec provisionSpec )
        throws ScannerException
    {
        final String rawFeatures = provisionSpec.getFilter();
        if( rawFeatures == null || rawFeatures.length() == 0 )
        {
            throw new ScannerException( "Feature names are mandatory (use !/)" );
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
