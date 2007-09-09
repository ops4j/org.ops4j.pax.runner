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
package org.ops4j.pax.runner.commons;

/**
 * Assertions utility.
 *
 * @author Alin Dreghiciu
 * @since August 10, 2007
 */
public class Assert
{

    /**
     * Utility class. ment to be used via static methods.
     */
    private Assert()
    {
        // utility class
    }

    /**
     * Validates that the passed value is not null. If null will throw IllegalArgumentException.
     *
     * @param label argument label to be used to construct the exception message
     * @param value value to be validated
     */
    public static void notNull( final String label, final Object value )
    {
        if ( value == null )
        {
            throw new IllegalArgumentException( label + " cannot be null" );
        }
    }

    /**
     * Validates that the passed string is not empty. If empty will throw IllegalArgumentException.
     * First it validates that is snot null.
     *
     * @param label argument label to be used to construct the exception message
     * @param value value to be validated
     */
    public static void notEmpty( final String label, final String value )
    {
        notNull( label, value );
        if ( value.trim().length() == 0 )
        {
            throw new IllegalArgumentException( label + " cannot be empty" );
        }
    }

    /**
     * Validates that the passed string array is:
     * - not null
     * - not empty
     * - all elemenst are not null
     * - all elemsn are not empty.
     * If empty will throw IllegalArgumentException.
     *
     * @param label argument label to be used to construct the exception message
     * @param value value to be validated
     */
    public static void notEmpty( final String label, final String[] value )
    {
        notNull( label, value );
        if ( value.length == 0 )
        {
            throw new IllegalArgumentException( label + " cannot be empty" );
        }
        for ( String element : value )
        {
            notEmpty( label + " element", element );
        }
    }

}
