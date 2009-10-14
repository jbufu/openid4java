<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Hello World!</title>
	<meta http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8" />
	<style type="text/css">
	.code {
		width: 100%;
		height: 200px;
	}
	th {
		text-align: right;
	}
	</style>
</head>
<body>
	<div>
		<div>Login Success!</div>
		<div>
			queryString:
			<textarea class="code">${pageContext.request.queryString}</textarea>
		</div>
		<div>
			<table>
				<tr>
					<th>Your OpenID:</th>
					<td>${identifier}</td>
				</tr>
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
		</div>
		<div>
			<a href="logout.jsp">Logout</a>
		</div>
	</div>
</body>
</html>
