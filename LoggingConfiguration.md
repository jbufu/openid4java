# Logging Configuration #

OpenID4Java uses Apache's commons-logging API. This allows the
application developers to choose (or change) their preferred logging
implementations, without the need to modify or recompile the library.

> http://commons.apache.org/logging/guide.html#Configuration

## Log4J configuration ##

Log4J v1.2 is used as the logging implementation for the sample
projects included with the OpenID4Java library. Commons-logging
uses Log4J as the primary default and automatically detects if
Log4J is available when discovering available logging
implementations.

A typical way to configure Log4J is using a log4j.properties file
in the classpath which should contain the following (adjust log
levels as desired):

```
        log4j.rootLogger=INFO, A1
        log4j.logger.org.openid4java=DEBUG, A1
        log4j.appender.A1=org.apache.log4j.ConsoleAppender
        log4j.appender.A1.layout=org.apache.log4j.PatternLayout
```

http://logging.apache.org/log4j/1.2/
