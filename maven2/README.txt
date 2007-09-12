OpenID4Java Maven2 Scripts

OpenID4Java is using ant(http://ant.apache.org/) to build.
The maven2(http://maven.apache.org/) scripts are the another choice for maven users.


Sub projects:

openid4java:
	All of openid4java.

openid4java-consumer:
	OpenID consumer.

openid4java-consumer-SampleConsumer:
	Sample code of consumer.

openid4java-infocard:
	Infocard support.

openid4java-server:
	OpenID server.

openid4java-server-JdbcServerAssociationStore:
	An association store implementention with springframework(http://www.springframework.org/) jdbc.

openid4java-server-SampleServer:
	Sample code of server.


Build:
$ mvn package

Install to your local repository:
$ mvn install
As default, openid4java will be installed into ~/.m2/repository/org/openid4java/.

Generate site documentation(contains javadoc etc):
$ mvn site
Generates site documentation in target/site/.

Clean up:
$ mvn clean


Use it by adding following to your project's pom.xml in section dependencies:
<dependency>
  <groupId>org.openid4java</groupId>
  <artifactId>openid4java</artifactId>
  <!-- Please change the version number to current. -->
  <version>0.9.3</version>
</dependency>
You can use a sub project if you only need a part of it:
<dependency>
  <groupId>org.openid4java</groupId>
  <artifactId>openid4java-consumer</artifactId>
  <!-- Please change the version number to current. -->
  <version>0.9.3</version>
</dependency>
or
<dependency>
  <groupId>org.openid4java</groupId>
  <artifactId>openid4java-server</artifactId>
  <!-- Please change the version number to current. -->
  <version>0.9.3</version>
</dependency>
...
<dependency>
  <groupId>org.openid4java</groupId>
  <artifactId>openid4java-infocard</artifactId>
  <!-- Please change the version number to current. -->
  <version>0.9.3</version>
</dependency>
