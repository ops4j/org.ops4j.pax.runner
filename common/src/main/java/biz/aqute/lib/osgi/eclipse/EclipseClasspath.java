package biz.aqute.lib.osgi.eclipse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parse the Eclipse project information for the classpath. Unfortunately, it is
 * impossible to read the variables. They are ignored but that can cause
 * problems.
 *
 * @version $Revision$
 */
public class EclipseClasspath
{

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder db;
    private File workspace;
    private List sources = new ArrayList();
    private List classpath = new ArrayList();
    private List dependents = new ArrayList();
    private File output;

    /**
     * Parse an Eclipse project structure to discover the classpath.
     *
     * @param workspace Points to workspace
     * @param project   Points to project
     * @throws java.io.IOException if IO problem occurs
     * @throws org.xml.sax.SAXException if XML format errors found.
     */

    public EclipseClasspath( File workspace, File project )
        throws SAXException, IOException
    {
        this.workspace = workspace;
        try
        {
            db = documentBuilderFactory.newDocumentBuilder();
        } catch( ParserConfigurationException e )
        {
            e.printStackTrace();  //Should not be possible
        }
        parse( project, true );
        db = null;
    }

    /**
     * Recursive routine to parse the files. If a sub project is detected, it is
     * parsed before the parsing continues. This should give the right order.
     *
     * @param project Project directory
     * @param top     If this is the top project
     * @throws java.io.IOException if IO problem occurs
     * @throws org.xml.sax.SAXException if XML format errors found.
     */
    void parse( File project, boolean top )
        throws SAXException, IOException
    {
        File file = new File( project, ".classpath" );
        if( !file.exists() )
        {
            throw new IllegalArgumentException( ".classpath file not found: " + file.getAbsolutePath() );
        }
        Document doc = db.parse( new File( project, ".classpath" ) );
        NodeList nodelist = doc.getDocumentElement().getElementsByTagName( "classpathentry" );

        if( nodelist == null )
        {
            throw new IllegalArgumentException( "Can not find classpathentry in classpath file" );
        }

        for( int i = 0; i < nodelist.getLength(); i++ )
        {
            Node node = nodelist.item( i );
            NamedNodeMap attrs = node.getAttributes();
            String kind = get( attrs, "kind" );
            if( "src".equals( kind ) )
            {
                String path = get( attrs, "path" );
                boolean exported = "true".equalsIgnoreCase( get( attrs, "exported" ) );
                if( path.startsWith( "/" ) )
                {
                    // We have another project
                    path = path.replace( '/', File.separatorChar );
                    File subProject = new File( workspace, path.substring( 1 ) );
                    dependents.add( subProject );
                    if( top || exported )
                    {
                        parse( subProject, false );
                    }
                }
                else
                {
                    File src = new File( project, path );
                    sources.add( src );
                }
            }
            else if( "lib".equals( kind ) )
            {
                String path = get( attrs, "path" );
                boolean exported = "true".equalsIgnoreCase( get( attrs, "exported" ) );
                if( top || exported )
                {
                    File jar = null;
                    path = path.replace( '/', File.separatorChar );
                    if( path.startsWith( File.separator ) )
                    {
                        jar = new File( workspace, path.substring( 1 ) );
                    }
                    else
                    {
                        jar = new File( project, path );
                    }
                    classpath.add( jar );
                }
            }
            else if( "output".equals( kind ) )
            {
                String path = get( attrs, "path" );
                path = path.replace( '/', File.separatorChar );
                output = new File( project, path );
                classpath.add( output );
            }
        }
    }

    private String get( NamedNodeMap map, String name )
    {
        Node node = map.getNamedItem( name );
        if( node == null )
        {
            return null;
        }

        return node.getNodeValue();
    }

    public List getClasspath()
    {
        return classpath;
    }

    public List getSourcepath()
    {
        return sources;
    }

    public File getOutput()
    {
        return output;
    }

    public List getDependents()
    {
        return dependents;
    }
}
