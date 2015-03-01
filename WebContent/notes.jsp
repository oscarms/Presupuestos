<%@ page import="budget.User" %>
<%@ page import="budget.Annotation" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%
String budgetIdN = request.getParameter("budgetId");
User userN = (User)session.getAttribute("user");
LogDAO logDAON = new LogDB();
BudgetDAO budgetDAON = new BudgetDB();
logDAON.add(LogType.ACTION, userN.getName() + " ha listado las anotaciones del presupuesto " + budgetIdN, System.currentTimeMillis());

Annotation[] annotations = budgetDAON.getAnnotations(budgetIdN);
if (annotations == null || annotations.length < 1) { %>
				<div class="linetext">
                    No hay notas
                </div>
<%
} else {
	for (Annotation annotation : annotations) { %>
				<h4>
                    <%= annotation.getDateString() %>
                </h4>
                <div class="linetext">
                    <%= annotation.getText() %>
                </div>
		<%
	} // end for
} // end else %>
