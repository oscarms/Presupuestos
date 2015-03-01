<%@ page import="budget.User" %>
<%@ page import="budget.Document" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%
String budgetIdD = request.getParameter("budgetId");
User userD = (User)session.getAttribute("user");
LogDAO logDAOD = new LogDB();
BudgetDAO budgetDAOD = new BudgetDB();
logDAOD.add(LogType.ACTION, userD.getName() + " ha listado los documentos del presupuesto " + budgetIdD, System.currentTimeMillis());

Document[] documents = budgetDAOD.getDocuments(budgetIdD);
if (documents == null || documents.length < 1) { %>
				<div class="linetext">
                    <div class="lefttext">
                        No hay documentos
                    </div>
                </div>
<%
} else {
	for (Document document : documents) { %>
				<div class="linetext" id="documentitem<%= document.getId() %>">
                    <div class="lefttext">
                        <a href="Download?budgetId=<%= budgetIdD %>&amp;documentId=<%= document.getId() %>" target="_blank">
                        <%= document.getName() %></a>
                    </div>
                    <div class="rightlink">
                        <img src="images/bin.png" alt="Eliminar" class="imgbuttonsmall" 
                        id="removedocumentitem<%= document.getId() %>" />
                    </div>
                </div>
				<script type="text/javascript">
					document.getElementById("removedocumentitem<%= document.getId() %>").onclick = 
						function(){ if (confirm("Se va a eliminar el documento")) { updateDiv('documentlist',
							'Remove?documentId=<%= document.getId() %>&budgetId=<%= budgetIdD %>'); } };
                </script>

		<%
	} // for
} // else
%>
