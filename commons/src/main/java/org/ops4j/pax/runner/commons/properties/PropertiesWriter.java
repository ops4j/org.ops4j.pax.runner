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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ops4j.pax.runner.commons.Assert;

/**
 * Helper for writing properties files.
 */
public class PropertiesWriter
{

    /**
     * Default separator.
     */
    private static final String DEFAULT_SEPARATOR = ",";
    /**
     * The output stream to write the properties to.
     */
    private final OutputStream m_outputStream;
    /**
     * List of lines to written.
     */
    private List<String> m_content;
    /**
     * A mapping between key of property to be written and the position in the list.
     */
    private Map<String, Integer> m_positions;
    /**
     * A mapping between key of property to be written and list of values.
     */
    private Map<String, List<String>> m_values;
    /**
     * Separator to be used to separate multiple properties.
     */
    private String m_separator;

    /**
     * Creates a new property writer with default sperator (,).
     *
     * @param outputStream the output stream to write properties to.
     */
    public PropertiesWriter( final OutputStream outputStream )
    {
        this( outputStream, DEFAULT_SEPARATOR );
    }

    /**
     * Creates a new property writer with a custom separator.
     *
     * @param outputStream the output stream to write properties to.
     * @param separator    separator to be used to separate multiple properties
     */
    public PropertiesWriter( final OutputStream outputStream, final String separator )
    {
        Assert.notNull( "Output stream", outputStream );
        Assert.notNull( "Separator", separator );
        m_outputStream = outputStream;
        m_separator = separator;
        m_content = new ArrayList<String>();
        m_positions = new HashMap<String, Integer>();
        m_values = new HashMap<String, List<String>>();
    }

    /**
     * Appends a property to be written.
     *
     * @param key   key of property
     * @param value value of property
     *
     * @return self for a fluent api
     */
    public PropertiesWriter append( final String key, final String value )
    {
        Assert.notNull( "Key", key );
        String valueToAdd = value;
        if ( value == null )
        {
            valueToAdd = "";
        }
        Integer position = m_positions.get( key );
        List<String> values = m_values.get( key );
        if ( values == null )
        {
            values = new ArrayList<String>();
            m_values.put( key, values );
        }
        values.add( valueToAdd );
        StringBuilder builder = new StringBuilder().append( key + "=" );
        if ( values.size() == 1 )
        {
            builder.append( valueToAdd );
        }
        else
        {
            builder.append( "\\\n" );
            String trail = null;
            for ( String storedValue : values )
            {
                if ( trail != null )
                {
                    builder.append( trail );
                }
                builder.append( storedValue );
                trail = m_separator + "\\\n";
            }
        }
        if ( position == null )
        {
            m_content.add( builder.toString() );
            m_positions.put( key, m_content.size() - 1 );
        }
        else
        {
            m_content.set( position, builder.toString() );
        }
        return this;
    }

    /**
     * Appends a comment to be written.
     *
     * @return self for a fluent api
     */
    public PropertiesWriter append( final String comment )
    {
        String commentToAdd = "#" + comment;
        if ( comment == null )
        {
            commentToAdd = "#";
        }
        m_content.add( commentToAdd );
        return this;
    }

    /**
     * Appends an empty line
     *
     * @return self for a fluent api
     */
    public PropertiesWriter append()
    {
        m_content.add( "" );
        return this;
    }

    /**
     * Appends a raw value = will be exactely as recived.
     *
     * @param value a raw value
     *
     * @return self for fluent api
     */
    public PropertiesWriter appendRaw( final String value )
    {
        if ( value != null )
        {
            m_content.add( value );
        }
        return this;
    }

    /**
     * Write properties to output stream.
     *
     * @throws java.io.IOException re-thrown from output stream
     */
    public void write()
        throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( m_outputStream ) );
        for ( String line : m_content )
        {
            writer.write( line );
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

}
