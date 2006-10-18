package biz.aqute.bnd.plugin.popup.actions;

import biz.aqute.bnd.plugin.Activator;
import java.util.Iterator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class WrapBundle implements IObjectActionDelegate
{
    private IFile[] locations;

    public WrapBundle()
    {
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run( IAction action )
    {
        try
        {
            if( locations != null )
            {
                for( int i = 0; i < locations.length; i++ )
                {
                    // TODO
                    MessageDialog.openInformation( null, "Not Implemented Yet", "TODO implement wrapping" );
                }
            }
        }
        catch( Exception e )
        {
            Activator.getDefault().error( "Could not start Test View", e );
        }
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction,ISelection)
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        locations = getLocations( selection );
    }

    private IFile[] getLocations( ISelection selection )
    {
        if( selection != null && ( selection instanceof StructuredSelection ) )
        {
            StructuredSelection ss = (StructuredSelection) selection;
            IFile[] result = new IFile[ss.size()];
            int n = 0;
            for( Iterator i = ss.iterator(); i.hasNext(); )
            {
                result[ n++ ] = (IFile) i.next();
            }
            return result;
        }
        return null;
    }

    public void setActivePart( IAction action, IWorkbenchPart targetPart )
    {
	}

}
