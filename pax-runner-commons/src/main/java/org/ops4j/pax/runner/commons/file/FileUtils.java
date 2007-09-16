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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Reads content of a text files and returns every line as an entry to a List.
     *
     * @param fileURL        url of the file to be read
     * @param skipEmptyLines if empty lines should be skippied
     *
     * @return a list of strings, one entry for each line (depending if it should skip empty lines or not)
     *
     * @throws IOException re-thrown if an exception appear during processing of input stream
     */
    public static List<String> readTextFile( final URL fileURL, final boolean skipEmptyLines )
        throws IOException
    {
        final List<String> content = new ArrayList<String>();
        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader( new InputStreamReader( fileURL.openStream() ) );
            String line;
            while( ( line = bufferedReader.readLine() ) != null )
            {
                if( !skipEmptyLines || line.trim().length() > 0 )
                {
                    content.add( line );
                }
            }
        }
        finally
        {
            if( bufferedReader != null )
            {
                bufferedReader.close();
            }
        }
        return content;
    }

}