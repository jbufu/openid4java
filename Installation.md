#summary How to install the package
#labels Phase-Deploy

# Installing the package #

For including the openid4java as a dependency using maven, see MavenHowTo.

This page describes manual installation of the openid4java jar and its dependencies. See also the INSTALL file from the official packages.

## OpenID4Java library ##

To make the OpenID4Java library available to a (web) application the following JAR files need to be copied to the application's classpath:
```
openid4java-*.jar
```

## Library dependencies ##

  * **lib/`*`jar : Required.** Core OpenID4Java library dependencies.

  * **lib/optional/`*`.jar : Optional.** Libraries supporting alternative deployments.

  * **lib/extra/`*`.jar : Optional.** Extra/development libraries, not needed for deployment (JUnit tests, Jetty servlet container, SVN/Ant utilities)

  * **lib/xri/`*`.jar : Optional.** Local OpenXRI resolver dependencies. Included only in the "openid4java-xri" and "openid4java-full" packages. (A dependency-less proxy XRI resolver is included in the standard package)

  * **lib/infocard/`*`.jar : Optional.** OpenID-Infocards/Higgins STS dependencies. Included only in the "openid4java-infocard" and "openid4java-full" packages.

## See Also ##

RelyingPartyDiscovery