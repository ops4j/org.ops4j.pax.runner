package org.ops4j.pax.runner.state;

import org.ops4j.pax.runner.repositories.BundleInfo;

public final class Bundle
{

    private BundleInfo m_bundleInfo;
    private BundleState m_TargetState;
    private int m_StartLevel;

    public Bundle( BundleInfo bundleInfo, int startLevel, BundleState targetState )
    {
        m_bundleInfo = bundleInfo;
        m_TargetState = targetState;
        m_StartLevel = startLevel;
    }

    public int getStartLevel()
    {
        return m_StartLevel;
    }

    public BundleState getTargetState()
    {
        return m_TargetState;
    }

    public BundleInfo getBundleInfo()
    {
        return m_bundleInfo;
    }
}
