<%@ page import="dao.BudgetDAO" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%
	User user = (User)session.getAttribute("user");
	BudgetDAO budgetDAO = new BudgetDB();
	int count = budgetDAO.getNotificationCount(user.getId());
%>

<%= Integer.toString(count) %>
