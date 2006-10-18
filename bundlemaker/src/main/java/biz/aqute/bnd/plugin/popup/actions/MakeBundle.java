package biz.aqute.bnd.plugin.popup.actions;

import biz.aqute.bnd.plugin.Activator;
import biz.aqute.lib.osgi.Builder;
import biz.aqute.lib.osgi.Jar;
import biz.aqute.lib.osgi.eclipse.EclipseClasspath;
import java.io.File;
import java.util.Iterator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class MakeBundle
    implements IObjectActionDelegate
{

    private IFile[] locations;

    public MakeBundle()
    {
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
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
                        // TODO of course we should get the classpath from
                        // inside API ...
                        IProject project = locations[ i ].getProject();
                        File p = project.getLocation().toFile();

                        // TODO for now we ignore the workspace and use the
                        // project parent directory
                        EclipseClasspath ecp = new EclipseClasspath( p.getParentFile(), p );

                        Builder builder = new Builder();
                        builder.setClasspath( (File[]) ecp.getClasspath().toArray( new File[0] ) );
                        builder.setSourcepath( (File[]) ecp.getSourcepath().toArray( new File[0] ) );
                        builder.setProperties( mf );
                        String path = builder.getProperty( "-output" );
                        if( path == null )
                        {
                            path = mf.getAbsolutePath().replaceAll( "\\.bnd$", ".jar" );
                        }
                        new File( path ).delete();

                        builder.build();
                        Jar jar = builder.getJar();
                        if( builder.getErrors().size() > 0 )
                        {
                            Activator.getDefault().error( builder.getErrors() );
                        }
                        else
                        {
                            jar.write( new File( path ) );
                            if( builder.getWarnings().size() > 0 )
                            {
                                Activator.getDefault().warning( builder.getWarnings() );
                            }
                        }

                    } catch( Exception e )
                    {
                        Activator.getDefault().error( "While generating JAR " + locations[ i ], e );
                    }
                    locations[ i ].getParent().refreshLocal( 1, null );
                }
            }
        }
        catch( Exception e )
        {
            Activator.getDefault().error( "Could not start Test View", e );
        }
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction,ISelection)
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
