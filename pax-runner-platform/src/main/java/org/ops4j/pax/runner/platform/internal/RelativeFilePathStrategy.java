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
package org.ops4j.pax.runner.platform.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.FilePathStrategy;

/**
 * A relative {@link FilePathStrategy} that normalizes the paths relative to a base directory.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.20.0, may 10, 2009
 */
public class RelativeFilePathStrategy
    implements FilePathStrategy
{

    /**
     * JCL
     */
    private static Log LOG = LogFactory.getLog( RelativeFilePathStrategy.class );

    /**
     * Base directory.
     */
    private final File m_baseDirectory;

    /**
     * Constructor.
     *
     * @param baseDirectory base directory that normalized paths are relative to (cannot be null)
     *
     * @throws IllegalArgumentException - If baseDirectory is null
     */
    public RelativeFilePathStrategy( final File baseDirectory )
    {
        m_baseDirectory = baseDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public String normalizeAsPath( final File file )
    {
        return normalizePath( m_baseDirectory, file );
    }

    /**
     * {@inheritDoc}
     */
    public String normalizeAsUrl( final File file )
    {
        return "file:" + normalizePath( m_baseDirectory, file );
    }

    /**
     * {@inheritDoc}
     */
    public String normalizeAsUrl( final URL url )
    {
        if( "file".equals( url.getProtocol() ) )
        {
            return "file:" + normalizePath( m_baseDirectory, new File( url.getFile() ) );
        }
        return url.toExternalForm();
    }

    /**
     * Here we finally decide on actual paths showing up in generated config files and commandline args.
     *
     * @param baseFolder folder to be used. This is what we will cut off.
     * @param file       to be normalized.
     *
     * @return if file is a child of base then we will return the relative path. If not, the full path of file will be returned.
     */
    private String normalizePath( final File baseFolder, final File file )
    {
        String out = file.getAbsolutePath();
        try
        {
            if( baseFolder.equals( file ) )
            {
                out = ".";
            }
            else
            {
                String s1 = baseFolder.getCanonicalPath();
                String s2 = file.getCanonicalPath();
                if( s2.startsWith( s1 ) )
                {
                    out = s2.substring( s1.length() + 1 );
                }
                else
                {
                    out = s2;
                }
            }
        }
        catch( IOException e )
        {
            LOG.warn( "problem during normalizing path.", e );
        }
        return out.replace( File.separatorChar, '/' );
    }

}
