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
package org.ops4j.pax.runner.internal;

import org.ops4j.pax.runner.ResourceManager;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class ResourceManagerImpl
    implements ResourceManager
{
    private static final Logger m_logger = Logger.getLogger( ResourceManager.class.getName() );

    public Properties getProperties( InputStream in )
    {
        if( !( in instanceof BufferedInputStream ) )
        {
            in = new BufferedInputStream( in );
        }
        Properties props = new Properties();
        try
        {
            props.load( in );
        } catch( IOException e )
        {
            Level error = Level.SEVERE;
            m_logger.log( error, "Can not load properties.", e );
        }
        return props;
    }
}
