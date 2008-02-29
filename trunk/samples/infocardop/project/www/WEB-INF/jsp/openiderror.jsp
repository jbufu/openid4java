<%--
  ~ Copyright 2006-2008 Sxip Identity Corporation
  --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>OpenID Error</title>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/style.css" type="text/css" media="all" title="InfocardOP" charset="utf-8"/>
</head>
<body>
<div id="wrapper" style="width: 600px;">
	<div id="header">
	</div>

<h1>Whoops!</h1>

<p>
	An error was encountered while processing the OpenID request:
    <br />
    Expected a POST HTTP request but received a HTTP GET.
</p>

</div>
</body>
</html>
