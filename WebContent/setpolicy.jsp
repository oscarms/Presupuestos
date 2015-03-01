<%@ page import="budget.User" %>
<%@ page import="budget.Salesperson" %>
<%@ page import="budget.Permission" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.UserDAO" %>
<%
	User userPo = (User)session.getAttribute("user");
	boolean isAdministratorPo = userPo.hasPermission(Permission.ADMINISTRATE);
	UserDAO userDAOPo = new UserDB();
	LogDAO logDAOPo = new LogDB();
	Integer salespersonIdPo;
	try {
		salespersonIdPo = Integer.parseInt(request.getParameter("salespersonId"));
	} catch (NumberFormatException e) {
		salespersonIdPo = null;
	} catch (NullPointerException e) {
		salespersonIdPo = null;
	}
	if ( salespersonIdPo == null || (userPo.getId() == salespersonIdPo) || !isAdministratorPo) {
		// Error: Only administrators can change the permissions of other users
		logDAOPo.add(LogType.WARNING, userPo.getName() + " ha intentado cambiar los permisos a otro usuario sin permisos", 
				System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}
	Salesperson salespersonPo = userDAOPo.getSalesperson(salespersonIdPo);
	if ( salespersonPo == null ) {
		// Error: The salesperson not exists
		logDAOPo.add(LogType.WARNING, userPo.getName() + " ha intentado cambiar los permisos a un usuario inexistente", 
				System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}
	
	
	%>            
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Establecer permisos</h2>
                	</div>
            	</div>

                <form id="setPolicy" action="SetPolicy" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="salespersonId" value="<%= salespersonPo.getId() %>" />
                    <table>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:139px" class="attributetitle">Habilitar
                            </td>
                            <td style="width:20px"><input type="checkbox" name="enable" id="enable" class="checkbox" 
                            	<% if (salespersonPo.isEnabled()) { %>checked="checked"<% } %> />
                            </td>
                            <td style="width:auto" class="attributecontent">
                            	El usuario habilitado puede iniciar sesión, el deshabilitado no
                            </td>
                        </tr>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:80px" class="attributetitle">Administrador
                            </td>
                            <td style="width:20px"><input type="checkbox" name="administrator" id="administrator" class="checkbox" 
                            	<% if (salespersonPo.hasPermission(Permission.ADMINISTRATE)) { %>checked="checked"<% } %> />
                            </td>
                            <td style="width:auto" class="attributecontent">
                            	El administrador puede editar comerciales, firmar ofertas y editar productos, además de lo siguiente
                            </td>
                        </tr>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:80px" class="attributetitle">Ver ofertas
                            </td>
                            <td style="width:20px"><input type="checkbox" name="viewOffers" id="viewOffers" class="checkbox" 
                            	<% if (salespersonPo.hasPermission(Permission.VIEWOFFERS)) { %>checked="checked"<% } %> />
                            </td>
                            <td style="width:auto" class="attributecontent">
                            	Permite ver las ofertas de la misma forma que ve presupuestos
                            </td>
                        </tr>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:80px" class="attributetitle">Crear ofertas
                            </td>
                            <td style="width:20px"><input type="checkbox" name="createOffers" id="createOffers" class="checkbox" 
                            	<% if (salespersonPo.hasPermission(Permission.CREATEOFFERS)) { %>checked="checked"<% } %> />
                            </td>
                            <td style="width:auto" class="attributecontent">
                            	Permite crear borradores de ofertas,<br />un administrador deberá firmarlas
                            </td>
                        </tr>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:80px" class="attributetitle">Todos los clientes
                            </td>
                            <td style="width:20px"><input type="checkbox" name="allClients" id="allClients" class="checkbox" 
                            	<% if (salespersonPo.hasPermission(Permission.ALLCLIENTS)) { %>checked="checked"<% } %> />
                            </td>
                            <td style="width:auto" class="attributecontent">
                            	Permite ver los clientes que no pertenecen a su cartera, crearles presupuestos y, si se activa, ofertas<br /><br />
                            </td>
                        </tr>
                        <tr>
                        	<td class="tablemargin">
                            </td>
                        	<td colspan="3">
	                            <div class="lefttext" id="infoSetPolicy">
				           			<% if (!salespersonPo.isEnabled()) { %>
				                		<%= salespersonPo.getName() %> actualmente es un comercial inhabilitado<br /><br  />
				                	<% } else if (salespersonPo.hasPermission(Permission.ADMINISTRATE)) { %>
					                	<%= salespersonPo.getName() %> actualmente es un administrador<br /><br  />
					                <% } else if (salespersonPo.getPermissions().length > 0) { %>
					                	<%= salespersonPo.getName() %> actualmente es un comercial avanzado<br /><br  />
					                <% } else { %>
					                	<%= salespersonPo.getName() %> actualmente es un comercial<br /><br  />
					                <% } %>
					                
					                <%
					                Integer messagePo = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
									if (messagePo == null)
										messagePo = 0;
									
									if (messagePo == 41) {
									%>
										<div style='color:#444'>Se han actualizado las políticas<br  /><br  /></div>
									<%
									} else if (messagePo == 42) {
										%>
										<div style='color:#C03'>Se ha producido un error y no se han actualizado las políticas<br  /><br  /></div>
										<%
										}
									%>
				            	</div>
			            	</td>
                        </tr>
                        <tr>
                            <td colspan="4" class="attributetitle">
                                <div id="restore" class="linkpointer">
                                    Deshacer cambios
                                </div>
                                <div class="rightlink">
                                    <div id="submitSetPolicy" class="linkpointer">
                                        Guardar cambios
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </table>
            </form>
            
            <script type="text/javascript">
                document.getElementById("submitSetPolicy").onclick = function(){ sendForm("setPolicyDiv", "SetPolicy", "setPolicy"); };
				document.getElementById("restore").onclick = function(){ 
					updateDiv("setPolicyDiv", "setpolicy.jsp?salespersonId=<%= salespersonPo.getId() %>"); };
				document.getElementById("administrator").onchange = function(){ checkPoliciesA(); };
				document.getElementById("viewOffers").onchange = function(){ checkPoliciesB(); };
				document.getElementById("createOffers").onchange = function(){ checkPoliciesB(); };
				document.getElementById("allClients").onchange = function(){ checkPoliciesB(); };
            </script>
            
            