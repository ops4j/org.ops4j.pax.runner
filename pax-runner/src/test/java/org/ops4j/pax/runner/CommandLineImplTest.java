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

import static org.junit.Assert.*;
import org.junit.Test;

public class CommandLineImplTest
{

    @Test
    public void simpleOption()
    {
        CommandLine commandLine = new CommandLineImpl( "--simpleOption=simpleValue" );
        assertEquals( "Option value", "simpleValue", commandLine.getOption( "simpleOption" ) );
    }

    @Test
    public void optionWithAValueThatContainsEqual01()
    {
        CommandLine commandLine = new CommandLineImpl( "--simpleOption=-Dhttp.port=80" );
        assertEquals( "Option value", "-Dhttp.port=80", commandLine.getOption( "simpleOption" ) );
    }

    @Test
    public void optionWithAValueThatContainsEqual02()
    {
        CommandLine commandLine = new CommandLineImpl( "--simpleOption=-Dhttp.port=80 -Dhttp.port.secure=443" );
        assertEquals( "Option value", "-Dhttp.port=80 -Dhttp.port.secure=443", commandLine.getOption( "simpleOption" )
        );
    }

    @Test
    public void implicitFalseValue()
    {
        CommandLine commandLine = new CommandLineImpl( "--noSimpleOption" );
        assertEquals( "Option value", "false", commandLine.getOption( "simpleOption" ) );
    }

    @Test
    public void implicitTrueValue()
    {
        CommandLine commandLine = new CommandLineImpl( "--simpleOption" );
        assertEquals( "Option value", "true", commandLine.getOption( "simpleOption" ) );
    }

    @Test
    public void arrayValue()
    {
        CommandLine commandLine = new CommandLineImpl( "--array=v1", "--array=v2" );
        assertEquals( "Option value", "v1", commandLine.getOption( "array" ) );
        assertArrayEquals( "Option value", new String[]{ "v1", "v2" }, commandLine.getMultipleOption( "array" ) );
    }

    @Test
    public void arrayOneValue()
    {
        CommandLine commandLine = new CommandLineImpl( "--array=v1" );
        assertEquals( "Option value", "v1", commandLine.getOption( "array" ) );
        assertArrayEquals( "Option value", new String[]{ "v1" }, commandLine.getMultipleOption( "array" ) );
    }

    @Test
    public void arrayValueNotDefined()
    {
        CommandLine commandLine = new CommandLineImpl( "--some" );
        assertArrayEquals( "Option value", new String[0], commandLine.getMultipleOption( "array" ) );
    }

}
