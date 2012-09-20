<%@ page contentType = "text/html;charset=utf8" %>
<%
	String url = "/index-gotoreal.html"; 
	if (request.getServerName().equals("localhost")) url = "/index-numagic.html";
	RequestDispatcher dispatcher = request.getRequestDispatcher(url);
	//dispatcher.forward(request, response);
%>
<jsp:forward page="<%=url%>" />

