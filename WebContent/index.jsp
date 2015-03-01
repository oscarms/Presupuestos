<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<% 
if ( ((User)session.getAttribute("user")).hasPermission(Permission.ADMINISTRATE) ) {
	response.sendRedirect("notifications.jsp");
} else {
	response.sendRedirect("budgets.jsp");
}
%>
