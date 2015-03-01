<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.Budget" %>
<%@ page import="dao.BudgetDAO" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.LogDB" %>
<%
User user = (User)session.getAttribute("user");
BudgetDAO budgetDAO = new BudgetDB();
Budget[] budgets;
List<String> scripts = new LinkedList<String>();

String baoc;
if (user.hasPermission(Permission.CREATEOFFERS))
	baoc = "Presupuestos y ofertas";
else
	baoc = "Presupuestos";

LogDAO logDAOLi = new LogDB();
logDAOLi.add(LogType.ACTION, user.getName() + " ha listado sus notificaciones", System.currentTimeMillis());

String cd;
%>

<!-- notificationlist.jsp -->

           <table>
           				
           			<tr>
	                    <th colspan="7" class="tablesuper">
	                        <%= baoc %> pendientes de finalizar
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getIncompleteBudgets(user.getId());
	            
	            	if (budgets == null || budgets.length < 1) {%>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay <%= baoc.toLowerCase() %> pendientes de finalizar
	                    </td>
	                </tr>
	                    
	            <%	} else {
		            for (Budget budget : budgets) {
		            cd = "D"; %>
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>">
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a></div>
	                    </td>
	                </tr>

		    	<% 
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>

	                <tr>
	                    <th colspan="7" class="tablesuper">
	                        Nuevos presupuestos para tus clientes
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getNewBudgets(user.getId());
	            
	            	if (budgets == null || budgets.length < 1) {%>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay nuevos presupuestos para tus clientes
	                    </td>
	                </tr>
	                    
	            <%	} else {
		            for (Budget budget : budgets) {
		            cd = "D"; %>
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a>&nbsp;&nbsp;&nbsp;</div><div class="linkpointer" id="notification<%= budget.getBudgetId() %>">Descartar</div>
	                    </td>
	                </tr>

		    	<% 
		    		scripts.add( budget.getBudgetId() );
		    	
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>
	                
	            <% if (user.hasPermission(Permission.VIEWOFFERS)) { %>
	                <tr>
	                    <th colspan="7" class="tablesuper">
	                        Nuevas ofertas firmadas para tus clientes
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getNewSignedOffers(user.getId());
	            
	            	if (budgets == null || budgets.length < 1) {%>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay nuevas ofertas firmadas para tus clientes
	                    </td>
	                </tr>
	                    
	            <%	} else {
		            for (Budget budget : budgets) {
		            cd = "D"; %>
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a>&nbsp;&nbsp;&nbsp;</div><div class="linkpointer" id="notification<%= budget.getBudgetId() %>">Descartar</div>
	                    </td>
	                </tr>

		    	<% 
		    		scripts.add( budget.getBudgetId() );
		    	
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>	                
	           
	            <% 	} // end if viewoffers %>
	            
				<% if (user.hasPermission(Permission.VIEWOFFERS)) { %>
	                <tr>
	                    <th colspan="7" class="tablesuper">
	                        Ofertas para tus clientes esperando firma
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getNotSignedOffers(user.getId());
	            
	            	if (budgets == null || budgets.length < 1) {%>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay ofertas esperando firma
	                    </td>
	                </tr>
	                    
	            <%	} else {
	            	cd = "D";
		            for (Budget budget : budgets) { %>
		            
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a></div>
	                    </td>
	                </tr>

		    	<% 
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>	                
	           
	            <% 	} // end if createoffers %>
	            
	            <% if (user.hasPermission(Permission.ADMINISTRATE)) { %>
	           		<tr>
	                    <th colspan="7" class="tablesuper">
	                        Ofertas pendientes de firma
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getNotSignedOffers();
	            
	            	if (budgets == null || budgets.length < 1) {%>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay ninguna oferta sin firmar
	                    </td>
	                </tr>
	                    
	            <%	} else {
		            for (Budget budget : budgets) { 
		            cd = "D"; %>
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a></div>
	                    </td>
	                </tr>

		    	<% 
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>
           		
           			<tr>
	                    <th colspan="7" class="tablesuper">
	                        Nuevos presupuestos
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getNewBudgets(user.getId(),true);
	            
	            	if (budgets == null || budgets.length < 1) { %>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay nuevos presupuestos de otros comerciales
	                    </td>
	                </tr>
	                    
	            <%	} else {
		            for (Budget budget : budgets) { 
		            cd = "D"; %>
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a>&nbsp;&nbsp;&nbsp;</div><div class="linkpointer" id="notification<%= budget.getBudgetId() %>">Descartar</div>
	                    </td>
	                </tr>

		    	<% 
		    		scripts.add( budget.getBudgetId() );
		    	
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>
	                
	                <tr>
	                    <th colspan="7" class="tablesuper">
	                        Nuevas ofertas firmadas
	                    </th>
	                </tr>
	           		<tr>
	                    <th>
	                    </th>
	                    <th class="tabletitle">Número
	                    </th>
	                    <th class="tabletitle">Fecha
	                    </th>
	                    <th class="tabletitle">Comercial
	                    </th>
	                    <th class="tabletitle">Cliente
	                    </th>
	                    <th class="tabletitle">Referencia de obra
	                    </th>
	                    <th class="tabletitle">
	                    </th>
	                </tr>
	            
	            <%	budgets = budgetDAO.getNewSignedOffers(user.getId(),true);
	            
	            	if (budgets == null || budgets.length < 1) {%>
	            	
	            	<tr>
	            		<td>
	                    </td>
	                    <td class="tablecontentD" colspan="6">No hay nuevas ofertas firmadas de otros comerciales
	                    </td>
	                </tr>
	                    
	            <%	} else {
		            for (Budget budget : budgets) { 
		            cd = "D"; %>
					<tr>
	                    <td>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>"><%= budget.getBudgetId() %></a>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getCreationDateString() %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getSalesperson() != null ? budget.getSalesperson().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getClient() != null ? budget.getClient().getName() : "Sin asignar" %>
	                    </td>
	                    <td class="tablecontent<%= cd %>"><%= budget.getConstructionRef() %>
	                    </td>
	                    <td class="tablemaincontent<%= cd %>" style="width:145px"><div class="linktext"><a href="budget.jsp?budgetId=<%= budget.getBudgetId() %>">Mostrar</a>&nbsp;&nbsp;&nbsp;</div><div class="linkpointer" id="notification<%= budget.getBudgetId() %>">Descartar</div>
	                    </td>
	                </tr>

		    	<% 
		    		scripts.add( budget.getBudgetId() );
		    	
		    	    if (cd.equals("D"))
		           	    cd = "C";
		            else
		                cd = "D";
		    		} // end else
		    		} // end for %>

	                <tr> 
	                    <th class="tablemargin">&nbsp;
	                    </th>
	                </tr>
           		
           		<% } // end if administrator %>
	            
				</table>
				
				<%
				if (scripts != null && !scripts.isEmpty()) { %>
					<script type="text/javascript">
					<%
					for (String script : scripts) { %>
							document.getElementById("notification<%= script %>").onclick = function(){ updateDiv('notificationlist','RemoveNotification?budgetId=<%= script %>'); };
						<%
					} %>
					</script>
					<%
				}
				%>
		            
        <!-- notificationlist.jsp / -->