<%@ page import="budget.User" %>
<%@ page import="budget.Salesperson" %>
<%@ page import="budget.Permission" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.UserDAO" %>
<%@ include file="header.jsp" %>

<%
// Create (isNew && salespersonId == null && user.isAdministrator)
// Edit (!isNew && salespersonId != null && user.isAdministrator)
// Self Edit (!isNew && salespersonId != null && user.getId() = salespersonId))
Integer salespersonId;
try {
	salespersonId = Integer.parseInt(request.getParameter("salespersonId"));
} catch (NumberFormatException e) {
	salespersonId = null;
} catch (NullPointerException e) {
	salespersonId = null;
}
Boolean isNew = (request.getParameter("isNew") != null
&& request.getParameter("isNew").equals("1"));

User user = (User)session.getAttribute("user");
boolean isAdministrator = user.hasPermission(Permission.ADMINISTRATE);
		
LogDAO logDAO = new LogDB();
UserDAO userDAO = new UserDB();

if ( salespersonId == null && !isNew ) {
	// Self editing
	salespersonId = user.getId();
}

// check if it is a valid case
if (!(isNew && salespersonId == null && isAdministrator) && 
		!(!isNew && salespersonId != null && isAdministrator) && 
		!(!isNew && salespersonId != null && user.getId() == salespersonId) ) {
	// Error, a user not administator may be trying to access data
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a salesperson.jsp sin permisos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}

// retrieve salesperson from database and show data
Salesperson salesperson = null;
if (!isNew) {
	salesperson = userDAO.getSalesperson(salespersonId);
	if (salesperson == null) {
		logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a un comercial inexistente", System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}
}

if (isNew)
	logDAO.add(LogType.ACTION, user.getName() + " ha accedido a crear un comercial ", System.currentTimeMillis());
else
	logDAO.add(LogType.ACTION, user.getName() + " ha visualizado el comercial " + salespersonId, System.currentTimeMillis());

String name = "";
String email = "";

if (salesperson != null) {
	name = salesperson.getName();
	if (name == null)
		name = "";
	
	email = salesperson.getEmail();
	if (email == null)
		email = "";
}
%>

<%
Integer message = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
if (message == null)
	message = 0;

if (message == 1) {
%>
	<script type="text/javascript">
		setMessage("La dirección de correo está siendo usada por otro usuario",true);
    </script>
<%
} else if (message == 2) {
%>
	<script type="text/javascript">
		setMessage("Los datos han sido actualizados",false);
    </script>
<%
} else if (message == 3) {
%>
	<script type="text/javascript">
		setMessage("Se ha producido un error",true);
    </script>
<%
} else if (message == 4) {
%>
	<script type="text/javascript">
		setMessage("Se ha creado el comercial. No olvide asignarle permisos y enviarle una nueva contraseña",false);
    </script>
<%
}
%>
							
    <div id="main">
    	
        <div class="bluebox">
            
            <div class="linetitleA">
                <div class="lefttext">
                <%
                if (isNew) { %>
                	<h2>Crear comercial</h2>
                <%
                } else if (user.getId() == salespersonId) {
                %>
                <h2>Actualizar mis datos</h2>
                <%
                } else { // administrator editing salesperson
                %>
                	<h2>Editar comercial</h2>
                <%
                }
                %>
                </div>
            </div>
            
            <form id="salespersonData" action="SaveSalesperson" method="post" enctype="multipart/form-data">
            	<input type="hidden" name="salespersonId" 
            		value="<% if (!isNew) { %><%= Integer.toString(salespersonId) %><% } %>" />
                <table>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:145px" class="attributetitle">Nombre
                        </td>
                        <td style="width:auto"><input type="text" name="name" id="name" class="textbox" value="<%= name %>" />
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:145px" class="attributetitle">Correo electrónico
                        </td>
                        <td style="width:auto"><input type="text" name="email" id="email" class="textbox" value="<%= email %>" />
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td>
                        </td>
                        <td style="width:auto" class="attributetitle">
                            <div class="rightlink">
                                <div id="submittext" class="linkpointer">
                                	<% if (!isNew) { %>
                                    Guardar&nbsp;cambios
                                    <% } else { %>
                                    Crear&nbsp;comercial
                                    <% } %>
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </form>
            
            <script type="text/javascript">
                document.getElementById("submittext").onclick = function(){ 
                	if (salespersonData()) { document.getElementById("salespersonData").submit(); } };
            </script>
                
        </div> <!-- bluebox / -->
        
    <% if (salespersonId != null && (user.getId() == salespersonId) && !isNew) { // if self-editing and not is new %>
        
        <div class="linetitleA">
            <div class="linktext">
                <a href="#windowChangePassword">Cambiar contraseña</a>
            </div>
            <div class="rightlink">
                <a href="#windowMailPassword">
	                <% if (salesperson.hasEmailPassword()) { %>
	                	Modificar contraseña del correo electrónico
	                <% } else { %>
	                	Establecer contraseña del correo electrónico
	                <% } %>
                </a>
            </div>
        </div>
        
        
        <!-- modal window to change password -->
        
        <div id="windowChangePassword" class="modalWindow">
            <div id="changePasswordDiv">
            
            <%@ include file="changepassword.jsp" %>
            
            </div> <!-- changePasswordDiv / -->
        </div>
    	
        <!-- modal window to change password / -->
        
        <!-- modal window to set mail password -->
        
        <div id="windowMailPassword" class="modalWindow">
            <div id="mailPasswordDiv">
            
            <%@ include file="setmailpassword.jsp" %>
            
            </div> <!-- mailPasswordDiv / -->
        </div>
    	
        <!-- modal window to set mail password / -->

	<% } // if self-editing and not is new %>

    <% if ( salespersonId != null && !(user.getId() == salespersonId) && isAdministrator) { // if administrator and not himself %>
        
        <div class="linetitleA">
            <div class="linktext">
                <a href="#windowSendNewPassword">Cambiar su contraseña y enviársela por correo electrónico</a>
            </div>
            <div class="rightlink">
                <a href="#windowSetPolicy">
                	<% if (!salesperson.isEnabled()) { %>
                		Comprobar permisos del comercial inhabilitado
                	<% } else if (salesperson.hasPermission(Permission.ADMINISTRATE)) { %>
	                	Comprobar permisos del administrador
	                <% } else if (salesperson.getPermissions().length > 0) { %>
	                	Comprobar permisos del comercial avanzado
	                <% } else { %>
	                	Comprobar permisos del comercial
	                <% } %>
                </a>
            </div>
        </div>
        
        
        <!-- modal window to send new password -->
        
        <div id="windowSendNewPassword" class="modalWindow">
            <div id="sendNewPasswordDiv">
            <%@ include file="sendpassword.jsp" %>
            
            </div> <!-- sendNewPasswordDiv / -->
        </div>
    	
        <!-- modal window to send new password / -->
        
        <!-- modal window to edit policies -->
        
        <div id="windowSetPolicy" class="modalWindow">
            <div id="setPolicyDiv">
            
            <%@ include file="setpolicy.jsp" %>
            
            </div> <!-- mailPasswordDiv / -->
        </div>
    	
        <!-- modal window to edit policies / -->

	<% } // if administrator and not himself %>

	</div> <!-- main / -->
<%@ include file="footer.jsp" %>