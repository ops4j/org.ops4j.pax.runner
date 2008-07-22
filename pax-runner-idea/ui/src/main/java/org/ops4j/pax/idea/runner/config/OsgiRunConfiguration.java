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
package org.ops4j.pax.idea.runner.config;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.JavaProgramRunner;
import com.intellij.execution.runners.RunnerInfo;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizable;
import org.ops4j.pax.idea.runner.editor.OsgiConfigurationEditor;
import org.ops4j.pax.idea.runner.run.EquinoxRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OsgiRunConfiguration extends RunConfigurationBase
    implements RunConfiguration
{
    private static final Log m_logger = LogFactory.getLog( OsgiRunConfiguration.class );
    private ConfigBean m_configBean;

    public OsgiRunConfiguration( final Project project, final ConfigurationFactory configFactory, final String name )
    {
        super( project, configFactory, name );
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "OsgiRunConfiguration(" + project + ", " + configFactory + ", " + "\"" + name +"\"" );
        }
        m_configBean = new ConfigBean( project, this );
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getConfigurationEditor()" );
        }

        return new OsgiConfigurationEditor( this );
    }

    public JDOMExternalizable createRunnerSettings( ConfigurationInfoProvider provider )
    {
        //TODO: Auto-generated, need attention.
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "createRunnerSettings(" + provider + ")    :  " + provider.getRunnerSettings() );
        }
        return provider.getRunnerSettings();
    }

    public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor( JavaProgramRunner runner )
    {
        //TODO: Auto-generated, need attention.
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getRunnerSettingsEditor( " + runner + ")" );
            m_logger.debug(  "runner.getInfo() : " + runner.getInfo() );
        }
        return runner.getSettingsEditor( this );
    }

    /**
     * todo - javadoc
     *
     * @param context
     * @param runnerInfo
     * @param runnerSettings
     * @param configurationSettings
     */
    public RunProfileState getState( DataContext context, RunnerInfo runnerInfo, RunnerSettings runnerSettings,
                                     ConfigurationPerRunnerSettings configurationSettings )
        throws ExecutionException
    {
        //TODO: Auto-generated, need attention.
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getState(" + context + ", " + runnerInfo + ", " + runnerSettings + ", " + configurationSettings + ")" );
        }

        Project project = getProject();
        String selected = m_configBean.getSelectedPlatform();
        TextConsoleBuilderFactory consoleBuidlerFactory = TextConsoleBuilderFactory.getInstance();
        TextConsoleBuilder consoleBuilder = consoleBuidlerFactory.createBuilder( project );
        if( "Equinox".equalsIgnoreCase( selected ) )
        {
            EquinoxRunner runner = new EquinoxRunner( m_configBean, runnerSettings, configurationSettings );
            runner.setConsoleBuilder( consoleBuilder );
            return runner;
        }
        return null;
    }

    public void checkConfiguration()
        throws RuntimeConfigurationException
    {
        //TODO: Auto-generated, need attention.
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "checkConfiguration()" );
        }
    }

    // return modules to compile before run. Null or empty list to make project
    public Module[] getModules()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getModules()" );
        }
        return null;
    }

    public ConfigBean getConfigBean()
    {
        return m_configBean;
    }
}
