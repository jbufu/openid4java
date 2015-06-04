# Official Release #

Add following to your project's pom.xml in the `<dependencies>` section:

```
<dependency>
    <groupId>org.openid4java</groupId>
    <artifactId>openid4java</artifactId>
    <version>0.9.7</version>
</dependency>
```

# Snapshots #

If you want to try the SNAPSHOT version, add the following to your pom.xml:

```
<project ...>
    ...
    <repositories>
        <repository>
             <id>openid4java snapshots repository</id>
             <name>openid4java snapshots repository</name>
             <url>https://oss.sonatype.org/content/repositories/openid4java-snapshots</url>
             <snapshots />
        </repository>
    </repositories>
    ...
    <dependencies>
        ...
        <dependency>
            <groupId>org.openid4java</groupId>
            <artifactId>openid4java</artifactId>
	    <version>0.9.8-SNAPSHOT</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
```