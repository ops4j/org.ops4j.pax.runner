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

import java.net.URL;

/**
 * Metadata about system file.
 *
 * @author Alin Dreghiciu
 * @since 0.15.0, October 28, 2008
 */
public interface SystemFileReference
{

    /**
     * Returns the name to the system file to be installed.
     * Name is used for nice diplaying when required, as for example when downloading the reference to a local system
     * file directory.
     *
     * @return an name
     */
    String getName();

    /**
     * Returns the url to the system file to be installed.
     *
     * @return an url
     */
    URL getURL();

    /**
     * Should the system file be in the classpath appended or prepended to framework system file.
     *
     * @return true, if the system file should be prepended
     */
    Boolean shouldPrepend();

}