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
package org.ops4j.pax.runner.handler.mvn;

/**
 * An enumeration of constants related to maven handler.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public interface ServiceConstants
{

    /**
     * Service PID used for configuration.
     */
    static final String PID = "org.ops4j.pax.runner.handler.mvn";
    /**
     * Certificate check configuration property name.
     */
    static final String PROPERTY_CERTIFICATE_CHECK = PID + ".certificateCheck";
    /**
     * Maven settings file configuration property name.
     */
    static final String PROPERTY_SETTINGS_FILE = PID + ".settings";
    /**
     * LocalRepository configuration property name.
     */
    static final String PROPERTY_LOCAL_REPOSITORY = PID + ".localRepository";
    /**
     * Repositories configuration property name.
     */
    static final String PROPERTY_REPOSITORIES = PID + ".repositories";
    /**
     * The protocol name.
     */
    public static final String PROTOCOL = "mvn";    

}
