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
package org.ops4j.pax.runner.provision;

import java.net.MalformedURLException;

/**
 * Thrown to indicate that a malformed specification was used. Either no legal scheme could be found in a specification
 * string or the string could not be parsed.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public class MalformedSpecificationException
    extends MalformedURLException
{

    /**
     * @param message The exception message.
     *
     * @see Exception#Exception(String)
     */
    public MalformedSpecificationException( final String message )
    {
        super( message );
    }

    /**
     * @param message The exception message.
     * @param cause   The original cause of the exception.
     *
     * @see Exception#Exception(String,Throwable)
     */
    public MalformedSpecificationException( final String message, Throwable cause )
    {
        super( message );
        initCause( cause );
    }

    /**
     * To simulate a malformed url exception.
     *
     * @param cause the cause
     */
    public MalformedSpecificationException( final MalformedURLException cause )
    {
        this( cause.getMessage(), cause );

    }

}
