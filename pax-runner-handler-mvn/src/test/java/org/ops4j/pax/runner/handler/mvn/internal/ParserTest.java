/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.handler.mvn.internal;

import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;

public class ParserTest
{

    @Test( expected = MalformedURLException.class )
    public void constructorWithNullPath()
        throws MalformedURLException
    {
        new Parser( null );
    }

    @Test( expected = MalformedURLException.class )
    public void urlStartingWithRepositorySeparator()
        throws MalformedURLException
    {
        new Parser( "!group" );
    }

    @Test( expected = MalformedURLException.class )
    public void urlEndingWithRepositorySeparator()
        throws MalformedURLException
    {
        new Parser( "http://repository!" );
    }

    @Test( expected = MalformedURLException.class )
    public void urlWithRepositoryAndNoGroup()
        throws MalformedURLException
    {
        new Parser( "http://repository!" );
    }

    @Test( expected = MalformedURLException.class )
    public void urlWithoutRepositoryAndNoGroup()
        throws MalformedURLException
    {
        new Parser( "" );
    }

    @Test( expected = MalformedURLException.class )
    public void urlWithRepositoryAndNoArtifact()
        throws MalformedURLException
    {
        new Parser( "http://repository!group" );
    }

    @Test( expected = MalformedURLException.class )
    public void urlWithoutRepositoryAndNoArtifact()
        throws MalformedURLException
    {
        new Parser( "group" );
    }

    @Test
    public void urlWithRepositoryAndGroupArtifact()
        throws MalformedURLException
    {
        Parser parser = new Parser( "http://repository!group/artifact" );
        assertEquals( "Group", "group", parser.getGroup() );
        assertEquals( "Artifact", "artifact", parser.getArtifact() );
        assertEquals( "Version", "LATEST", parser.getVersion() );
        assertEquals( "Type", "jar", parser.getType() );
        assertEquals( "Artifact path", "group/artifact/LATEST/artifact-LATEST.jar", parser.getArtifactPath() );
        assertEquals( "repository", new URL( "http://repository" ), parser.getRepositoryURL() );
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifact()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact" );
        assertEquals( "Group", "group", parser.getGroup() );
        assertEquals( "Artifact", "artifact", parser.getArtifact() );
        assertEquals( "Version", "LATEST", parser.getVersion() );
        assertEquals( "Type", "jar", parser.getType() );
        assertEquals( "Artifact path", "group/artifact/LATEST/artifact-LATEST.jar", parser.getArtifactPath() );
        assertEquals( "repository", null, parser.getRepositoryURL() );
    }

    @Test
    public void urlWithRepositoryAndGroupArtifactVersionType()
        throws MalformedURLException
    {
        Parser parser = new Parser( "http://repository!group/artifact/version/type" );
        assertEquals( "Group", "group", parser.getGroup() );
        assertEquals( "Artifact", "artifact", parser.getArtifact() );
        assertEquals( "Version", "version", parser.getVersion() );
        assertEquals( "Type", "type", parser.getType() );
        assertEquals( "Artifact path", "group/artifact/version/artifact-version.type", parser.getArtifactPath() );
        assertEquals( "repository", new URL( "http://repository" ), parser.getRepositoryURL() );
    }

    @Test
    public void urlWithoutRepositoryAndGroupArtifactVersionType()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact/version/type" );
        assertEquals( "Group", "group", parser.getGroup() );
        assertEquals( "Artifact", "artifact", parser.getArtifact() );
        assertEquals( "Version", "version", parser.getVersion() );
        assertEquals( "Type", "type", parser.getType() );
        assertEquals( "Artifact path", "group/artifact/version/artifact-version.type", parser.getArtifactPath() );
        assertEquals( "repository", null, parser.getRepositoryURL() );
    }

    @Test
    public void urlWithJarRepository()
        throws MalformedURLException
    {
        Parser parser = new Parser( "jar:http://repository/repository.jar!/!group/artifact/0.1.0" );
        assertEquals( "Artifact path", "group/artifact/0.1.0/artifact-0.1.0.jar", parser.getArtifactPath() );
        assertEquals( "repository", new URL( "jar:http://repository/repository.jar!/" ), parser.getRepositoryURL() );
    }

    @Test
    public void snapshotPath()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact/version-SNAPSHOT" );
        assertEquals( "Artifact snapshot path", "group/artifact/version-SNAPSHOT/artifact-version-timestamp-build.jar",
                      parser.getSnapshotPath( "version-SNAPSHOT", "timestamp", "build" )
        );
    }

    @Test
    public void artifactPathWithVersion()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact/version" );
        assertEquals( "Artifact path", "group/artifact/version2/artifact-version2.jar",
                      parser.getArtifactPath( "version2" )
        );
    }

    @Test
    public void versionMetadataPath()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact/version" );
        assertEquals( "Version metadata path", "group/artifact/version2/maven-metadata.xml",
                      parser.getVersionMetadataPath( "version2" )
        );
    }

    @Test
    public void artifactMetadataPath()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact/version" );
        assertEquals( "Artifact metadata path", "group/artifact/maven-metadata.xml",
                      parser.getArtifactMetdataPath()
        );
    }

    @Test
    public void artifactLocalMetadataPath()
        throws MalformedURLException
    {
        Parser parser = new Parser( "group/artifact/version" );
        assertEquals( "Artifact local metadata path", "group/artifact/maven-metadata-local.xml",
                      parser.getArtifactLocalMetdataPath()
        );
    }

}
