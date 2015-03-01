<%@ page import="budget.User" %>
<%@ page import="budget.Salesperson" %>
<%@ page import="budget.Permission" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.UserDAO" %>
<%
	User userSP = (User)session.getAttribute("user");
	boolean isAdministratorSP = userSP.hasPermission(Permission.ADMINISTRATE);
	UserDAO userDAOSP = new UserDB();
	LogDAO logDAOSP = new LogDB();
	Integer salespersonIdSP;
	try {
		salespersonIdSP = Integer.parseInt(request.getParameter("salespersonId"));
	} catch (NumberFormatException e) {
		salespersonIdSP = null;
	} catch (NullPointerException e) {
		salespersonIdSP = null;
	}
	if ( salespersonIdSP == null || (userSP.getId() == salespersonIdSP) || !isAdministratorSP) {
		// Error: Only administrators can change the password of other users
		logDAOSP.add(LogType.WARNING, userSP.getName() + " ha intentado cambiar la contraseña a otro usuario sin permisos", 
				System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}
	Salesperson salespersonSP = userDAOSP.getSalesperson(salespersonIdSP);
	if ( salespersonSP == null ) {
		// Error: The salesperson not exists
		logDAOSP.add(LogType.WARNING, userSP.getName() + " ha intentado cambiar la contraseña a un usuario inexistente", 
				System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}
	
	
	%>

                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Enviar nueva contraseña</h2>
                	</div>
            	</div>
           						
           		<div class="lefttext" id="infoSendNewPassword">
           		
	           		<%
	           		Integer messageSP = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
					if (messageSP == null)
						messageSP = 0;
					
					if (messageSP == 31) {
					%>
						<div style='color:#444'>Se ha cambiado y enviado la contraseña<br  /><br /></div>
					<%
					} else if (messageSP == 32) {
					%>
						<div style='color:#C03'>Se ha cambiado la contraseña pero no se ha podido enviar por correo electrónico<br  /><br /></div>
					<%
					} else if (messageSP == 33) {
					%>
						<div style='color:#C03'>Se ha producido un error<br  /><br /></div>
					<%
					} else {
					%>
                	A <%= salespersonSP.getName() %> se le cambiará y enviará una nueva contraseña aleatoria a <%= salespersonSP.getEmail() %><br  />
                    <br  />
                

                    <%
					}
					%>
				</div>
           		<table>
                    <tr>
                        <td class="attributetitle">
                            <div class="linkpointer">
                            <% if ( messageSP == 31 || messageSP == 32 || messageSP == 33 ) { %>
                                <a href="#closeModalWindow">Cerrar</a>
                            <% } else { %>
                            	<a href="#closeModalWindow">Cancelar</a>
                            <% } %>
                            </div>
               
                            <div class="rightlink">
                            <% if ( !(messageSP == 31 || messageSP == 32 || messageSP == 33 )) { %>
                                <div id="sendNewPassword" class="linkpointer">
                                    Continuar
                                </div>
                                <script type="text/javascript">
				                    document.getElementById("sendNewPassword").onclick = function(){ updateDiv("sendNewPasswordDiv", "SendNewPassword?salespersonId=<%= salespersonSP.getId() %>"); };
				               </script>
                            <% } %>
                            </div>
                        </td>
                    </tr>
                </table>
               