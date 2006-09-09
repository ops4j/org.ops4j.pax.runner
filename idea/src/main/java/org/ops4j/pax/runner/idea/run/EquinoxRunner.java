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
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.util.PathsList;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.idea.config.ConfigBean;

public class EquinoxRunner extends JavaCommandLineState
{
    private static final Logger m_logger = Logger.getLogger( EquinoxRunner.class );

    private ConfigBean m_configBean;

    public EquinoxRunner( ConfigBean configBean, RunnerSettings runnerSettings,
                          ConfigurationPerRunnerSettings configurationSettings )
    {
        super( runnerSettings, configurationSettings );
        m_configBean = configBean;
    }

    protected JavaParameters createJavaParameters()
        throws ExecutionException
    {
        JavaParameters params = new JavaParameters();
        params.setJdk( m_configBean.getJdk() );
        params.setMainClass( "org.ops4j.pax.runner.Main" );
        params.setWorkingDirectory( m_configBean.getWorkDir() );
        processClasspath( params );
        processArguments( params );
        processVmArgs( params );
        return params;
    }

    public ExecutionResult execute()
        throws ExecutionException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "execute() - start");
        }
        ExecutionResult result = super.execute();
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "execute() - finished: " + result );
        }
        return result;
    }

    protected OSProcessHandler startProcess()
        throws ExecutionException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "startProcess() - start");
        }
        OSProcessHandler osProcessHandler = super.startProcess();
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "startProcess() - finished: " + osProcessHandler );
        }
        return osProcessHandler;    //TODO: Auto-generated, need attention.
    }

    private void processClasspath( JavaParameters params )
    {
        PathsList classpath = params.getClassPath();
    }

    private void processArguments( JavaParameters params )
    {
        ParametersList arguments = params.getProgramParametersList();
    }

    private void processVmArgs( JavaParameters params )
    {
        ParametersList vmArgs = params.getVMParametersList();
        StringTokenizer st = new StringTokenizer( m_configBean.getVmArguments(), " ", false );
        while( st.hasMoreTokens() )
        {
            vmArgs.add( st.nextToken() );
        }
    }

}
