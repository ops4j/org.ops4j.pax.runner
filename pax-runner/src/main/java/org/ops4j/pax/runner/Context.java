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

import java.util.List;
import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.util.EventDispatcher;
import org.ops4j.pax.runner.osgi.RunnerBundle;

/**
 * Runner context. Holder for objects to be passed arround.
 *
 * @author Alin Dreghiciu
 * @since August 29, 2007
 */
public interface Context
{

    /**
     * Returns the command line in use.
     *
     * @return a command line
     */
    CommandLine getCommandLine();

    /**
     * Sets the command line in use.
     *
     * @param commandLine a command line
     *
     * @return self, for fluent api.
     */
    Context setCommandLine( CommandLine commandLine );

    /**
     * Returns the configuration in use.
     *
     * @return a configuration
     */
    Configuration getConfiguration();

    /**
     * Sets the configuration in use.
     *
     * @param configuration a configuration
     *
     * @return self, for fluent api.
     */
    Context setConfiguration( Configuration configuration );

    /**
     * Returns the option resolver in use.
     *
     * @return an option resolver
     */
    OptionResolver getOptionResolver();

    /**
     * Sets the option resolver in use.
     *
     * @param optionResolver an option resolver
     *
     * @return self, for fluent api.
     */
    Context setOptionResolver( OptionResolver optionResolver );

    /**
     * Returns the service registry in use.
     *
     * @return a service registry
     */
    ServiceRegistry getServiceRegistry();

    /**
     * Sets the service registry  in use.
     *
     * @param serviceRegistry a service registry
     *
     * @return self, for fluent api.
     */
    Context setServiceRegistry( ServiceRegistry serviceRegistry );

    /**
     * Returns the event dispatcher in use.
     *
     * @return a event dispatcher
     */
    EventDispatcher getEventDispatcher();

    /**
     * Sets the event dispatcher  in use.
     *
     * @param eventDispatcher an event dispatcher
     *
     * @return self, for fluent api.
     */
    Context setEventDispatcher( EventDispatcher eventDispatcher );

    /**
     * Adds a runner bundle to the list of bunsles to be installed in the platform
     *
     * @param bundle a runner bundle to add
     *
     * @return self, for fluent api.
     */
    Context addBundle( RunnerBundle bundle );

    /**
     * Returns a list of runner bundles to be installed.
     *
     * @return a list of runner bundles
     */
    List<RunnerBundle> getBundles();

}
