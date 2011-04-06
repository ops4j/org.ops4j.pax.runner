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
package org.ops4j.pax.runner.osgi.felix;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.util.EventDispatcher;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.Configuration;
import org.ops4j.pax.runner.OptionResolver;

/**
 * Bean like implementation of context.
 * TODO add unit testing
 *
 * @author Alin Dreghiciu
 * @since August 29, 2007
 */
public class ContextImpl
    implements Context
{

    /**
     * Command line holder.
     */
    private CommandLine m_commandLine;
    /**
     * Configuration holder.
     */
    private Configuration m_configuration;
    /**
     * Option resolver holder.
     */
    private OptionResolver m_optionResolver;
    /**
     * A list of runner bundle references to be installed into the target platform.
     */
    private List<RunnerBundle> m_bundles;
    /**
     * Felix Service Registry.
     */
    private ServiceRegistry m_serviceRegistry;
    /**
     * Felix Event dispatcher.
     */
    private EventDispatcher m_dispatcher;
    /**
     * System properties to be used when starting the platform.
     */
    private Properties m_systemProperties;
    
    /**
     * Create a new Context implementation.
     */
    public ContextImpl()
    {
        m_bundles = new ArrayList<RunnerBundle>();
    }

    /**
     * {@inheritDoc}
     */
    public CommandLine getCommandLine()
    {
        return m_commandLine;
    }

    /**
     * {@inheritDoc}
     */
    public Context setCommandLine( final CommandLine commandLine )
    {
        m_commandLine = commandLine;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration()
    {
        return m_configuration;
    }

    /**
     * {@inheritDoc}
     */
    public Context setConfiguration( final Configuration configuration )
    {
        m_configuration = configuration;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public OptionResolver getOptionResolver()
    {
        return m_optionResolver;
    }

    /**
     * {@inheritDoc}
     */
    public Context setOptionResolver( final OptionResolver optionResolver )
    {
        m_optionResolver = optionResolver;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ServiceRegistry getServiceRegistry()
    {
        return m_serviceRegistry;
    }

    /**
     * {@inheritDoc}
     */
    public Context setServiceRegistry( ServiceRegistry serviceRegistry )
    {
        m_serviceRegistry = serviceRegistry;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public EventDispatcher getEventDispatcher()
    {
        return m_dispatcher;
    }

    /**
     * {@inheritDoc}
     */
    public Context setEventDispatcher( EventDispatcher dispatcher )
    {
        m_dispatcher = dispatcher;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Context addBundle( final RunnerBundle bundle )
    {
        m_bundles.add( bundle );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public List<RunnerBundle> getBundles()
    {
        return m_bundles;
    }

    /**
     * {@inheritDoc}
     */
    public Context setSystemProperties( Properties properties )
    {
        m_systemProperties = properties;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Properties getSystemProperties()
    {
        return m_systemProperties;
    }
}
