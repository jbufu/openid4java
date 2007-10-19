
************
INTRODUCTION
************

This is a demo consumer and provider using only JSPs. It uses only
in-memory stores for nonces and associations, and the provider does 
no user authentication, though it should be obvious in provider.jsp
how to do user authentication using standard web authentication 
methods.

This demo *requires* apache Maven2 to build. 

The index.jsp and consumer_*.jsp files comprise the consumer. 

The provider_*.jsp files comprise the provider. 

The user.jsp produces a XRDS file which points to the provider.

There are hardcoded URLs in each of the JSP files - a README at the 
top indicates what you need to change to deploy this to other than
http://localhost:8080/simple-openid 

There are no dependencies between consumer and provider. 

To install these JSPs in another project/jsp container, you should
make sure to see the pom.xml file which documents dependencies,
and note the xalan files which must go in an endorsed libs 
directory (xercesImpl*.jar and xml-apis*.jar)

The mvn war:war task should create a war file which can be deployed 
by copying the war file - but this does not deploy the above mentioned
jars into an endorsed lib directory (such as $CATALINA_HOME/common/endorsed)
 
****************
RUNNING THE DEMO
****************
To run this as a demo, install the maven2 tool and run the following
command in the simple-openid directory:
mvn jetty:run 

DEMO CONSUMER
-------------
Visit the demo consumer at:
http://localhost:8080/simple-openid

You can use an IName, or any HTTP URL which acts as an openid. 

DEMO PROVIDER
-------------
To visit the demo provider, visit the consumer at:
http://localhost:8080/simple-openid

And login with the following URL:
http://localhost:8080/simple-openid/user.jsp

You'll be prompted to approve the OP request (no authenticatinon)

*******
LOGGING
*******
You can tweak the incldued log4j.properties file and instruct maven
to run the demo with various levels of logging. Example:

mvn -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger
    -Dlog4j.configuration=file:log4j.properties jetty:run

****
TODO
****
Lots. 

Error handling is almost nonexistent. 

Authentication of the user is an excercise left to the reader. 

There are probably better ways to manage the ConsumerManager and
ServerManager objects.

In general, the JSPs are rather brittle.  
