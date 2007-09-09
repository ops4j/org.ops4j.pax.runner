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

import org.osgi.framework.BundleException;

/**
 * A set of bundles that can be iterated, installed or started as a group.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public interface InstallableBundles
    extends Iterable<InstallableBundle>
{

    /**
     * Installs the available bundles.
     *
     * @return itself, for fluent api usage
     */
    InstallableBundles install()
        throws BundleException;

}
