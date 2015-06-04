![http://openid4java.googlecode.com/svn/trunk/openid4java.png](http://openid4java.googlecode.com/svn/trunk/openid4java.png)
[English](http://code.google.com/p/openid4java/)


# 这个开发包是什么？ #

这个开发包可以让你在你的 Java web 应用程序中启用 OpenID 支持。

目前本开发包支持下面的规范：

  * [OpenID Authentication 2.0](http://openid.net/specs/openid-authentication-2_0.html)
  * [OpenID Authentication 1.1（兼容模式）](http://openid.net/specs/openid-authentication-1_1.html)
  * [OpenID Attribute Exchange 1.0](http://openid.net/specs/openid-attribute-exchange-1_0.html)
  * [OpenID Simple Registration 1.0](http://openid.net/specs/openid-simple-registration-extension-1_0.html) and [1.1](http://openid.net/specs/openid-simple-registration-extension-1_1-01.html)
  * [OpenID Provider Authentication Policy Extension 1.0, draft 1](http://openid.net/specs/openid-provider-authentication-policy-extension-1_0-01.html)
  * [OpenID Information Cards 1.0, draft 1](http://openidcards.sxip.com/spec/openid-infocards.html)

# OpenID 是什么？ #

OpenID 基于如下思想：任何人都可以在互联网上用一个 URI 来认证他们自己。基于 URI 是 Web 体系结构的核心，提供了一个可靠的以用户为中心的认证基础。

OpenID 框架的第一个部分就是认证——如何证实你对一个 URI 的拥有所有权。现在，一些网站使用用户名和密码来登录，也就是说很多人在很多地方使用相同的密码。如果使用 OpenID 认证，你的用户名就是你的 URI，而你的密码（或者是认证资料）则安全地存放在你的 OpenID 服务器（提供商）那里（你可以假设自己的 OpenID 服务器，或者使用第三方的认证提供商提供的服务）。

想了解更多关于 OpenID 协议，请参考技术规范： http://openid.net/specs.bml
# 快速上手 #

要在你的站点启用 OpenID 支持 —— a Relying Party (RP) in OpenID terms, 或 Consumer at code-level, 你需要按照如下所示来做：

  * [安装](Installation.md) OpenID4Java 开发包

  * 获取用户的 OpenID（URL）标识而不是询问他们用户名／密码对

  * 为指定的身份标识创建一个认证请求，并重定向到用户的 OpenID 服务器（提供商）

  * 接受 OpenID 服务器的认证返回（参照 ReturnURL），并验证返回信息。

去[快速上手](QuickStart_zh_CN.md)看看，这里有一些完整的代码示例。

# 一个简单的例子 #
在源码的 samples/simple-openid/ 下有一个非常简单的 OpenID 服务端和客户端的例子。使用下面的 maven 命令来快速运行它：
```
mvn jetty:run
```
然后访问 http://localhost:8080/simple-openid/
要了解更多详细信息可以参考这个目录下的README.txt。

# 下载 #

如果你需要一个已经编译好了的二进制包，可以到下列网址下载 OpenID4Java 开发包： http://code.sxip.com

你也可以从源码库中[取出代码](http://code.google.com/p/openid4java/source)然后自己[编译](BuildFromSource.md)。
# 参见 #

[必需环境](Requirements.md)

[安装](Installation.md)

[文档](Documentation.md)

使用示例和代码： [SampleConsumer](SampleConsumer.md) [SampleServer](SampleServer.md)