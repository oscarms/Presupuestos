<%@ page import="budget.User" %>
<%@ page import="budget.Client" %>
<%@ page import="budget.Permission" %>
<%@ page import="dao.UserDAO" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.LogDB" %>
<% 
String budgetId = request.getParameter("budgetId");
if (budgetId == null)
	budgetId = "";

User user = (User)session.getAttribute("user");
int salespersonId = user.getId();
boolean inactives = (request.getParameter("showInactive") != null
&& request.getParameter("showInactive").equals("on"));

String filter = request.getParameter("filter");
if (filter == null)
	filter = "";

LogDAO logDAOLi = new LogDB();
logDAOLi.add(LogType.ACTION, user.getName() + " ha listado clientes", System.currentTimeMillis());

UserDAO userDAO = new UserDB();
Client[] clients = userDAO.getClients(salespersonId, false, inactives, filter);
String cd;
%>
                
            <table>
                <tr>
                    <th colspan="4" class="tablesuper">
                        Clientes
                    </th>
                </tr>
                
                <tr>
                    <th>
                    </th>
                    <th class="tabletitle">NIF
                    </th>
                    <th class="tabletitle">Nombre de la empresa
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>
                <%
           if (clients == null || clients.length<1) { %>
                <tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="3">No hay clientes
                    </td>
                </tr>
           <%
           } else {
        	   	cd = "D";
                for (Client client : clients) { %>     
                <tr>
                    <td>
                    </td>
                    <td class="tablemaincontent<%= cd %>" style="width:140px">
                 <% if (budgetId.length() < 1) { %>
                    <a href="client.jsp?clientId=<%= client.getClientId() %>"><%= client.getClientNumber() %></a>
                 <% } else { // adding client to budget %>
                    <a href="SetClient?budgetId=<%= budgetId %>&clientId=<%= client.getClientId() %>"><%= client.getClientNumber() %></a>
                 <% } %>                    
                    </td>
                    <td class="tablecontent<%= cd %>"><%= client.getName() %>
                    </td>
                    <td class="tablemaincontent<%= cd %>" style="width:85px"><div class="linktext">
                    
                 <% if (budgetId.length() < 1) { %>
                    <a href="client.jsp?clientId=<%= client.getClientId() %>">Mostrar</a>
                 <% } else { // adding client to budget %>
                    <a href="SetClient?budgetId=<%= budgetId %>&clientId=<%= client.getClientId() %>">Seleccionar</a>
                 <% } %>
                    </div>
                    </td>
                </tr>
           <%  
           if (cd.equals("D"))
           	   cd = "C";
           else
               cd = "D";
       	   } // for
    	   } // else
    	   if (clients != null && clients.length >= UserDAO.MAXRESULTS) { %>
                <tr>
                    <th>
                    </th>
                    <th colspan="3" class="tabletitle">
                        Hay más resultados, utilice los filtros para reducir la lista
                    </th>
                </tr>
        <% } %> 
                
                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
<% if (user.hasPermission(Permission.ALLCLIENTS)) { 
	clients = userDAO.getClients(salespersonId, true, inactives, filter);
%> 
                <tr>
                    <th colspan="4" class="tablesuper">
                        Otros clientes
                    </th>
                </tr>
                <tr>
                    <th>
                    </th>
                    <th class="tabletitle">NIF
                    </th>
                    <th class="tabletitle">Nombre de la empresa
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>
                <%
           if (clients == null || clients.length<1) { %>
                <tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="3">No hay clientes
                    </td>
                </tr>
           <%
           } else {
       	   	cd = "D";
               for (Client client : clients) { %>     
               <tr>
                   <td>
                   </td>
                   <td class="tablemaincontent<%= cd %>" style="width:140px">
                <% if (budgetId.length() < 1) { %>
                   <a href="client.jsp?clientId=<%= client.getClientId() %>"><%= client.getClientNumber() %></a>
                <% } else { // adding client to budget %>
                   <a href="SetClient?budgetId=<%= budgetId %>&clientId=<%= client.getClientId() %>"><%= client.getClientNumber() %></a>
                <% } %>                    
                   </td>
                   <td class="tablecontent<%= cd %>"><%= client.getName() %>
                   </td>
                   <td class="tablemaincontent<%= cd %>" style="width:85px"><div class="linktext">
                   
                <% if (budgetId.length() < 1) { %>
                   <a href="client.jsp?clientId=<%= client.getClientId() %>">Mostrar</a>
                <% } else { // adding client to budget %>
                   <a href="SetClient?budgetId=<%= budgetId %>&clientId=<%= client.getClientId() %>">Seleccionar</a>
                <% } %>
                   </div>
                   </td>
               </tr>
          <%  
          if (cd.equals("D"))
          	   cd = "C";
          else
              cd = "D";
      	  } // for
   	   	  } // else  
   	   	   
   	   	  if (clients != null && clients.length >= UserDAO.MAXRESULTS) { %>
                <tr>
                    <th>
                    </th>
                    <th colspan="3" class="tabletitle">
                        Hay más resultados, utilice los filtros para reducir la lista
                    </th>
                </tr>
       <% } %> 
                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
                
 <% } // all clients %> 
                
            </table>