<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.Budget" %>
<%@ page import="dao.BudgetDAO" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.util.Date" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.LogDB" %>
<%
SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
simpleDateFormat.setLenient(false);
Date dateL;
String from = request.getParameter("from");
try {
	dateL = simpleDateFormat.parse(from);
} catch (ParseException e) {
	dateL = new Date(System.currentTimeMillis() - 2592000000L );
} catch (NullPointerException e) {
	dateL = new Date(System.currentTimeMillis() - 2592000000L );
}
long fromL = dateL.getTime();
 		
String to = request.getParameter("to");
try {
	dateL = simpleDateFormat.parse(to);
} catch (ParseException e) {
	dateL = new Date( System.currentTimeMillis() );
} catch (NullPointerException e) {
	dateL = new Date( System.currentTimeMillis() );
}
long toL = dateL.getTime()+86400000; // Include all last day

String filter = request.getParameter("filter");
if (filter == null)
	filter = "";

boolean expired = (request.getParameter("showExpired") != null
		&& request.getParameter("showExpired").equals("on"));

User user = (User)session.getAttribute("user");

BudgetDAO budgetDAO = new BudgetDB();
Budget[] budgets = budgetDAO.getBudgets(user.getId(), false, fromL, toL, filter, expired);
String pyo;
if (user.hasPermission(Permission.VIEWOFFERS)) {
	pyo = "Presupuestos y ofertas";
} else {
	pyo = "Presupuestos";
}

LogDAO logDAOLi = new LogDB();
logDAOLi.add(LogType.ACTION, user.getName() + " ha listado presupuestos", System.currentTimeMillis());

String cd;
%>                           
            <table>
                <tr>
                    <th colspan="7" class="tablesuper">
                        <%= pyo %> de mis clientes
                    </th>
                </tr>
                
                <tr>
                    <th>
                    </th>
                    <th class="tabletitle">Tipo
                    </th>
                    <th class="tabletitle">Número
                    </th>
                    <th class="tabletitle">Fecha de creación
                    </th>
                    <th class="tabletitle">Cliente
                    </th>
                    <th class="tabletitle">Referencia de obra
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>
           	<%
           	if (budgets == null || budgets.length<1) { %>
           		<tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="6">No hay <%= pyo.toLowerCase() %>
                    </td>
                </tr>
                 <%
           	} else {
        	cd = "D";
            	for (Budget budget : budgets) { %> 
                <tr>
                    <td>
                    </td>
                    <td class="tablecontent<%= cd %>"><% if (budget.isOffer()) { %>Oferta<% } else { %>Presupuesto<%} %>
                    </td>
                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= budget.getClient().getName() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
                    </td>
                    <td class="tablemaincontent<%= cd %>" style="width:85px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a></div>
                    </td>
                </tr>
           	<%  
           			if (cd.equals("D"))
           				cd = "C";
           			else
               			cd = "D";
       	   		} // for
    	   	} // else
    	   	if (budgets != null && budgets.length >= BudgetDAO.MAXRESULTS) { %>
                <tr>
                    <th>
                    </th>
                    <th colspan="6" class="tabletitle">
                        Hay más resultados, utilice los filtros para reducir la lista
                    </th>
                </tr>
        <% } %> 

                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
                
<% if (user.hasPermission(Permission.ALLCLIENTS)) { 
	budgets = budgetDAO.getBudgets(user.getId(), true, fromL, toL, filter, expired);
%>                 <tr>
                    <th colspan="7" class="tablesuper">
                        <%= pyo %> de otros clientes
                    </th>
                </tr>
                <tr>
                    <th>
                    </th>
                    <th class="tabletitle">Tipo
                    </th>
                    <th class="tabletitle">Número
                    </th>
                    <th class="tabletitle">Fecha de creación
                    </th>
                    <th class="tabletitle">Cliente
                    </th>
                    <th class="tabletitle">Referencia de obra
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>

<%
           	if (budgets == null || budgets.length<1) { %>
           		<tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="6">No hay <%= pyo.toLowerCase() %>
                    </td>
                </tr>
                 <%
           	} else {
        	cd = "D";
            	for (Budget budget : budgets) { %> 
                <tr>
                    <td>
                    </td>
                    <td class="tablecontent<%= cd %>"><% if (budget.isOffer()) { %>Oferta<% } else { %>Presupuesto<%} %>
                    </td>
                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= budget.getClient().getName() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
                    </td>
                    <td class="tablemaincontent<%= cd %>" style="width:85px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a></div>
                    </td>
                </tr>
           	<%  
           			if (cd.equals("D"))
           				cd = "C";
           			else
               			cd = "D";
       	   		} // for
    	   	} // else
    	   	if (budgets != null && budgets.length >= BudgetDAO.MAXRESULTS) { %>
                <tr>
                    <th>
                    </th>
                    <th colspan="6" class="tabletitle">
                        Hay más resultados, utilice los filtros para reducir la lista
                    </th>
                </tr>
        <% } %> 
                
                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
<% } // all budgets %>              
            </table>