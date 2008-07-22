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
package org.ops4j.pax.idea.runner.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.OSProcessHandler;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import org.ops4j.pax.model.bundles.BundleRef;
import org.ops4j.pax.idea.runner.config.ConfigBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EquinoxRunner extends JavaCommandLineState
{
    private static final Log m_logger = LogFactory.getLog( EquinoxRunner.class );

    private ConfigBean m_configBean;

    public EquinoxRunner( ConfigBean configBean, RunnerSettings runnerSettings,
                          ConfigurationPerRunnerSettings configurationSettings )
    {
        super( runnerSettings, configurationSettings );
        m_configBean = configBean;
        Properties props = configBean.getProperties();
    }

    protected JavaParameters createJavaParameters()
        throws ExecutionException
    {
        JavaParameters params = new JavaParameters();
        params.setJdk( m_configBean.getJdk() );
        File workDir = m_configBean.getWorkDir();
        params.setWorkingDirectory( workDir );
        processClasspath( params );
        processArguments( params, workDir );
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
//        m_preparer.prepareForRun( m_configBean );
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
        // No classpath support at the moment.
    }

    private void processArguments( JavaParameters params, File workDir )
    {
        ParametersList arguments = params.getProgramParametersList();
        arguments.add( "-console" );
        arguments.add( "-configuration", workDir.getAbsolutePath() + "/configuration" );
        arguments.add( "-install", workDir.getAbsolutePath() );

    }

    private void processVmArgs( JavaParameters params )
    {
        ParametersList vmArgs = params.getVMParametersList();
        BundleRef ref = m_configBean.getSystemBundle();
        URL location = ref.getLocation();
        String jarFile = location.getPath();
        StringTokenizer st = new StringTokenizer( m_configBean.getVmArguments(), " ", false );
        while( st.hasMoreTokens() )
        {
            vmArgs.add( st.nextToken() );
        }
        vmArgs.add( "-jar", jarFile ); // must be last
    }

}
