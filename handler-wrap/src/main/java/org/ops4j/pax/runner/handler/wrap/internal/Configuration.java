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
package org.ops4j.pax.runner.handler.wrap.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Handler configuration.
 *
 * @author Alin Dreghiciu
 * @since September 09, 2007
 */
public interface Configuration
{

    /**
     * Returns true if the certificate should be checked on SSL connection, false otherwise.
     *
     * @return true if the certificate should be checked
     */
    Boolean getCertificateCheck();

}
