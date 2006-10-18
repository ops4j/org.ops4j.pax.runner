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
package org.ops4j.pax.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceManager
{

    private static final Logger m_logger = Logger.getLogger( ServiceManager.class.getName() );

    private static ServiceManager m_instance;
    private HashMap<String, String> m_services;
    private Map<String, Boolean> m_singletonTypes;
    private HashMap<String, Object> m_singletons;

    static
    {
        m_instance = new ServiceManager();
    }

    private ServiceManager()
    {
        m_singletonTypes = new HashMap<String, Boolean>();
        m_singletons = new HashMap<String, Object>();
        ClassLoader loader = getClass().getClassLoader();
        InputStream in = loader.getResourceAsStream( "META-INF/services/pax-runner.properties" );
        if( in == null )
        {
            throw new IllegalStateException( "Missing META-INF/services/pax-runner.properties." );
        }
        Properties props = new Properties();
        try
        {
            props.load( in );
        } catch( IOException e )
        {
            e.printStackTrace();
        }
        m_services = new HashMap<String, String>();
        Map<String, String> clone = new HashMap<String, String>();
        clone.putAll( (Map<? extends String, ? extends String>) props );
        for( Map.Entry<String, String> entry : clone.entrySet() )
        {
            String key = entry.getKey();
            String value = entry.getValue();
            int lastDotPos = key.lastIndexOf( '.' );
            String classname = key.substring( 0, lastDotPos );
            if( key.endsWith( ".class" ) )
            {
                m_services.put( classname, value );
            }
            if( key.endsWith( ".singleton" ) )
            {
                m_singletonTypes.put( classname, Boolean.valueOf( value ) );
            }
        }
    }

    public static ServiceManager getInstance()
    {
        return m_instance;
    }

    public synchronized <T> T getService( Class<T> serviceType )
    {
        String service = serviceType.getName();
        String classname = m_services.get( service );
        boolean useSingleton = m_singletonTypes.get( service );
        try
        {
            if( useSingleton )
            {
                T singleton = (T) m_singletons.get( service );
                if( singleton == null )
                {
                    singleton = instantiate( serviceType, classname );
                    m_singletons.put( service, singleton );
                }
                return singleton;
            }
            return instantiate( serviceType, classname );
        } catch( IllegalAccessException e )
        {
            Level error = Level.SEVERE;
            m_logger.log( error, "Invalid service type: " + serviceType, e );
            throw new ServiceException( "Invalid service type: " + serviceType, e );
        } catch( InstantiationException e )
        {
            Level error = Level.SEVERE;
            String message = "Invalid service type: " + serviceType;
            m_logger.log( error, message, e );
            throw new ServiceException( message, e );
        } catch( ClassNotFoundException e )
        {
            Level error = Level.SEVERE;
            String message = "No implementation class available for " + serviceType;
            m_logger.log( error, message, e );
            throw new ServiceException( message );
        }
    }

    private <T> T instantiate( Class<T> serviceType, String classname )
        throws IllegalAccessException, InstantiationException, ClassNotFoundException, ServiceException
    {
        Class<?> aClass = Class.forName( classname );
        if( serviceType.isAssignableFrom( aClass ) )
        {
            Class<? extends T> implClass = (Class<? extends T>) aClass;
            return implClass.newInstance();
        }
        else
        {
            String message = "Class '" + classname + "' does not implement '" + serviceType.getName() + "'.";
            throw new ServiceException( message );
        }
    }
}
