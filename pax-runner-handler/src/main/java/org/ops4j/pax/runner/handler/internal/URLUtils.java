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
package org.ops4j.pax.runner.handler.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities related to URL's.
 *
 * @author Alin Dreghiciu
 * @see java.net.URLStreamHandlerFactory
 * @since 0.5.6, December 13, 2007
 */
public class URLUtils
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( URLUtils.class );

    /**
     * Ment to be used via static methods.
     */
    private URLUtils()
    {
        // utility class
    }

    /**
     * Sets the URL stream handler factory even if the URLSStreamHandler factory is already set in the URL.
     * URL permits setting of the factory only once pe JVM. If the factory is already set it will set the field via
     * reflection and will keep the already set handler for delegation.
     *
     * @see java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)
     */
    public static void setURLStreamHandlerFactory( final URLStreamHandlerFactory urlStreamHandlerFactory )
    {
        try
        {
            URL.setURLStreamHandlerFactory( urlStreamHandlerFactory );
        }
        catch( Error err )
        {
            // usually we get here because the URLStreamHandlerFactory was already set and is only permited onec pe JVM
            // so, we will try to "still" the static field inside URL via reflection and install our handler factory
            // that will delegate to the original factory
            LOGGER.debug( "URLStreamHandlerFactory already set in the system. Replacing it with a composite" );
            synchronized( URL.class )
            {
                final URLStreamHandlerFactory currentFactory = resetURLStreamHandlerFactory();
                // ususally it should not be null as otherwise we shouldn't be here but then we try again
                if( currentFactory == null )
                {
                    URL.setURLStreamHandlerFactory( urlStreamHandlerFactory );
                }
                else if( currentFactory instanceof CompositeURLStreamHandlerFactory )
                {
                    URL.setURLStreamHandlerFactory( currentFactory );
                    ( (CompositeURLStreamHandlerFactory) currentFactory ).registerFactory( urlStreamHandlerFactory );
                }
                else
                {
                    URL.setURLStreamHandlerFactory(
                        new CompositeURLStreamHandlerFactory()
                            .registerFactory( urlStreamHandlerFactory )
                            .registerFactory( currentFactory )
                    );
                }
            }
        }
    }

    /**
     * Resets the current URLStreamHandlerFactory and returns the current factory.
     *
     * @return current factory
     */
    public static URLStreamHandlerFactory resetURLStreamHandlerFactory()
    {
        Field field = getURLStreamHandlerFactoryField();
        // no need for null check as the method above will throw an exception if not found
        field.setAccessible( true );
        try
        {
            // get the current factory
            final URLStreamHandlerFactory currentFactory = (URLStreamHandlerFactory) field.get( null );
            // reset the factory in URL
            field.set( null, null );
            return currentFactory;
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( "Cannot access URLStreamHandlerFactory field", e );
        }
    }

    /**
     * Return the current URLStreamHandlerFactory.
     *
     * @return current factory
     */
    public static URLStreamHandlerFactory getURLStreamHandlerFactory()
    {
        Field field = getURLStreamHandlerFactoryField();
        // no need for null check as the method above will throw an exception if not found
        field.setAccessible( true );
        try
        {
            return (URLStreamHandlerFactory) field.get( null );
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( "Cannot access URLStreamHandlerFactory field", e );
        }
    }

    /**
     * Return the current URLStreamHandlerFactory reflection field.
     *
     * @return current factory field
     */
    private static Field getURLStreamHandlerFactoryField()
    {
        final Field[] fields = URL.class.getDeclaredFields();
        if( fields != null )
        {
            for( Field field : fields )
            {
                if( Modifier.isStatic( field.getModifiers() ) && field.getType()
                    .equals( URLStreamHandlerFactory.class ) )
                {
                    return field;
                }
            }
        }
        throw new RuntimeException( "Caanot find URLStreamHandlerFactory field " );
    }

}