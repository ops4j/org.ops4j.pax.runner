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
package org.ops4j.pax.runner.provision.scanner;

/**
 * Scanner generic configuration.
 *
 * @author Alin Dreghiciu
 * @since August 16, 2007
 */
public interface ScannerConfiguration
{

    /**
     * Returns an integer representing the configured default start level of the bundle.
     *
     * @return the tsrat level or null if not configured
     */
    Integer getStartLevel();

    /**
     * Returns if installed bundles should be started ot not. If the value is not configured will return true.
     *
     * @return true if bundles shouls be started
     */
    Boolean shouldStart();

    /**
     * If installed bundles should be updated ot not. If the value is not configured will return false.
     *
     * @return true if bundles shouls be updated
     */
    Boolean shouldUpdate();

    /**
     * Returns true if the certificate should be checked on SSL connection, false otherwise.
     *
     * @return true if the certificate should be checked
     */
    Boolean getCertificateCheck();    

}
