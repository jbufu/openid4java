<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="text/html; charset=UTF-8" import="java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Hello World!</title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="consumer-servlet.css" />
</head>
<body>
	<div>
		<div>Login Success! - <a href="logout.jsp">Logout</a></div>
		<div>
			<fieldset>
				<legend>Your OpenID</legend>
				<input type="text" name="openid_identifier" value="${identifier}" />
			</fieldset>
		</div>
		<div id="sreg-result">
			<fieldset>
				<legend>Simple Registration</legend>
				<table>
					<tr>
						<th>Nickname:</th>
						<td>${nickname}</td>
					</tr>
					<tr>
						<th>Email:</th>
						<td>${email}</td>
					</tr>
					<tr>
						<th>Fullname:</th>
						<td>${fullname}</td>
					</tr>
					<tr>
						<th>Date of birth:</th>
						<td>${dob}</td>
					</tr>
					<tr>
						<th>Gender:</th>
						<td>${gender}</td>
					</tr>
					<tr>
						<th>Postcode:</th>
						<td>${postcode}</td>
					</tr>
					<tr>
						<th>Country:</th>
						<td>${country}</td>
					</tr>
					<tr>
						<th>Language:</th>
						<td>${language}</td>
					</tr>
					<tr>
						<th>Timezone:</th>
						<td>${timezone}</td>
					</tr>
				</table>
			</fieldset>
		</div>
		<div id="ax-result">
			<fieldset>
				<legend>Attribute Exchange</legend>
				<table>
					<c:forEach items="${attributes}" var="attribute">
					<tr>
						<th>${attribute.key}:</th>
						<td>${attribute.value}</td>
					</tr>
					</c:forEach>
				</table>
			</fieldset>
		</div>
		<div>
			<fieldset>
				<legend>queryString</legend>
				<textarea name="queryString">${pageContext.request.queryString}</textarea>
			</fieldset>
		</div>
	</div>
</body>
</html>
