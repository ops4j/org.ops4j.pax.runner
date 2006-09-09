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
package org.ops4j.pax.runner.idea.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunnerSettings;
import org.ops4j.pax.runner.idea.config.ConfigBean;

public class FelixRunner extends JavaCommandLineState
{
    public FelixRunner( ConfigBean config, RunnerSettings runnerSettings, ConfigurationPerRunnerSettings confSettings )
    {
        super( runnerSettings, confSettings );
        //TODO: Auto-generated, need attention.
    }

    protected JavaParameters createJavaParameters()
        throws ExecutionException
    {
        JavaParameters params = new JavaParameters();
        return params;
    }
}
