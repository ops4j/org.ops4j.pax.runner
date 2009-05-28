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
package org.ops4j.pax.cursor.shared;

/**
 * Pax Cursor configuration attributes names.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class Attribute
{

    /**
     * The name space of Pax Cursor configuration attributes.
     */
    private static final String NAME_SPACE = "org.ops4j.pax.cursor";

    /**
     * Overwrite all bundles attribute. Value is a boolean.
     */
    public static final String OVERWRITE_ALL = NAME_SPACE + ".overwrite";
    /**
     * Overwrite user bundles bundles attribute. Value is a boolean.
     */
    public static final String OVERWRITE_USER = NAME_SPACE + ".overwriteUserBundles";
    /**
     * Overwrite system bundles attribute. Value is a boolean.
     */
    public static final String OVERWRITE_SYSTEM = NAME_SPACE + ".overwriteSystemBundles";
    /**
     * Log level attribute. Value is a String.
     */
    public static final String LOG_LEVEL = NAME_SPACE + ".logLevel";
    /**
     * Provisioning urls attribute. Value is a map where the key is the url to be provisioned and value is a "@"
     * separated string of selected, start, start level, update options.
     */
    public static final String PROVISION_ITEMS = NAME_SPACE + ".provisionItems";
    /**
     * Profiles attribute. Value is a list of profiles names.
     */
    public static final String PROFILES = NAME_SPACE + ".profiles";

    /**
     * Pax Runner Arguments attribute. Value is a list of Pax Runner arguments that Pax Cursor sends them unchanged.
     */
    public static final String RUN_ARGUMENTS = NAME_SPACE + ".runArguments";
}
