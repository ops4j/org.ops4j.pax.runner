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
package org.ops4j.pax.runner.commons.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.util.collections.PropertiesWriter;
import org.ops4j.io.FileUtils;

public class PropertiesWriterTest
{

    private OutputStream m_os;
    private File m_file;

    @Before
    public void setUp()
        throws IOException
    {
        m_file = File.createTempFile( "writer", ".ini" );
        m_file.deleteOnExit();
        m_os = new FileOutputStream( m_file );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullOutputStream()
    {
        new PropertiesWriter( null );
    }

    // test empty line
    @Test
    public void appendNewLine()
        throws IOException
    {
        new PropertiesWriter( m_os ).append().write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/newline.ini" ),
            m_file,
            true
        );
    }

    // test comment
    @Test
    public void appendComment()
        throws IOException
    {
        new PropertiesWriter( m_os ).append( "my comment" ).write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/comment.ini" ),
            m_file,
            true
        );
    }

    // test null comment
    @Test
    public void appendNullComment()
        throws IOException
    {
        new PropertiesWriter( m_os ).append( null ).write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/nullcomment.ini" ),
            m_file,
            true
        );
    }

    // test valid property
    @Test
    public void appendValidProperty()
        throws IOException
    {
        new PropertiesWriter( m_os ).append( "key", "value" ).write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/validproperty.ini" ),
            m_file,
            true
        );
    }

    // expect an illegal argument exception when null key is used
    @Test( expected = IllegalArgumentException.class )
    public void appendNullPropertyKey()
        throws IOException
    {
        new PropertiesWriter( m_os ).append( null, "value" );
    }

    // test valid property with null value
    @Test
    public void appendValidPropertyWithNullValue()
        throws IOException
    {
        new PropertiesWriter( m_os ).append( "key", null ).write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/nullvalueproperty.ini" ),
            m_file,
            true
        );
    }

    // test valid property
    @Test
    public void appendValidPropertyWithMoreValues()
        throws IOException
    {
        new PropertiesWriter( m_os ).append( "key", "value1" ).append( "key", "value2" ).write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/multiplevaluesproperty.ini" ),
            m_file,
            true
        );
    }

    // test valid property
    @Test
    public void appendRawProperty()
        throws IOException
    {
        new PropertiesWriter( m_os ).appendRaw( "key value" ).write();
        compareFiles(
            FileUtils.getFileFromClasspath( "propertieswriter/rawproperty.ini" ),
            m_file,
            true
        );
    }

    private static void compareFiles( File expected, File actual, boolean reverse )
        throws IOException
    {
        BufferedReader expectedReader = null;
        BufferedReader actualReader = null;
        try
        {
            expectedReader = new BufferedReader( new FileReader( expected ) );
            actualReader = new BufferedReader( new FileReader( actual ) );
            String actualLine, expectedLine;
            int lineNumber = 1;
            while( ( actualLine = actualReader.readLine() ) != null )
            {
                expectedLine = expectedReader.readLine();
                if( reverse )
                {
                    assertEquals( "Config ini line " + lineNumber++, expectedLine, actualLine );
                }
                else
                {
                    assertEquals( "Config ini line " + lineNumber++, actualLine, expectedLine );
                }
            }
        }
        finally
        {
            expectedReader.close();
            actualReader.close();
        }
        if( reverse )
        {
            compareFiles( actual, expected, false );
        }
    }


}
