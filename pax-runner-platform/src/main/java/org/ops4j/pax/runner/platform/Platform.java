/*
 * Copyright 2007 Alin Dreghiciu, Stuart McCulloch.
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

import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

/**
 * Platform service exposes a service that could start an osgi implementation.
 *
 * @author Alin Dreghiciu, Stuart McCulloch
 * @since August 20, 2007
 */
public interface Platform
{

    /**
     * Starts the platform using the internal Java runner service and waits for it to exit.
     *
     * @param systemFiles        a list of system files to be available in the classpath; optional
     * @param bundles            a list of bundles to be included in the target platform; optional
     * @param platformProperties platform properties to be available in the started platform; optional
     * @param config             service configuration properties
     *
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *          if something goes wrong
     */
    void start( List<SystemFileReference> systemFiles, List<BundleReference> bundles, Properties platformProperties,
                Dictionary config )
        throws PlatformException;

    /**
     * Starts the platform using an external Java runner service, which may not wait for it to exit.
     *
     * @param systemFiles        a list of system files to be available in the classpath; optional
     * @param bundles            a list of bundles to be included in the target platform; optional
     * @param platformProperties platform properties to be available in the started platform; optional
     * @param config             service configuration properties
     * @param runner             an external service to run Java programs
     *
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *          if something goes wrong
     */
    void start( List<SystemFileReference> systemFiles, List<BundleReference> bundles, Properties platformProperties,
                Dictionary config, JavaRunner runner )
        throws PlatformException;

}
