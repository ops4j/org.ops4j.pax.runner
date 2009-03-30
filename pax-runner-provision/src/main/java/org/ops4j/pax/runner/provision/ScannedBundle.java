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

/**
 * Metadata about the bundle.
 *
 * @author Alin Dreghiciu
 * @since August 16, 2007
 */
public interface ScannedBundle
{

    /**
     * Returns the url to the bundle to be installed.
     *
     * @return an url
     */
    String getLocation();

    /**
     * The start level of the bundle.
     * If null is returned the start level will not be set on the bundle.
     *
     * @return the start level
     */
    Integer getStartLevel();

    /**
     * Should the bundle should be started?
     *
     * @return true, if the bundle should start
     */
    Boolean shouldStart();

    /**
     * Should the bundle should be updated?
     *
     * @return true, if the bundle should be updated
     */
    Boolean shouldUpdate();

}
