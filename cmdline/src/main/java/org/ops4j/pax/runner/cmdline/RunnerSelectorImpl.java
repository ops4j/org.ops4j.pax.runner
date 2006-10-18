/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner.cmdline;

import org.ops4j.pax.runner.RunnerSelector;
import org.ops4j.pax.runner.Runner;
import org.ops4j.pax.runner.exec.EquinoxPreparer;
import org.ops4j.pax.runner.exec.KnopflerfishPreparer;
import org.ops4j.pax.runner.exec.FelixPreparer;
import java.util.Properties;

public class RunnerSelectorImpl
    implements RunnerSelector
{

    public Runner select( String name, Properties props )
    {
        if( "equinox".equalsIgnoreCase( name ) )
        {
            return selectEquinox( props );
        }
        if( "felix".equalsIgnoreCase( name ) )
        {
            return selectFelix( props );
        }
        if( "knopflerfish".equalsIgnoreCase( name ) )
        {
            return selectKnopflerfish( props );
        }
        return null;
    }

    private Runner selectEquinox( Properties props )
    {
        EquinoxPreparer preparer = new EquinoxPreparer( props );
        return new EquinoxRunner(preparer);
    }

    private Runner selectKnopflerfish( Properties props )
    {
        KnopflerfishPreparer preparer = new KnopflerfishPreparer( props );
        return new KnopflerfishRunner( preparer);
    }

    private Runner selectFelix( Properties props )
    {
        FelixPreparer preparer = new FelixPreparer( props );
        return new FelixRunner(preparer);
    }
}
