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
package org.ops4j.pax.runner;

import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Test;

public class AuditedPropertiesTest
{

    /**
     * Test with one placeholder.
     */
    @Test
    public void filteredProperty01()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder", "value" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", "${holder}" );
        assertEquals( "Filtered property value", "value", audited.getProperty( "filtered" ) );
    }

    /**
     * Test with more place holders with the same name that are not one after the other.
     */
    @Test
    public void filteredProperty02()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder", "value" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", " / ${holder} / ${holder} /" );
        assertEquals( "Filtered property value", " / value / value /", audited.getProperty( "filtered" ) );
    }

    /**
     * Test with more place holders with the same name one after the other.
     */
    @Test
    public void filteredProperty03()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder", "value" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", "${holder}${holder}" );
        assertEquals( "Filtered property value", "valuevalue", audited.getProperty( "filtered" ) );
    }

    /**
     * Test without placeholders.
     */
    @Test
    public void filteredProperty04()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder", "value" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", "holder" );
        assertEquals( "Filtered property value", "holder", audited.getProperty( "filtered" ) );
    }

    /**
     * Test with more place holders.
     */
    @Test
    public void filteredProperty05()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder1", "value1" );
        defaults.setProperty( "holder2", "value2" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", "${holder1} / ${holder2}" );
        assertEquals( "Filtered property value", "value1 / value2", audited.getProperty( "filtered" ) );
    }

    /**
     * Test with a place holders that does not exist.
     */
    @Test
    public void filteredProperty06()
    {
        Properties audited = new AuditedProperties( null );
        audited.setProperty( "filtered", "${holder}" );
        assertEquals( "Filtered property value", "${holder}", audited.getProperty( "filtered" ) );
    }

    /**
     * Test with a holder that contains another holder.
     */
    @Test
    public void filteredProperty07()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder1", "2" );
        defaults.setProperty( "holder2", "value2" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", "${holder${holder1}}" );
        assertEquals( "Filtered property value", "value2", audited.getProperty( "filtered" ) );
    }

    /**
     * Test with a holder and ${ before the holder.
     */
    @Test
    public void filteredProperty08()
    {
        Properties defaults = new Properties();
        defaults.setProperty( "holder", "value" );
        Properties audited = new AuditedProperties( defaults );
        audited.setProperty( "filtered", "${${holder}" );
        assertEquals( "Filtered property value", "${value", audited.getProperty( "filtered" ) );
    }

}
