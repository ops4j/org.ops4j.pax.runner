/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.builder.services;

public class ServiceModel
{
    private boolean m_factoryPattern;
    private String m_serviceName;
    private String m_classname;
    private ServiceProperties m_serviceProps;

    public ServiceModel( String serviceName, String classname, ServiceProperties serviceProps, boolean useFactory )
    {
        m_factoryPattern = useFactory;
        m_serviceName = serviceName;
        m_classname = classname;
        m_serviceProps = serviceProps;
    }

    public void setServiceName( String serviceName )
    {
        m_serviceName = serviceName;
    }

    public String getClassname()
    {
        return m_classname;
    }

    public void setClassname( String classname )
    {
        m_classname = classname;
    }

    public ServiceProperties getServiceProps()
    {
        return m_serviceProps;
    }

    public void setServiceProps( ServiceProperties serviceProps )
    {
        m_serviceProps = serviceProps;
    }

    public String getServiceName()
    {
        return m_serviceName;
    }

    public boolean isFactoryPattern()
    {
        return m_factoryPattern;
    }

    public void setFactoryPattern( boolean factoryPattern )
    {
        m_factoryPattern = factoryPattern;
    }
}
