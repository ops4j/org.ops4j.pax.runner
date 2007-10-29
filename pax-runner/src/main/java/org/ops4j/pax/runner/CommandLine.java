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

import java.util.List;

/**
 * Abstracts accesss to command line arguments.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public interface CommandLine
{

    /**
     * Configuration url option.
     */
    static final String OPTION_CONFIG = "config";
    /**
     * Debug option.
     */
    static final String OPTION_DEBUG = "debug";
    /**
     * Handlers option.
     */
    static final String OPTION_HANDLERS = "handlers";
    /**
     * Scanners option.
     */
    static final String OPTION_SCANNERS = "scanners";
    /**
     * Platform option.
     */
    static final String OPTION_PLATFORM = "platform";
    /**
     * Platform version option.
     */
    static final String OPTION_PLATFORM_VERSION = "version";    

    /**
     * Returns the value of an option by key. If option is not defined returns null.
     *
     * @param key option key
     * @return The option value.
     */
    String getOption( String key );

    /**
     * Returns the list of all arguments.
     *
     * @return list of arguments
     */
    List<String> getArguments();

}
