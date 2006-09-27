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
package org.ops4j.pax.runner.idea.module;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class OsgiModuleTypeForm
{
    private JComponent m_wholePanel;
    private JTextField m_category;
    private JTextField m_symbolicName;
    private JTextField m_bundleName;
    private JTextField m_vendor;
    private JTextField m_copyright;
    private JTextField m_contactAddress;
    private JTextField m_license;
    private JTextField m_activator;
    private JTextField m_updateLocation;
    private JTextField m_docUrl;
    private JTextField m_version;
    private JTextArea m_description;

    public void setData( DataBeanManifest data )
    {
        m_category.setText( data.getCategory() );
        m_symbolicName.setText( data.getSymbolicName() );
        m_bundleName.setText( data.getBundleName() );
        m_vendor.setText( data.getVendor() );
        m_copyright.setText( data.getCopyright() );
        m_contactAddress.setText( data.getContactAddress() );
        m_license.setText( data.getLicense() );
        m_activator.setText( data.getActivator() );
        m_updateLocation.setText( data.getUpdateLocation() );
        m_docUrl.setText( data.getDocUrl() );
        m_version.setText( data.getVersion() );
        m_description.setText( data.getDescription() );
    }

    public void getData( DataBeanManifest data )
    {
        data.setCategory( m_category.getText() );
        data.setSymbolicName( m_symbolicName.getText() );
        data.setBundleName( m_bundleName.getText() );
        data.setVendor( m_vendor.getText() );
        data.setCopyright( m_copyright.getText() );
        data.setContactAddress( m_contactAddress.getText() );
        data.setLicense( m_license.getText() );
        data.setActivator( m_activator.getText() );
        data.setUpdateLocation( m_updateLocation.getText() );
        data.setDocUrl( m_docUrl.getText() );
        data.setVersion( m_version.getText() );
        data.setDescription( m_description.getText() );
    }

    public boolean isModified( DataBeanManifest data )
    {
        if( m_category.getText() != null
            ? !m_category.getText().equals( data.getCategory() )
            : data.getCategory() != null )
        {
            return true;
        }
        if( m_symbolicName.getText() != null
            ? !m_symbolicName.getText().equals( data.getSymbolicName() )
            : data.getSymbolicName() != null )
        {
            return true;
        }
        if( m_bundleName.getText() != null
            ? !m_bundleName.getText().equals( data.getBundleName() )
            : data.getBundleName() != null )
        {
            return true;
        }
        if( m_vendor.getText() != null ? !m_vendor.getText().equals( data.getVendor() ) : data.getVendor() != null )
        {
            return true;
        }
        if( m_copyright.getText() != null
            ? !m_copyright.getText().equals( data.getCopyright() )
            : data.getCopyright() != null )
        {
            return true;
        }
        if( m_contactAddress.getText() != null
            ? !m_contactAddress.getText().equals( data.getContactAddress() )
            : data.getContactAddress() != null )
        {
            return true;
        }
        if( m_license.getText() != null ? !m_license.getText().equals( data.getLicense() ) : data.getLicense() != null )
        {
            return true;
        }
        if( m_activator.getText() != null
            ? !m_activator.getText().equals( data.getActivator() )
            : data.getActivator() != null )
        {
            return true;
        }
        if( m_updateLocation.getText() != null
            ? !m_updateLocation.getText().equals( data.getUpdateLocation() )
            : data.getUpdateLocation() != null )
        {
            return true;
        }
        if( m_docUrl.getText() != null ? !m_docUrl.getText().equals( data.getDocUrl() ) : data.getDocUrl() != null )
        {
            return true;
        }
        if( m_version.getText() != null ? !m_version.getText().equals( data.getVersion() ) : data.getVersion() != null )
        {
            return true;
        }
        if( m_description.getText() != null
            ? !m_description.getText().equals( data.getDescription() )
            : data.getDescription() != null )
        {
            return true;
        }
        return false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     */
    private void $$$setupUI$$$()
    {
        m_wholePanel = new JPanel();
        m_wholePanel.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        m_wholePanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), null ) );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new GridLayoutManager( 2, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        m_wholePanel.add( panel1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                       GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                  | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                  | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                  null, null, null, 0, false
        )
        );
        final JPanel panel2 = new JPanel();
        panel2.setLayout( new GridLayoutManager( 11, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel2, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$( label1,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.symbolicname"
                                  )
        );
        panel2.add( label1, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$( label2,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.bundlename"
                                  )
        );
        panel2.add( label2, new GridConstraints( 2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$( label3,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.vendor"
                                  )
        );
        panel2.add( label3, new GridConstraints( 4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$( label4,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.contactaddress"
                                  )
        );
        panel2.add( label4, new GridConstraints( 6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$( label5,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.updatelocation"
                                  )
        );
        panel2.add( label5, new GridConstraints( 9, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$( label6,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.docurl"
                                  )
        );
        panel2.add( label6, new GridConstraints( 10, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label7 = new JLabel();
        this.$$$loadLabelText$$$( label7,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.bundlecategory"
                                  )
        );
        panel2.add( label7, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label8 = new JLabel();
        this.$$$loadLabelText$$$( label8,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.bundleactivator"
                                  )
        );
        panel2.add( label8, new GridConstraints( 8, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label9 = new JLabel();
        this.$$$loadLabelText$$$( label9,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.copyright"
                                  )
        );
        panel2.add( label9, new GridConstraints( 5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label10 = new JLabel();
        this.$$$loadLabelText$$$( label10,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.license"
                                  )
        );
        panel2.add( label10, new GridConstraints( 7, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                  null, null, null, 0, false
        )
        );
        m_category = new JTextField();
        panel2.add( m_category, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                     GridConstraints.FILL_HORIZONTAL,
                                                     GridConstraints.SIZEPOLICY_WANT_GROW,
                                                     GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                     null, 0, false
        )
        );
        m_symbolicName = new JTextField();
        panel2.add( m_symbolicName, new GridConstraints( 1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                         GridConstraints.FILL_HORIZONTAL,
                                                         GridConstraints.SIZEPOLICY_WANT_GROW,
                                                         GridConstraints.SIZEPOLICY_FIXED, null,
                                                         new Dimension( 150, -1 ), null, 0, false
        )
        );
        m_bundleName = new JTextField();
        panel2.add( m_bundleName, new GridConstraints( 2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                       GridConstraints.FILL_HORIZONTAL,
                                                       GridConstraints.SIZEPOLICY_WANT_GROW,
                                                       GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                       null, 0, false
        )
        );
        m_vendor = new JTextField();
        panel2.add( m_vendor, new GridConstraints( 4, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                   GridConstraints.FILL_HORIZONTAL,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW,
                                                   GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                   null, 0, false
        )
        );
        m_copyright = new JTextField();
        panel2.add( m_copyright, new GridConstraints( 5, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                      null, 0, false
        )
        );
        m_contactAddress = new JTextField();
        panel2.add( m_contactAddress, new GridConstraints( 6, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                           GridConstraints.FILL_HORIZONTAL,
                                                           GridConstraints.SIZEPOLICY_WANT_GROW,
                                                           GridConstraints.SIZEPOLICY_FIXED, null,
                                                           new Dimension( 150, -1 ), null, 0, false
        )
        );
        m_license = new JTextField();
        panel2.add( m_license, new GridConstraints( 7, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                    GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_WANT_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                    null, 0, false
        )
        );
        m_activator = new JTextField();
        panel2.add( m_activator, new GridConstraints( 8, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                      null, 0, false
        )
        );
        m_updateLocation = new JTextField();
        panel2.add( m_updateLocation, new GridConstraints( 9, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                           GridConstraints.FILL_HORIZONTAL,
                                                           GridConstraints.SIZEPOLICY_WANT_GROW,
                                                           GridConstraints.SIZEPOLICY_FIXED, null,
                                                           new Dimension( 150, -1 ), null, 0, false
        )
        );
        m_docUrl = new JTextField();
        panel2.add( m_docUrl, new GridConstraints( 10, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                   GridConstraints.FILL_HORIZONTAL,
                                                   GridConstraints.SIZEPOLICY_WANT_GROW,
                                                   GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                   null, 0, false
        )
        );
        final JLabel label11 = new JLabel();
        this.$$$loadLabelText$$$( label11,
                                  ResourceBundle.getBundle( "org/ops4j/pax/runner/idea/OsgiResourceBundle" ).getString(
                                      "manifest.version"
                                  )
        );
        panel2.add( label11, new GridConstraints( 3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                  null, null, null, 0, false
        )
        );
        m_version = new JTextField();
        panel2.add( m_version, new GridConstraints( 3, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                    GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_WANT_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                    null, 0, false
        )
        );
        final JPanel panel3 = new JPanel();
        panel3.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel3, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_WANT_GROW, null,
                                                                                                                   null,
                                                                                                                   null,
                                                                                                                   0,
                                                                                                                   false
        )
        );
        panel3.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(),
                                                            ResourceBundle.getBundle(
                                                                "org/ops4j/pax/runner/idea/OsgiResourceBundle"
                                                            ).getString( "manifest.bundledescription" )
        )
        );
        m_description = new JTextArea();
        panel3.add( m_description, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                        GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW, null,
                                                        new Dimension( 150, 50 ), null, 0, false
        )
        );
    }

    private void $$$loadLabelText$$$( JLabel component, String text )
    {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = ' ';
        int mnemonicIndex = -1;
        for( int i = 0; i < text.length(); i++ )
        {
            if( text.charAt( i ) == '&' )
            {
                i++;
                if( i == text.length() ) break;
                if( !haveMnemonic && text.charAt( i ) != '&' )
                {
                    haveMnemonic = true;
                    mnemonic = text.charAt( i );
                    mnemonicIndex = result.length();
                }
            }
            result.append( text.charAt( i ) );
        }
        component.setText( result.toString() );
        if( haveMnemonic )
        {
            component.setDisplayedMnemonic( mnemonic );
            component.setDisplayedMnemonicIndex( mnemonicIndex );
        }
    }

    public JComponent $$$getRootComponent$$$()
    {
        return m_wholePanel;
    }
}
