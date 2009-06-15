<%@ page session="true" %>
<html>
<body>
<%
    if (request.getParameter("logout")!=null)
    {
        session.removeAttribute("openid");
        session.removeAttribute("openid-claimed");
%>
    Logged out!<p>
<%
    }
	if (session.getAttribute("openid")==null) {
%>
<form method="POST" action="consumer_redirect.jsp">
<strong>OpenID:</strong>
<input type="text" name="openid" size="60"/><br>
<input type="submit"/>
</form>
<%	
} else {

%>
Logged in as <%= session.getAttribute("openid") %><p>
<a href="?logout=true">Log out</a>

<% } %>
</body>
</html>
