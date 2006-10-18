package org.ops4j.pax.runner.state;

import org.ops4j.pax.model.bundles.BundleModel;

public final class Bundle
{

    private BundleState m_TargetState;
    private int m_StartLevel;
    private BundleModel m_bundleModel;

    public Bundle( BundleModel bundleInfo )
    {
        this( bundleInfo, 3 );
    }

    public Bundle( BundleModel bundleInfo, int startLevel )
    {
        this( bundleInfo, startLevel, BundleState.START );
    }

    public Bundle( BundleModel bundleInfo, int startLevel, BundleState targetState )
    {
        m_bundleModel = bundleInfo;
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

    public BundleModel getBundleModel()
    {
        return m_bundleModel;
    }
}
