/*
 * Copyright 2004 Niclas Hedhman
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.common;

import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Utility class that handles substitution of property names in the string
 * for ${value} relative to a supplied set of properties.
 *
 * @author <a href="http://www.ops4j.org">Open Participation Software for Java</a>
 * @version $Id: PropertyResolver.java 3785 2006-03-26 06:23:52Z niclas@hedhman.org $
 */
public final class PropertyResolver
{

    /**
     * Symbol substitution from properties.
     * Replace any occurances of ${[key]} with the value of the property
     * assigned to the [key] in the supplied properties argument.
     *
     * @param props the source properties from which substitution is resolved
     * @param value a string containing possibly multiple ${[value]} sequences
     *
     * @return the expanded string
     */
    public static String resolve( Properties props, String value )
    {
        if( value == null )
        {
            return null;
        }

        // optimization for common case.
        if( value.indexOf( '$' ) < 0 )
        {
            return value;
        }
        int pos1 = value.indexOf( "${" );
        if( pos1 < 0 )
        {
            return value;
        }

        Stack<String> stack = new Stack<String>();
        StringTokenizer st = new StringTokenizer( value, "${}", true );

        while( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            if( "}".equals( token ) )
            {
                String name = stack.pop();
                String open = stack.pop();
                if( "${".equals( open ) )
                {
                    String propValue = System.getProperty( name );
                    if( propValue == null )
                    {
                        propValue = props.getProperty( name );
                    }
                    if( propValue == null )
                    {
                        push( stack, "${" + name + "}" );
                    }
                    else
                    {
                        push( stack, propValue );
                    }
                }
                else
                {
                    push( stack, "${" + name + "}" );
                }
            }
            else
            {
                if( "$".equals( token ) )
                {
                    stack.push( "$" );
                }
                else
                {
                    push( stack, token );
                }
            }
        }
        String result = "";
        while( stack.size() > 0 )
        {
            result = stack.pop() + result;
        }
        return result;
    }

    /**
     * Pushes a value on a stack
     *
     * @param stack the stack
     * @param value the value
     */
    private static void push( Stack<String> stack, String value )
    {
        if( stack.size() > 0 )
        {
            String data = stack.pop();
            if( "${".equals( data ) )
            {
                stack.push( data );
                stack.push( value );
            }
            else
            {
                stack.push( data + value );
            }
        }
        else
        {
            stack.push( value );
        }
    }

    /**
     * Null constructor.
     */
    private PropertyResolver()
    {
    }
}

