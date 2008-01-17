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

import java.io.IOException;
import java.io.InputStream;

/**
 * Callback methods for platform specific actions.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public interface PlatformBuilder
{

    /**
     * Prepares platform for running. Specific tasks could include creating of configuration files for platform, ...
     *
     * @param context context information
     *
     * @throws PlatformException if something goes wrong durring building the platform
     */
    void prepare( PlatformContext context )
        throws PlatformException;

    /**
     * Returns the name of the main class of the platform, class that will be used to fire up the platform.
     *
     * @return main class name
     */
    String getMainClassName();

    /**
     * Returns an array of platform specific startup arguments. This are appended to the startup command.
     *
     * @param context context information
     *
     * @return an array of arguments
     */
    String[] getArguments( PlatformContext context );

    /**
     * Returns an array of virtual machine options specific to platform as -D.
     *
     * @param context context information
     *
     * @return an array of system properties
     */
    String[] getVMOptions( PlatformContext context );

    /**
     * Retuns an input stream out of the definition file to be used.
     *
     * @return an input stream
     *
     * @throws java.io.IOException if the input stream could not be opened.
     */
    InputStream getDefinition()
        throws IOException;

    /**
     * Returns a comma separated list of profiles if required by platform or null if no profile is required.
     *
     * @param context context information
     *
     * @return a comma separated list of profiles
     */
    String getRequiredProfile( PlatformContext context );

    /**
     * Returns the name of the provider (e.g. felix, equionox)
     *
     * @return name of the provider
     */
    String getProviderName();

    /**
     * Returns the version of the provider (e.g. 1.0, 3.3.0)
     *
     * @return version of the provider
     */
    String getProviderVersion();

}
