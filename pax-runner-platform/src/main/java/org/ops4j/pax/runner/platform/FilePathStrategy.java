/*
 * Copyright 2009 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.platform;

import java.io.File;
import java.net.URL;

/**
 * Strategy to be used regarding file paths.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.20.0, May 10, 2009
 */
public interface FilePathStrategy
{

    /**
     * Returns the file in relative form (compared to working directory)
     *
     * @param file file to normalize
     *
     * @return the file in relative form (compared to working directory)
     */
    String normalizeAsPath( File file );

    /**
     * Returns the file in relative form as url (compared to working directory)
     *
     * @param file file to normalize
     *
     * @return the file in relative form as url (compared to working directory)
     */
    String normalizeAsUrl( File file );

    /**
     * Returns the url in relative form as url (compared to working directory).
     * The normalization is done only in case that the url is a file url.
     *
     * @param url url to normalize
     *
     * @return the url in relative form as url (compared to working directory)
     */
    String normalizeAsUrl( URL url );

}
