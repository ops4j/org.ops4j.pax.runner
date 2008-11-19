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

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * PlatformImpl start context.
 * Contains startup related objects.
 * Passed arround to platform builder for thread safety.
 *
 * @author Alin Dreghiciu
 * @since August 20, 2007
 */
public interface PlatformContext
{

    /**
     * Returns a list of bundles to be installed.
     *
     * @return a list of local bundles
     */
    List<LocalBundle> getBundles();

    /**
     * Sets the client bundles to be installed.
     *
     * @param platformBundles a list of Local Bundles
     */
    void setBundles( List<LocalBundle> platformBundles );

    /**
     * Returns the working directory.
     *
     * @return a working directory
     */
    File getWorkingDirectory();

    /**
     * Sets the working directory.
     *
     * @param workingDirectory a file directory
     */
    void setWorkingDirectory( File workingDirectory );

    /**
     * Returns platform properties.
     *
     * @return platform properties
     */
    Properties getProperties();

    /**
     * Sets the platform properties.
     *
     * @param properties a properties file. Can be null.
     */
    void setProperties( Properties properties );

    /**
     * Returns the list of system packages in use depending on java version.
     *
     * @return a list of system packages
     */
    String getSystemPackages();

    /**
     * Sets the system packages tht will be exported by the framework.
     * System packages must be a comma separated list of system packages.
     *
     * @param packageList comma separated list of system packages
     */
    void setSystemPackages( String packageList );

    /**
     * Returns the configuration.
     *
     * @return a configuration
     */
    Configuration getConfiguration();

    /**
     * Sets the configuration.
     *
     * @param configuration a configuration
     */
    void setConfiguration( Configuration configuration );

    /**
     * Sets the execution environment.
     *
     * @param executionEnvironment comma separated list of supported execution environments
     */
    void setExecutionEnvironment( String executionEnvironment );

    /**
     * Returns the execution environments.
     *
     * @return comma separated list of framewok supported execution environments.
     */
    String getExecutionEnvironment();

}
