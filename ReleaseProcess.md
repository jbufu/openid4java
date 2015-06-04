Official OpenID4Java packages can be downloaded from the projects download page:
> http://code.google.com/p/openid4java/downloads/

The packaging is done with Ant, which is also the officially supported method for building the library from source. Maven is also provided as an alternate building and installation method.

The release process is documented below in order to provide for a consistent package.

# Release Process #

  * work on **x.a-SNAPSHOT**
  * **Preparation**:
    * **project.properties:** change the version on local copy from **x.a-SNAPSHOT** to **x.a**
    * **ant sync-pom-version** to update version number in every pom.xml under maven2/
    * check for version in other POM files possibly missed by sync-pom-version
    * update CHANGELOG
  * **Testing and Packaging**:
    * commit changes and **svn update** (important for updating the last modification revision which is used as part of the ant package name and VERSION file inside the package)
    * test and package the artifact
      * **clean sub-projects**: ant's 'build' folders, maven's 'target' folders, etc.
      * **ant:** trunk/: "ant clean test release" --> 4x openid4java-<dist.type>-x.a.tar.gz
      * **maven:** build openid4java and the samples using maven
        * trunk/: "mvn clean install"  --> target/openid4java-x.a.jar
        * trunk/samples/: "mvn clean package"
    * if there are local changes, restart **Testing and Packaging**
  * **Tagging and Publishing**:
    * tag the version on SVN: **tags/openid4java-x.a/**
    * publish the artifacts
      * **ant**: java-openid-x.a.tar.gz --> http://code.google.com/p/openid4java/downloads
      * **maven**:
        * **mvn clean deploy -Prelease** deploys target/openid4java-x.a`*`.jar to Sonatype OSS
        * [promote](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-8a.ReleaseIt) from Sonatye OSS to Maven Central
    * announce new release
  * **Post Cleanup**:
    * **project.properties**: change/increment the version from **x.a** to **x.b-SNAPSHOT**
    * **ant sync-pom-version** to update version number in every pom.xml under maven2/
    * commit changes
  * work on **x.b-SNAPSHOT**
  * ...