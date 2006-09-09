package org.ops4j.pax.runner;

import java.io.File;

public final class Bundle 
{
	private BundleState m_TargetState;
	private File m_File;
	private int m_StartLevel;
	public Bundle(File file, int startLevel, BundleState targetState )
	{
		m_TargetState = targetState;
		m_File = file;
		m_StartLevel = startLevel;
	}
	
	public File getFile()
	{
		return m_File;
	}
	
	public int getStartLevel()
	{
		return m_StartLevel;
	}

	public BundleState getTargetState() 
	{
		return m_TargetState;
	}
}
