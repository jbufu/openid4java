<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>${title}</title></head>
<body onload="document.forms['openid-auto-post'].submit();">
    <form name="openid-auto-post" action="${message.OPEndpoint}" method="post" accept-charset="utf-8">
        <c:forEach var="parameter" items="${message.parameterMap}">
        <input type="hidden" name="${parameter.key}" value="${parameter.value}"/>
        </c:forEach>
        <noscript>
            <p>Please click the <b>Continue...</b> to resume your OpenID request.<br />
			To prevent this message from appearing in the future, enable Javascript on your browser.</p>
            <button type="submit">Continue...</button>
        </noscript>
    </form>
</body>
</html>
