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
 * Provision service allow unified provisioning based on provisioning scheme.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public interface ProvisionService
{

    /**
     * Parses the spec and based on the provision scheme from the spec delegates to the right provisioning scheme.<br/>
     * The spec has the syntax:<br/>
     * scheme:scheme_specific_path<br/>
     * where:<br/>
     * * scheme is the scheme to be used<br/>
     * * scheme_specific_path is a scheme specific string
     *
     * @param spec the provisioning spec
     *
     * @return a set of installable bundle
     *
     * @throws MalformedSpecificationException
     *                          if the scheme is not available or the string could not be parsed
     * @throws ScannerException if a scanning process related exception occured
     */
    InstallableBundles scan( String spec )
        throws MalformedSpecificationException, ScannerException;

}
