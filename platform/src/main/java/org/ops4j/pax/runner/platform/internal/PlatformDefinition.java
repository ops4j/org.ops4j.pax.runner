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
package org.ops4j.pax.runner.platform.internal;

import java.net.URL;
import java.util.List;
import org.ops4j.pax.runner.platform.BundleReference;

/**
 * Abstracts the platform definition file.
 *
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public interface PlatformDefinition
{

    /**
     * Returns the system package = a jar containing the main platform classes.
     *
     * @return the system package url
     */
    URL getSystemPackage();

    /**
     * Returns the "nice" name for system package.
     *
     * @return name of system package
     */
    String getSystemPackageName();

    /**
     * Returns a list of comma separated list of packages required by the platform.
     * The list will be appended to the system packages.
     *
     * @return a comma separated list of packages
     */
    String getPackages();

    /**
     * Returns a list of bundles to be installed as part of the platform.
     * If profile name is null or empty will return the default profile.
     *
     * @param profileName name of the profile in use
     *
     * @return a list of bundle urls
     */
    List<BundleReference> getPlatformBundles( String profileName );

}
