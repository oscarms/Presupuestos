<%@ page import="budget.Salesperson" %>
<%@ page import="budget.Permission" %>
<%@ page import="dao.UserDAO" %>
<%@ page import="budget.User" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.LogDB" %>

<% UserDAO userDAO = new UserDB();
   Salesperson[] salespeople = userDAO.getSalespeople();
   LogDAO logDAOLi = new LogDB();
   User userLi = (User)session.getAttribute("user");
   
   // check if the user is an administrator
   if (!userLi.hasPermission(Permission.ADMINISTRATE) || !userLi.hasPermission(Permission.ALLCLIENTS)) {
	logDAOLi.add(LogType.WARNING, userLi.getName() + " ha acedido a salespeoplelist.jsp sin permisos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}
   
   logDAOLi.add(LogType.ACTION, userLi.getName() + " ha listado comerciales", System.currentTimeMillis());
   String cd;
%>

            <table>
                <tr>
                    <th colspan="6" class="tablesuper">
                        Comerciales
                    </th>
                </tr>

           <%
           if (request.getParameter("clientId") == null || request.getParameter("clientId").length() < 1) { // No está asignando comercial a cliente %>
                <tr>
                    <th> 
                    </th>
                    <th class="tabletitle">Nombre
                    </th>
                    <th class="tabletitle">Correo electrónico
                    </th>
                    <th class="tabletitle">Administrador 
                    </th>
                    <th class="tabletitle">Habilitado 
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>           
           <%
           if (salespeople == null || salespeople.length<1) { %>
                <tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="5">No hay comerciales
                    </td>
                </tr>
           <%
           } else {
        	   	cd = "D";
                for (Salesperson salesperson : salespeople) { %>     
                <tr>
                    <td> 
                    </td>
                    <td class="tablecontent<%= cd %>"><%= salesperson.getName() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= salesperson.getEmail() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><% if (salesperson.hasPermission(Permission.ADMINISTRATE)) { %>Sí <% } else { %>No<% } %>
                    </td>
                    <td class="tablecontent<%= cd %>"><% if (salesperson.isEnabled()) { %>Sí <% } else { %>No<% } %>
                    </td>
                    <td class="tablemaincontent<%= cd %>" style="width:100px"><div class="linktext"><a href="salesperson.jsp?salespersonId=<%= Integer.toString(salesperson.getId()) %>">Más opciones</a></div>
                    </td>
                </tr>
       <%  
           if (cd.equals("D"))
           	   cd = "C";
           else
               cd = "D";
       	   } // for
    	   } // else 
    	   } else { // Está asignando comercial a cliente %>
                <tr>
                    <th> 
                    </th>
                    <th class="tabletitle">Nombre
                    </th>
                    <th class="tabletitle">Correo electrónico
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>    		
    	   <%   
           if (salespeople == null || salespeople.length<1) { %>
                <tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="3">No hay comerciales
                    </td>
                </tr>
           <%
           } else {
        	   	cd = "D";
                for (Salesperson salesperson : salespeople) { %>     
                <tr>
                    <td> 
                    </td>
                    <td class="tablecontent<%= cd %>"><%= salesperson.getName() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= salesperson.getEmail() %>
                    </td>
                    <td class="tablemaincontent<%= cd %>" style="width:100px"><div class="linktext"><a href="SetSalesperson?clientId=<%= request.getParameter("clientId") %>&salespersonId=<%= Integer.toString(salesperson.getId()) %>">Seleccionar</a></div>
                    </td>
                </tr>
       <%  
           if (cd.equals("D"))
           	   cd = "C";
           else
               cd = "D";
       	   } // for
    	   } // else 
    	   } // else de asignar a comercial o no
   		%>
    	   
                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
                
            </table>
            