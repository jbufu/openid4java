<%@ page import="java.util.Calendar"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="stylesheet" href="<c:out value='${pageContext.request.contextPath}'/>/style.css" type="text/css"
          media="screen" title="no title" charset="utf-8"/>

    <title>${title}</title>

    <style type="text/css" xml:space="preserve">
        #assertion
        {
            display:none;
            font-size: 9pt;
        }
    </style>

    <script type="text/javascript" xml:space="preserve">
        function toggleAssertion()
        {
            var a = document.getElementById('assertion');

            if (a.style.display != 'block')
                a.style.display = 'block';
            else
                a.style.display = 'none';
        }
    </script>
</head>

<body>
<div id="wrapper">
    <div id="header">
        <img src="${pageContext.request.contextPath}/images/logo.gif" alt="logo" title=""/>
    </div>

    <div id="body">
        <p>Welcome! You have logged in using your <b>${identifier}</b> OpenID identifier.</p>

        <table cellpadding="5">

      <c:forEach var="parameter" items="${attributes}">
          <c:if test="${not empty parameter.value}">
            <tr >
            <td>${parameter.key}:</td>
            <td>${parameter.value}</td>
            </tr>
          </c:if>
          <c:if test="${empty parameter.value}">
            <tr style="color: #ccc;">
              <td><font color="#ccc">${parameter.key}:</td>
              <td>N/A</td>
            </tr>
          </c:if>
      </c:forEach>

        </table>

        <a href="javascript:toggleAssertion();" id="assertionlink">[Show / hide OpenID Assertion]</a>

        <pre id="assertion" xml:space="preserve">
${fn:escapeXml(openidAssertion)}
        </pre>

      <br />


      <form name="openid-logout" action="<c:out value='${pageContext.request.contextPath}'/>/" method="post">
        <button type="submit" name="logout">Logout</button>
      </form>


        <div id="contents">${contents}</div>
    </div>

    <div id="footer">
    <ul>
     <li class="footer_contact"><a href="http://nym.sxip.com/nymail/sendmail.pl?a=2a89335f448c01ac"
       target="_blank">Contact</a></li>
    </ul>
    <div class="footer_copyright">&copy; 2007 <a href="http://www.sxip.com/">Sxip Identity Corporation.</a></div>
    </div>
</div>
</body>
</html>
