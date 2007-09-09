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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * A bundle that can be iterated, installed and/or started.
 *
 * @author Alin Dreghiciu
 * @since August 17, 2007
 */
public interface InstallableBundle
{

    /**
     * Return the installed bundle. If the bunndle was not installed returns null.
     *
     * @return the installed bundle
     */
    Bundle getBundle();

    /**
     * Installs the bundle.
     *
     * @return itself, for fluent api usage
     *
     * @throws BundleException if the bundle could not be installed
     */
    InstallableBundle install()
        throws BundleException;

    /**
     * Starts the installed bundle if the bundle should be started.
     * If the bundle was not installed before it will first try to install the bundle.
     *
     * @return itself, for fluent api usage
     *
     * @throws BundleException if the bundle could not be installed
     */
    InstallableBundle startIfNecessary()
        throws BundleException;

    /**
     * Starts the installed bundle regardles the fact that the bundle was marked that it should not start.
     * If the bundle was not installed before it will first try to install the bundle.
     *
     * @return itself, for fluent api usage
     *
     * @throws BundleException if the bundle could not be installed
     */
    InstallableBundle start()
        throws BundleException;

}
