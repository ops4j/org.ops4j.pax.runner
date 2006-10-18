package biz.aqute.lib.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Jar
{

    private Map<String,Resource> resources = new TreeMap<String, Resource>();

    private Map<String,Map<String,Resource>> directories = new TreeMap<String, Map<String, Resource>>();

    private Manifest manifest;

    private boolean manifestFirst;

    private String name;

    private File source;

    public Jar( String name )
    {
        this.name = name;
    }

    public Jar( String name, File dirOrFile )
        throws IOException
    {
        this( name );
        source = dirOrFile;
        if( dirOrFile.isDirectory() )
        {
            FileResource.build( this, dirOrFile, Analyzer.doNotCopy );
        }
        else
        {
            ZipResource.build( this, dirOrFile );
        }
    }

    public Jar( String name, InputStream in )
        throws IOException
    {
        this( name );
        EmbeddedResource.build( this, in );
    }

    public Jar( String name, String path )
        throws IOException
    {
        this( name, new FileInputStream( path ) );
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String toString()
    {
        return "Jar:" + name;
    }

    public void putResource( String path, Resource resource )
    {
        if( resources.isEmpty() && "META-INF/MANIFEST.MF".equals( path ) )
        {
            manifestFirst = true;
        }

        resources.put( path, resource );
        String dir = getDirectory( path );
        Map<String,Resource> s = directories.get( dir );
        if( s == null )
        {
            s = new TreeMap<String, Resource>();
            directories.put( dir, s );
        }
        s.put( path, resource );
    }

    public Resource getResource( String path )
    {
        return (Resource) resources.get( path );
    }

    private String getDirectory( String path )
    {
        int n = path.lastIndexOf( '/' );
        if( n < 0 )
        {
            return "";
        }

        return path.substring( 0, n );
    }

    public Map<String,Map<String,Resource>> getDirectories()
    {
        return directories;
    }

    public Map<String,Resource> getResources()
    {
        return resources;
    }

    public void addDirectory( Map<String,Resource> directory )
    {
        for( Map.Entry entry : directory.entrySet() )
        {
            String key = (String) entry.getKey();
            if( !key.endsWith( ".java" ) )
            {
                putResource( key, (Resource) entry.getValue() );
            }

        }
    }

    public Manifest getManifest()
        throws IOException
    {
        if( manifest == null )
        {
            Resource manifestResource = getResource( "META-INF/MANIFEST.MF" );
            if( manifestResource != null )
            {
                manifest = new Manifest( manifestResource.openInputStream() );
            }
        }
        return manifest;
    }

    public boolean exists( String path )
    {
        return resources.containsKey( path );
    }

    public void setManifest( Manifest manifest )
    {
        manifestFirst = true;
        this.manifest = manifest;
    }

    public void write( File file )
        throws IOException
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream( file );
            write( out );
            out.close();
            return;
        } catch( RuntimeException t )
        {
            t.printStackTrace();
        } finally
        {
            out.close();
        }
    }

    public void write( String file )
        throws IOException
    {
        write( new File( file ) );
    }

    public void write( OutputStream out )
        throws IOException
    {
        JarOutputStream jout = new JarOutputStream( out );
        Set<String> done = new HashSet<String>();

        Set<String> directories = new HashSet<String>();
        doManifest( done, jout );
        for( Map.Entry<String, Resource> entry : getResources().entrySet() )
        {
            // Skip metainf contents
            if( !done.contains( entry.getKey() ) )
            {
                writeResource( jout, directories, entry.getKey(), entry.getValue() );
            }
        }
        jout.close();
        out.close();
    }

    private void doManifest( Set<String> done, JarOutputStream jout )
        throws IOException
    {
        JarEntry ze = new JarEntry( "META-INF/MANIFEST.MF" );
        jout.putNextEntry( ze );
        manifest.write( jout );
        jout.closeEntry();
        done.add( ze.getName() );
    }

    private void writeResource( JarOutputStream jout, Set<String> directories,
                                String path, Resource resource )
        throws IOException
    {
        createDirectories( directories, jout, path );
        ZipEntry ze = new ZipEntry( path );
        ze.setMethod( ZipEntry.DEFLATED );
        jout.putNextEntry( ze );
        try
        {
            resource.write( jout );
        } catch( Exception e )
        {
            throw new IllegalArgumentException( "Cannot write resource: " + path
                                                + " " + e
            );
        }
        jout.closeEntry();
    }

    void createDirectories( Set<String> directories, JarOutputStream zip, String name )
        throws IOException
    {
        int index = name.lastIndexOf( '/' );
        if( index > 0 )
        {
            String path = name.substring( 0, index );
            if( directories.contains( path ) )
            {
                return;
            }
            createDirectories( directories, zip, path );
            ZipEntry ze = new ZipEntry( path + '/' );
            zip.putNextEntry( ze );
            zip.closeEntry();
            directories.add( path );
        }
    }

    public String getName()
    {
        return name;
    }

    public boolean doManifestFirst()
    {
        return manifestFirst;
    }
}
