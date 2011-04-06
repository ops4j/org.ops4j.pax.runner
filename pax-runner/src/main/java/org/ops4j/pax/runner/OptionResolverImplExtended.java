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
package org.ops4j.pax.runner;

import static org.ops4j.pax.runner.CommandLine.OPTION_EXECUTOR;

/**
 * Resolvs options by:<br/>
 * 1. look in the cache;<br/>
 * 2. look in command line;<br/>
 * 3. look for an alias and if found look in the command line for the alias<br/>
 * 3. if value is "choose" ask the user;<br/>
 * 4. look in configuration for a default value
 *
 * TODO add unit testing
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class OptionResolverImplExtended extends OptionResolverImpl
    implements OptionResolver
{

    /**
     * Creates anew option resolver.
     *
     * @param commandLine   command line to use
     * @param configuration configuration to use
     */
    public OptionResolverImplExtended( final CommandLine commandLine, final Configuration configuration )
    {
        super(commandLine, configuration);
    }

    /**
     * {@inheritDoc}
     */
    public String get( final String name )
    {
        final String result = super.get(name);

        // using executor=inProcess needs --absoluteFilePaths
        if( name.equalsIgnoreCase( org.ops4j.pax.runner.platform.ServiceConstants.CONFIG_USE_ABSOLUTE_FILE_PATHS )
            && result == null )
        {
            final String executor = get( OPTION_EXECUTOR );
            if( "inProcess".equalsIgnoreCase( executor ) )
            {
                return "TRUE";
            }
        }

        return result;
    }

    

}
