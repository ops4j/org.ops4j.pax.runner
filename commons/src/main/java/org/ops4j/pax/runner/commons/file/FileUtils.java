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
package org.ops4j.pax.runner.commons.file;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * File related utilities.
 *
 * @author Alin Dreghiciu
 * @since August 19, 2007
 */
public class FileUtils
{

    /**
     * Utility class. Ment to be used via static methods.
     */
    private FileUtils()
    {
        // utility class
    }

    /**
     * Searches the classpath for the file denoted by the file path and returns the corresponding file.
     *
     * @param filePath path to the file
     *
     * @return a file corresponding to the path
     */
    public static File getFileFromClasspath( final String filePath )
    {
        try
        {
            URL fileURL = FileUtils.class.getClassLoader().getResource( filePath );
            if( fileURL == null )
            {
                throw new RuntimeException( "File [" + filePath + "] could not be found" );
            }
            return new File( fileURL.toURI() );
        } catch( URISyntaxException e )
        {
            throw new RuntimeException( "File [" + filePath + "] could not be found", e );
        }
    }

    /**
     * Deletes the file or recursively deletes a directory depending on the file passed
     *
     * @param file file or directory to be deleted.
     *
     * @return true if th efile was deleted.
     */
    public static boolean delete( final File file )
    {
        boolean delete = false;
        if( file != null && file.exists() )
        {
            // even if is a directory try to delete. maybe is empty or maybe is a *nix symbolic link
            delete = file.delete();
            if( !delete && file.isDirectory() )
            {
                File[] childs = file.listFiles();
                if( childs != null && childs.length > 0 )
                {
                    for( File child : childs )
                    {
                        delete( child );
                    }
                    // then try again as by now the directory can be empty
                    delete = file.delete();
                }
            }
        }
        return delete;
    }

}