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
package org.ops4j.pax.cursor.ui;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class ProvisionURL
{

    private String m_url;
    private boolean m_selected;
    private boolean m_start;
    private Integer m_startLevel;
    private boolean m_update;

    public ProvisionURL()
    {
        m_selected = true;
        m_start = true;
        m_update = false;
    }

    public ProvisionURL( final String url, final boolean selected, boolean start, final Integer startLevel,
                         final boolean update )
    {
        m_url = url;
        m_selected = selected;
        m_start = start;
        m_startLevel = startLevel;
        m_update = update;
    }

    public String getUrl()
    {
        return m_url;
    }

    public void setUrl( String url )
    {
        this.m_url = url;
    }

    public boolean isSelected()
    {
        return m_selected;
    }

    public void setSelected( boolean selected )
    {
        this.m_selected = selected;
    }

    public boolean isStart()
    {
        return m_start;
    }

    public void setStart( boolean start )
    {
        this.m_start = start;
    }

    public Integer getStartLevel()
    {
        return m_startLevel;
    }

    public void setStartLevel( Integer startLevel )
    {
        this.m_startLevel = startLevel;
    }

    public boolean isUpdate()
    {
        return m_update;
    }

    public void setUpdate( boolean update )
    {
        this.m_update = update;
    }

}
