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
package org.ops4j.pax.runner.scanner.dir.internal;

import java.util.regex.Pattern;

/**
 * Parser for dir scanner url specification.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public interface Parser
{

    /**
     * Returns the URL to the directory containing the bundles to be installed.
     *
     * @return an URL
     */
    String getURL();

    /**
     * Returns the start level option.
     *
     * @return the start level option value.
     */
    Integer getStartLevel();

    /**
     * Returns the start option.
     *
     * @return the start level option value
     */
    Boolean shouldStart();

    /**
     * Returns the filter that entries should match or null if no filter was specified.
     *
     * @return a filer
     */
    Pattern getFilter();
}
