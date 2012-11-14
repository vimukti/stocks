<html>
	<head>
		<meta http-equiv="refresh" content="3">
	</head>
	<body>
		<%
		String status=(String)request.getAttribute("status");
		if(status==null){
			status="";
		}
		%>
		Please wait content creating...<br>
		Current status is '<%= status%>'
	</body>
</html>