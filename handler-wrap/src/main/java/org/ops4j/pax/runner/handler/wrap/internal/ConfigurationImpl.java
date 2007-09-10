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
package org.ops4j.pax.runner.handler.wrap.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.ConfigurationMap;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.handler.wrap.ServiceConstants;

/**
 * Service Configuration implementation.
 *
 * @author Alin Dreghiciu
 * @see Configuration
 * @since September 09, 2007
 */
public class ConfigurationImpl
    extends ConfigurationMap
    implements Configuration
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ConfigurationImpl.class );

    /**
     * Property resolver. Cannot be null.
     */
    private final Resolver m_resolver;

    /**
     * Creates a new service configuration.
     *
     * @param resolver resolver used to resolve properties; mandatory
     */
    public ConfigurationImpl( final Resolver resolver )
    {
        Assert.notNull( "Property resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * @see Configuration#getCertificateCheck()
     */
    public Boolean getCertificateCheck()
    {
        if ( !contains( ServiceConstants.PROPERTY_CERTIFICATE_CHECK ) )
        {
            return set( ServiceConstants.PROPERTY_CERTIFICATE_CHECK,
                        Boolean.valueOf( m_resolver.get( ServiceConstants.PROPERTY_CERTIFICATE_CHECK ) )
            );
        }
        return get( ServiceConstants.PROPERTY_CERTIFICATE_CHECK );
    }

}
