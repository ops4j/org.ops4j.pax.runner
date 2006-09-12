package org.ops4j.pax.runner.state;

import org.ops4j.pax.runner.repositories.BundleInfo;

public final class Bundle
{

    private BundleState m_TargetState;
    private int m_StartLevel;
    private BundleInfo m_bundleInfo;

    public Bundle( BundleInfo bundleInfo )
    {
        this( bundleInfo, 3 );
    }

    public Bundle( BundleInfo bundleInfo, int startLevel )
    {
        this( bundleInfo, startLevel, BundleState.START );
    }

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
