package biz.aqute.bnd.plugin;

import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

    // The plug-in ID
    public static final String PLUGIN_ID = "biz.bmaker";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator()
    {
        plugin = this;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
      */
    public void start( BundleContext context )
        throws Exception
    {
        super.start( context );
    }

    /*
      * (non-Javadoc)
      *
      * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
      */
    public void stop( BundleContext context )
        throws Exception
    {
        plugin = null;
        super.stop( context );
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     *
     * @param path the path
     *
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor( String path )
    {
        return imageDescriptorFromPlugin( PLUGIN_ID, path );
    }

    public void error( String msg, Throwable t )
    {
        Status s = new Status( Status.ERROR, PLUGIN_ID, 0, msg, t );
        getLog().log( s );
    }

    public void info( String msg )
    {
        Status s = new Status( Status.INFO, PLUGIN_ID, 0, msg, null );
        getLog().log( s );
    }

    public void error( List errors )
    {
        StringBuffer sb = new StringBuffer();
        for( Iterator i = errors.iterator(); i.hasNext(); )
        {
            String msg = (String) i.next();
            sb.append( msg );
            sb.append( "\r\n" );
        }
        Status s = new Status( Status.ERROR, PLUGIN_ID, 0, "", null );
        ErrorDialog.openError( null, "Errors during bundle generation", sb.toString(), s );
    }

    public void warning( List errors )
    {
        StringBuffer sb = new StringBuffer();
        for( Iterator i = errors.iterator(); i.hasNext(); )
        {
            String msg = (String) i.next();
            sb.append( msg );
            sb.append( "\r\n" );
        }
        Status s = new Status( Status.WARNING, PLUGIN_ID, 0, "", null );
        ErrorDialog.openError( null, "Warnings during bundle generation", sb.toString(), s );
	}
}
