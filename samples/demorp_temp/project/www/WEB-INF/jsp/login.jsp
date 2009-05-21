<%--
  ~ Copyright 2006-2008 Sxip Identity Corporation
  --%>

<%@ page import="java.util.Calendar"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="stylesheet" href="<c:out value='${pageContext.request.contextPath}'/>/style.css" type="text/css"
          media="screen" title="no title" charset="utf-8"/>

    <title>${title}</title>
    <script type="text/javascript">
        <!--

        function areCardsSupported()
        {
            var IEVer = -1;

            if (navigator.appName == 'Microsoft Internet Explorer')
                if (new RegExp("MSIE ([0-9]{1,}[\\.0-9]{0,})").exec(navigator.userAgent) != null)
                    IEVer = parseFloat(RegExp.$1);

            if (IEVer >= 6)
            {
                var embed = document.createElement("object");

                embed.setAttribute("type", "application/x-informationcard");

                return "" + embed.issuerPolicy != "undefined";
            }

            if (IEVer < 0 && navigator.mimeTypes && navigator.mimeTypes.length)
            {
                var x = navigator.mimeTypes['application/x-informationcard'];

                if (x && x.enabledPlugin)
                    return true;

                var event = document.createEvent("Events");
                event.initEvent("IdentitySelectorAvailable", true, true);
                top.dispatchEvent(event);

                if (top.IdentitySelectorAvailable == true)
                    return true;
            }

            return false;
        }

        //        document.write ("<style>body{display: none} div{display: none} p{display: none} input{display: none}</style>");
        //-->
    </script>
</head>

<body onload="document.forms['openid-login'].elements['openid_identifier'].focus();">
<div id="wrapper">
    <div id="header">
        <img src="<c:out value='${pageContext.request.contextPath}'/>/images/openid_information_cards_demo.png" alt="logo" title=""/>
    </div>

    <div id="body">
        <c:if test="${not empty message}">
            <div id="message">
                <p>
                    <c:out value="${message}"/>
                </p>
            </div>
        </c:if>

        <div id="content">
            <p>
                <c:out value="${contents}" escapeXml="false"/>

            </p>
        </div>

        <div class="form">

            <form name="openid-login" action="<c:out value='${pageContext.request.contextPath}'/>/" method="post">
                <table>
                    <tr>
                        <td>
                            <input type="text" name="openid_identifier" value="<c:out value='${openid_identifier}'/>" class="openid"/>
                        </td>
                        <td>
                            <button type="submit">Login</button>
                        </td>
                    </tr>
                </table>
            </form>

<%--
            <form name="infocard" method="post" action="./infocard"
                  id="infocard" enctype="application/x-www-form-urlencoded">

                <object type="application/x-informationCard" name="xmlToken">
                    <param name="tokenType" value="http://specs.openid.net/auth/2.0" />
                    <param name="requiredClaims" Value="http://schema.openid.net/2007/05/claims/identifier">
                </object>

                <button type="submit">Login with an InfoCard</button>

            </form>
--%>
                
            <form name='infocard' method='post' action="<c:out value='${pageContext.request.contextPath}'/>/"
                  id='infocard' enctype='application/x-www-form-urlencoded'>

                <div id="infocardSupport" style="display:none">
                    <p>
                        It appears that your browser does not support Information Cards!
                    </p>
                    <br />
                </div>
                <p>Login with an OpenID-InfoCard:</p>
            <img src="./images/openid_infocard.png"
                 onClick="infocard.submit()"/>

                <OBJECT type="application/x-informationCard" name="xmlToken">
                    <PARAM Name="tokenType" Value="http://specs.openid.net/auth/2.0">
                    <PARAM Name="requiredClaims" Value="http://schema.openid.net/2007/05/claims/identifier">
                    <param name="optionalClaims" Value="http://axschema.org/contact/email http://axschema.org/namePerson/first http://axschema.org/namePerson/last http://axschema.org/contact/phone/default http://axschema.org/contact/postalAddress/home http://axschema.org/contact/city/home http://axschema.org/contact/postalCode/home http://axschema.org/contact/country/home http://axschema.org/contact/web/blog">
                </OBJECT>
            </form>

        </div>

        <c:if test="${not empty contents2}">
          <div id="footnote">
          <p>
            <c:out value="${contents2}" escapeXml="false"/>
          </p>
          </div>
        </c:if>

    </div>


    <div id="footer">
    <ul>
     <li class="footer_contact"><a href="http://nym.sxip.com/nymail/sendmail.pl?a=2a89335f448c01ac"
       target="_blank">Contact</a></li>
    </ul>
    <div class="footer_copyright">&copy; 2006-2008 <a href="http://www.sxip.com/">Sxip Identity Corporation.</a></div>
    </div>
</div>
<script type="text/javascript">
    <!--

    if (!areCardsSupported())
    {
        var p = document.getElementById('infocardSupport');

        p.style.display = "block";
    }

    //-->
</script>
</body>
</html>
