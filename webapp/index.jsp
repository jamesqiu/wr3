<%@ page contentType = "text/html;charset=utf8" %>
<%
	String url = "/index-gotoreal.html"; 
	String serverName = request.getServerName();
	if (serverName.equals("localhost")) {
		url = "/index-numagic.html";
	} else if (serverName.equals("www.vismed.org") || serverName.equals("vismed.org")) {
		url = "/index-vismed.html";
	}
	RequestDispatcher dispatcher = request.getRequestDispatcher(url);
	//dispatcher.forward(request, response);
%>
<jsp:forward page="<%=url%>" />

