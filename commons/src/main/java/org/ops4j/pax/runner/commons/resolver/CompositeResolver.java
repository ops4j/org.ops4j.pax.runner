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
package org.ops4j.pax.runner.commons.resolver;

/**
 * Resolves properties by looking at the underlying resolvers. If a property is not found in one of them then it looks
 * the the next one.
 * TODO add unit tests
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class CompositeResolver
    implements Resolver
{

    /**
     * An array of resolvers to use to resolve properties.
     */
    private Resolver[] m_resolvers;

    /**
     * Creates a property resolver.
     *
     * @param resolvers rersolvers to use to resolve properties
     */
    public CompositeResolver( final Resolver... resolvers )
    {
        m_resolvers = resolvers;
    }

    /**
     * Resolves a property based on it's name by doing fallback on each of the resolvers.
     *
     * @param propertyName property name to be resolved
     *
     * @return value of property or null if property is not set or is empty.
     */
    public String get( final String propertyName )
    {
        String value;
        if( m_resolvers != null )
        {
            for( Resolver resolver : m_resolvers )
            {
                value = resolver.get( propertyName );
                if( value != null )
                {
                    if( value.trim().length() == 0 )
                    {
                        return null;
                    }
                    return value;
                }
            }
        }
        return null;
    }

}
