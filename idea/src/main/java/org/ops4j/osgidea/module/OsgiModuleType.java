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
package org.ops4j.osgidea.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Computable;
import com.intellij.util.ArrayUtil;
import java.util.ArrayList;
import javax.swing.Icon;
import org.ops4j.osgidea.OsgiIcons;
import org.ops4j.pax.model.bundles.ManifestModel;

public class OsgiModuleType extends ModuleType<OsgiModuleBuilder>
{

    private static final String OSGI_MODULE_TYPE = "org.ops4j.osgidea.module.OsgiModuleType";
    private static OsgiModuleType m_instance = new OsgiModuleType();


    private OsgiModuleType()
    {
        super( OSGI_MODULE_TYPE );
    }

    public static OsgiModuleType getInstance()
    {
        return m_instance;
    }

    public ModuleWizardStep[] createWizardSteps( WizardContext wizardContext,
                                                 OsgiModuleBuilder moduleBuilder,
                                                 ModulesProvider modulesProvider )
    {
        ProjectWizardStepFactory wizardFactory = ProjectWizardStepFactory.getInstance();
        ManifestModel manifest = moduleBuilder.getManifest();
        manifest.setVendor( "OPS4J - Open Participation Software for Java");
        manifest.setVersion( "1.0.0" );
        manifest.setLicense( "Apache License -  http://www.apache.org/licenses/LICENSE-2.0");
        String projectName = wizardContext.getProjectName();
        manifest.setBundleName( projectName );
        manifest.setSymbolicName( "org.ops4j.pax." + projectName );

        ArrayList<ModuleWizardStep> steps = new ArrayList<ModuleWizardStep>();
        ModuleWizardStep nameAndLocationStep =
            wizardFactory.createNameAndLocationStep( wizardContext, moduleBuilder, modulesProvider,
                                                     OsgiIcons.WIZARD_PANEL,
                                                     "osgi.createOsgi"
            );
        steps.add( nameAndLocationStep );
        Computable computable = new Computable<Boolean>()
        {
            public Boolean compute()
            {
                return Boolean.TRUE;
            }
        };
        ModuleWizardStep sdkStep = wizardFactory.createProjectJdkStep( wizardContext, JavaSdk.getInstance(), moduleBuilder, computable, OsgiIcons.WIZARD_PANEL, "osgi.createOsgi" );
        steps.add( sdkStep );
        steps.add( wizardFactory.createSourcePathsStep( nameAndLocationStep, moduleBuilder, OsgiIcons.WIZARD_PANEL, "osgi.createOsgi" ) );
        OsgiModuleTypeStep step = new OsgiModuleTypeStep( wizardContext, moduleBuilder, OsgiIcons.WIZARD_PANEL, "osgi.createOsgi" );
        steps.add( step );
        ModuleWizardStep[] wizardSteps = steps.toArray( new ModuleWizardStep[steps.size()] );
        return ArrayUtil.mergeArrays( wizardSteps,
                                      super.createWizardSteps( wizardContext, moduleBuilder, modulesProvider ),
                                      ModuleWizardStep.class
        );
    }

    public OsgiModuleBuilder createModuleBuilder()
    {
        return new OsgiModuleBuilder( this );
    }

    public String getName()
    {
        return "OSGi Bundle";
    }

    public String getDescription()
    {
        return "OSGi Bundle type. See http://www.osgi.org for information about OSGi.";
    }

    public Icon getBigIcon()
    {
        return OsgiIcons.ICON_BIG;
    }

    public Icon getNodeIcon( boolean isOpened )
    {
        if( isOpened )
        {
            return OsgiIcons.ICON_OPEN;
        }
        else
        {
            return OsgiIcons.ICON_CLOSE;
        }
    }
}
