package biz.aqute.bnd.plugin.popup.actions;

import biz.aqute.bnd.plugin.Activator;
import biz.aqute.lib.osgi.Jar;
import biz.aqute.lib.osgi.Verifier;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class VerifyBundle
    implements IObjectActionDelegate
{

    private IFile[] locations;

    public VerifyBundle()
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
                    File mf = locations[ i ].getLocation().toFile();
                    try
                    {
                        Jar jar = new Jar( mf.getName(), mf );
                        Verifier verifier = new Verifier( jar );
                        verifier.verify();
                        if( verifier.getErrors().size() + verifier.getWarnings().size() > 0 )
                        {
                            List info = new ArrayList( verifier.getErrors() );
                            info.addAll( verifier.getWarnings() );
                            Activator.getDefault().error( info );
                        }

                    } catch( Exception e )
                    {
                        Activator.getDefault().error( "While verifying JAR " + locations[ i ], e );
                    }
                    locations[ i ].getParent().refreshLocal( 1, null );
                }
            }
        }
        catch( Exception e )
        {
            Activator.getDefault().error( "Could not start verification", e );
        }
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction,ISelection)
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        locations = getLocations( selection );
    }

    IFile[] getLocations( ISelection selection )
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
