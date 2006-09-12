package org.ops4j.pax.runner.state;

import java.io.File;
import org.ops4j.pax.runner.repositories.BundleInfo;

public final class Bundle
{
    private File m_bundleData;
    private BundleState m_TargetState;
    private int m_StartLevel;
    private boolean m_starting;
    private BundleInfo m_info;

    public Bundle( File bundleInfo, int startLevel, BundleState targetState )
    {
        m_bundleData = bundleInfo;
        m_TargetState = targetState;
        m_StartLevel = startLevel;
        m_starting = true;
    }

    public int getStartLevel()
    {
        return m_StartLevel;
    }

    public BundleState getTargetState()
    {
        return m_TargetState;
    }

    public File getBundleData()
    {
        return m_bundleData;
    }

    public void setStarting( boolean starting )
    {
        m_starting = starting;
    }

    public boolean isStarting()
    {
        return m_starting;
    }

    public BundleInfo getInfo()
    {
        return m_info;
    }

    public void setInfo( BundleInfo info )
    {
        m_info = info;
    }
}
