<%@ page import="budget.Salesperson" %>
<%@ page import="budget.User" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.UserDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%
// retrieve salesperson from database and show data
Salesperson salespersonMP;
UserDAO userDAOMP = new UserDB();
LogDAO logDAOMP = new LogDB();
salespersonMP = userDAOMP.getSalesperson(((User)session.getAttribute("user")).getId());
if (salespersonMP == null) {
	logDAOMP.add(LogType.ERROR, "setmailpassword.jsp no ha obtenido el comercial de la base de datos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}
%>

                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            		<% if (salespersonMP.hasEmailPassword()) { %>
	                	<h2>Modificar contrase�a del correo electr�nico</h2>
	                <% } else { %>
	                	<h2>Establecer contrase�a de correo electr�nico</h2>
	                <% } %>
                	</div>
            	</div>
           
                <form id="mailPassword" action="SetMailPassword" method="post" enctype="multipart/form-data">
                    <table>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:140px" class="attributetitle">Nueva contrase�a
                            </td>
                            <td style="width:auto"><input type="password" name="password" id="password" class="textbox" />
                            </td>
                        </tr>
                        <tr>
                        	<td class="tablemargin">
                            </td>
                        	<td colspan="2">
	                        	<div class="lefttext" id="infoMailPassword">
				           		<%
				           		Integer messageMP = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
								if (messageMP == null)
									messageMP = 0;
								
								if (messageMP == 21) {
								%>
									<div style='color:#444'><br  />Se ha guardado la contrase�a de correo electr�nico<br  /></div>
								<%
								} else if (messageMP == 22) {
								%>
									<div style='color:#444'><br  />Se ha eliminado la contrase�a de correo electr�nico<br  /></div>
								<%
								} else if (messageMP == 23) {
								%>
									<div style='color:#C03'><br  />Se ha producido un error<br  /></div>
								<%
								}
								%>
								<% if (salespersonMP.hasEmailPassword()) { %>
									<div style='color:#444'><br  />Actualmente tiene una contrase�a de correo electr�nico y puede enviar correo electr�nico en su nombre<br /></div>
								<% } else { %>
									<div style='color:#444'><br  />Actualmente no tiene una contrase�a de correo electr�nico y no podr� enviar correo electr�nico en su nombre<br /></div>
								<% } %>
				            	</div>
                        	</td>
                        </tr>
                        <tr>
                            <td colspan="3" class="attributetitle">
                            	<% if (salespersonMP.hasEmailPassword()) { %>
	                                <div id="removemailpassword" class="linkpointer"> <!-- only if exists -->
	                                    Eliminar&nbsp;contrase�a
	                                </div>
                                <% } %>
                                <div class="rightlink">
                                    <div id="submitmailpassword" class="linkpointer">
                                        Establecer&nbsp;contrase�a
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </table>
            </form>
            
            <script type="text/javascript">
                document.getElementById("submitmailpassword").onclick = function(){ submitMailPassword(); };
                <% if (salespersonMP.hasEmailPassword()) { %>
					document.getElementById("removemailpassword").onclick = function(){ updateDiv("mailPasswordDiv", "RemoveMailPassword"); };
				<% } %>
			</script>
            