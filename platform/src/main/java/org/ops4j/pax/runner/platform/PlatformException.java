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
package org.ops4j.pax.runner.platform;

/**
 * Thrown to indicate an exception during running of the platform.
 *
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public class PlatformException
    extends Exception
{

    /**
     * @param message The exception message.
     * @see Exception#Exception(String)
     */
    public PlatformException( String message )
    {
        super( message );
    }

    /**
     * @param message The exception message.
     * @param cause The original cause of this exception.
     * @see Exception#Exception(String,Throwable)
     */
    public PlatformException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
