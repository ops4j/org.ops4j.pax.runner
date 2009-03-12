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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * PlatformImpl configuration.
 *
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public interface Configuration
{

    /**
     * Returns the definition url option - optional; url to the xml file containing the definition of the platform
     * (system bundle, profiles).
     *
     * @return value of definition url option
     *
     * @throws java.net.MalformedURLException if the definition url is malformed
     */
    URL getDefinitionURL()
        throws MalformedURLException;

    /**
     * Returns the working directory option - ptional; a file path where the platform specific artifacts will be stored.
     * Default value "runner".
     *
     * @return value of working directory option
     */
    String getWorkingDirectory();

    /**
     * Returns the virtual machine options option - optional; virtual machine options to be used for the VM that will
     * be used for the platform.
     *
     * @return value of virtual machine options option
     */
    String[] getVMOptions();

    /**
     * Returns the classpatch option - optional; a File.pathSeparator separated list of classpath entries to be
     * appended to the framework classpath which consists out of the framework system jar.
     *
     * @return value of classpath option
     */
    String getClasspath();

    /**
     * Returns the system packages option - optional; a comma separated list of packages that should be exported by the
     * framework bundles, in addition to the standard packages list.
     *
     * @return value of system packages option
     */
    String getSystemPackages();

    /**
     * Returns the java version option - optional; the targeted version for the platform. Default is the same version
     * as the runner is working on.
     *
     * @return value of java version option
     */
    String getExecutionEnvironment();

    /**
     * Returns the java home option - optional; If no version was specified then first the value of system property
     * "JAVA_HOME" is used. If that is not set then teh value of system property "java.home" will be used (=same with
     * the home in which runner is running).
     *
     * @return value of java home option
     */
    String getJavaHome();

    /**
     * Returns true if persisted platform state (platform specific) should be used on start up.
     * Default value is false.
     *
     * @return true if persisted platform state (platform specific) should be used on start up
     */
    Boolean usePersistedState();

    /**
     * Returns the platform bundles start level option - optional; the start level at which the platform bundles will be
     * started. Default value is 1.
     *
     * @return value of platform bundles start level option.
     */
    Integer getProfileStartLevel();

    /**
     * Returns the platform start level option - optional; the platform start level. Default value is "6".
     *
     * @return value of platform start level option.
     */
    Integer getStartLevel();

    /**
     * Returns the installed bundles start level option - optional; the platform start level. Default value is "5".
     *
     * @return value of installed bundles start level option.
     */
    Integer getBundleStartLevel();

    /**
     * Returns the start console option - true/false; whether the platform console should be started.
     * Default value is "true".
     *
     * @return value of start console option
     */
    Boolean startConsole();

    /**
     * Returns true if the already downloaded bundles from a previous run must be overwritten.
     * Default value is "false".
     *
     * @return value of overwrite option
     */
    Boolean isOverwrite();

    /**
     * Returns the profiles option - optional; a comma separated list of profiles to be used.
     * Default value is "minimal".
     *
     * @return value of profiles option
     */
    String getProfiles();

    /**
     * Returns the framework profile option - optional; name of the framework profile to use.
     *
     * @return value of framework profile
     */
    String getFrameworkProfile();

    /**
     * Returns true if the cached bundles and target platforms configuration folder shall be removed.
     *
     * @return true if there should be a clean start
     */
    Boolean isCleanStart();

    /**
     * Returns true if the already downloaded user bundles from a previous run must be overwritten.
     * Default value is "false".
     *
     * @return value of overwrite user bundles option
     */
    Boolean isOverwriteUserBundles();

    /**
     * Returns true if the already downloaded system bundles from a previous run must be overwritten.
     * Default value is "false".
     *
     * @return value of overwrite system bundles option
     */
    Boolean isOverwriteSystemBundles();

    /**
     * Returns true if the platform should print debug messages about class loading.
     * Default value is "false".
     *
     * @return value of debug class loading option
     */
    Boolean isDebugClassLoading();

    /**
     * Returns true if detailed information about downloading process should be displayed.
     * Default value is true.
     *
     * @return value of download feedback option
     */
    Boolean isDownloadFeedback();

    /**
     * Returns a comma separated list of boot delegation packages according to OSGi core specs 3.2.3 (in 4.0.1). If not
     * set it should return null.
     * The returned value should not be an empty string and should not end with a comma (",").
     *
     * @return value of boot delegation option
     */
    String getBootDelegation();

    /**
     * Returns true if not osgi compliant jars should be automatically wrapped (transformed to osgi compliant).
     * Default value is false.
     *
     * @return value of auto wrap option
     */
    Boolean isAutoWrap();
    
}
