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
package org.ops4j.pax.runner.scanner.dir.internal;

import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * List urls from different sources. Implementatiosn mat list urls from file directory or zip files.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public interface Lister
{

    /**
     * Returns a list of urls the lister knows about.
     * @return list of urls
     */
    List<URL> list()
        throws MalformedURLException;

}
