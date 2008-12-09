/*
 * Copyright 2008 Alin Dreghiciu.
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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * {@link DefaultJavaRunner} unit tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 09, 2008
 */
public class DefaultJavaRunnerTest
{

    // normal flow
    @Test
    public void getJavaExecutable()
        throws Exception
    {
        assertEquals( "Java executable", "javaHome/bin/java", DefaultJavaRunner.getJavaExecutable( "javaHome" ) );
    }

    // test that a platform exception is thrown when there is no java home
    @Test( expected = PlatformException.class )
    public void getJavaExecutableWithInvalidJavaHome()
        throws Exception
    {
        DefaultJavaRunner.getJavaExecutable( null );
    }

}
