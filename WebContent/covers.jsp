<%@ page import="budget.User" %>
<%@ page import="budget.Cover" %>
<%@ page import="budget.Budget" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%
String budgetIdC = request.getParameter("budgetId");
User userC = (User)session.getAttribute("user");
LogDAO logDAOC = new LogDB();
BudgetDAO budgetDAOC = new BudgetDB();
Budget budgetC = budgetDAOC.getBudget(budgetIdC);
logDAOC.add(LogType.ACTION, userC.getName() + " ha listado las portadas en el presupuesto " + budgetIdC, System.currentTimeMillis());

Cover[] covers = budgetDAOC.getCovers();
if (covers != null && covers.length > 0) {

	for(Cover cover : covers) {
		// if cover.id == budgetC.getCoverId() selectedcover %>
					<img src="Download?coverImage=<%= cover.getId() %>" alt="Elegir portada" 
						class="<% if (cover.getId() != budgetC.getCoverId()) { %>imgcover<% } else { %>selectedcover<% } %>" 
						id="cover<%= cover.getId() %>" />
					<script type="text/javascript">
						document.getElementById("cover<%= cover.getId() %>").onclick = 
							function(){ updateDiv('coverlist',
									'SelectCover?coverId=<%= cover.getId() %>&budgetId=<%= budgetIdC %>'); };
					</script>
		<%
	} // end for
} // end if
%>
                    <a href="#windowUploadCover"><img src="images/addcover.png" alt="Añadir" class="imgcover" id="addcover" /></a>
                    