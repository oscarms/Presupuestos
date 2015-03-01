<%@ page import="budget.User" %>
<%@ page import="budget.Client" %>
<%@ page import="budget.Permission" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.UserDB" %>
<%@ page import="dao.UserDAO" %>
<%@ include file="header.jsp" %>
<%
// Create (isNew == 1 && clientId == null) Edit (clientId != null) Error (clientId == null && !isNew))
Integer clientId;
try {
	clientId = Integer.parseInt(request.getParameter("clientId"));
} catch (NumberFormatException e) {
	clientId = null;
} catch (NullPointerException e) {
	clientId = null;
}
Boolean isNew = (request.getParameter("isNew") != null
&& request.getParameter("isNew").equals("1"));

User user = (User)session.getAttribute("user");

LogDAO logDAO = new LogDB();
UserDAO userDAO = new UserDB();

if ( clientId == null && !isNew ) {
	// ERROR, log and logout user
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a client.jsp sin argumentos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}

Client client = null;
String clientNumber ="";
String name = "";
String email = "";
String phone = "";
String person = "";
boolean isActive = true;
String address = "";
String town = "";
String province = "";
String country = "";
String postalCode = "";
String notes = "";
if ( clientId != null ) {
	// get clientId from database and show data
	client = userDAO.getClient(clientId);
	if (client == null || // Check if the user can view the client or Error
			!( user.hasPermission(Permission.ALLCLIENTS) || // all clients
					(client.getSalesperson() != null && user.equals(client.getSalesperson()) ) )) { // salesperson
		// ERROR, log and logout user
		logDAO.add(LogType.WARNING, user.getName() + 
				" ha intentado acceder a client.jsp con clientId no existente o sin permiso", System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}
	
	clientNumber = client.getClientNumber();
	if (clientNumber == null)
		clientNumber = "";
	
	name = client.getName();
	if (name == null)
		name = "";
	
	email = client.getEmail();
	if (email == null)
		email = "";
	
	phone = client.getPhone();
	if (phone == null)
		phone = "";
	
	person = client.getPerson();
	if (person == null)
		person = "";
	
	isActive = client.isActive();
	
	address = client.getAddress();
	if (address == null)
		address = "";
	
	town = client.getTown();
	if (town == null)
		town = "";
	
	province = client.getProvince();
	if (province == null)
		province = "";
	
	country = client.getCountry();
	if (country == null)
		country = "";
	
	postalCode = client.getPostalCode();
	if (postalCode == null)
		postalCode = "";
	
	notes = client.getNotes();
	if (notes == null)
		notes = "";
	
	logDAO.add(LogType.ACTION, user.getName() + " ha visualizado el cliente " + clientNumber, System.currentTimeMillis());
	
} else 
	logDAO.add(LogType.ACTION, user.getName() + " ha accedido a crear un cliente", System.currentTimeMillis());
%>
<%
Integer message = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
if (message == null)
	message = 0;

if (message == 1) {
%>
	<script type="text/javascript">
		setMessage("Información del cliente actualizada",false);
    </script>
<%
} else if (message == 2) {
%>
	<script type="text/javascript">
		setMessage("Cliente creado",false);
    </script>
<%
} else if (message == 3) {
%>
	<script type="text/javascript">
		setMessage("El CIF o correo electrónico introducido ya existe",true);
    </script>
<%
} else if (message == 4) {
%>
	<script type="text/javascript">
		setMessage("Se ha cambiado el comercial asignado",false);
    </script>
<%
} else if (message == 5) {
%>
	<script type="text/javascript">
		setMessage("Se ha producido un error",true);
    </script>
<%
}
%>
    <div id="main">
    	
        <div class="bluebox">
       
            <form id="clientData" action="SaveClient" method="post" enctype="multipart/form-data">
            	<input type="hidden" name="clientId" id="clientId" <% if (!isNew) { %>value="<%= clientId %>" <% } %>/> 
                <table>
                	<tr>
                        <th colspan="4" class="tablesuper">
                            Detalles del cliente
                        </th>
                        <% if (!isNew) { %>
	                        <th style="width:300px" class="superattribute">
	                        	<div class="lefttext">
	                        		Logotipo
	                            </div>
	                            <div class="rightlink">
	                            	<a href="#windowUploadImage">Cambiar</a>
	                            </div>
                            <% } %>
                        </th>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">NIF
                        </td>
                        <td style="width:auto"><input type="text" name="number" id="number" class="textbox" 
                        <% if (!isNew) { %>value="<%= clientNumber %>" <% } %> />
                        </td>
                        <td class="tablemargin">
                        </td>
                        <% if (!isNew) { %>
	                        <td style="width:300px" rowspan="8">
	                        	<img src="Download?clientImage=<%= clientId %>" alt="Logotipo" class="image" id="clientimage" />
	                        </td>
                        <% } %>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Nombre
                        </td>
                        <td style="width:auto"><input type="text" name="name" id="name" class="textbox" 
                        <% if (!isNew) { %>value="<%= name %>" <% } %> />
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Correo electrónico
                        </td>
                        <td style="width:auto"><input type="text" name="email" id="email" class="textbox" 
                        <% if (!isNew) { %>value="<%= email %>" <% } %> />
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <% if (!isNew) { %>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Teléfono
                        </td>
                        <td style="width:auto"><input type="text" name="phone" id="phone" class="textbox" 
                        <% if (!isNew) { %>value="<%= phone %>" <% } %> />
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Persona de contacto
                        </td>
                        <td style="width:auto"><input type="text" name="person" id="person" class="textbox" 
                        <% if (!isNew) { %>value="<%= person %>" <% } %> />
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <tr>
                    	<td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Cliente activo
                        </td>
                        <td style="width:auto"><input type="checkbox" name="active" id="active" class="checkbox" 
                        <% if (!isNew && isActive) { %>checked="checked" <% } %> />
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <tr>
                    	<td class="tablemargin">&nbsp;
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Dirección
                        </td>
                        <td style="width:auto">
                        	<table>
                            	<tr>
                                    <td>&nbsp; 
                                    </td>
                                </tr>
                            	<tr>
                                    <td style="width:110px" class="attributecontent">Calle, Nº, Piso
                                    </td>
                                    <td style="width:auto"><input type="text" name="address" id="address" class="textbox" 
                                    <% if (!isNew) { %>value="<%= address %>" <% } %> />
                                    </td>
                                </tr>
                            	<tr>
                                	<td colspan="2">
                                        <table>
                                            <tr>
                                                <td style="width:55px" class="attributecontent">Ciudad
                                                </td>
                                                <td style="width:auto"><input type="text" name="town" id="town" class="textbox" 
                                                <% if (!isNew) { %>value="<%= town %>" <% } %> />
                                                </td>
                                                <td class="tablemargin">
                                                </td>
                                                <td style="width:85px" class="attributecontent">Provincia
                                                </td>
                                                <td style="width:auto"><input type="text" name="province" id="province" class="textbox" 
                                                <% if (!isNew) { %>value="<%= province %>" <% } %> />
                                                </td>
                                                <td class="tablemargin">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="width:auto" class="attributecontent">País
                                                </td>
                                                <td style="width:auto"><input type="text" name="country" id="country" class="textbox" 
                                                <% if (!isNew) { %>value="<%= country %>" <% } %> />
                                                </td>
                                                <td class="tablemargin">
                                                </td>
                                                <td style="width:85px" class="attributecontent">Cód. Postal
                                                </td>
                                                <td style="width:auto"><input type="text" name="postalcode" id="postalcode" class="textbox" 
                                                <% if (!isNew) { %>value="<%= postalCode %>" <% } %> />
                                                </td>
                                                <td class="tablemargin">
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <% } // if (!isNew) %>
                        	</table>
                        </td>
                    </tr>
                    <tr>
                    	<td>&nbsp;
                        </td>
                    </tr>
                    <% if (!isNew) { %>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Otra información
                        </td>
                        <td style="width:auto"><textarea name="notes" id="notes" class="bigtextbox"><%= notes %></textarea>
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <% } %>
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
                                    Crear&nbsp;cliente
                                    <% } %>
                                </div>
                            </div>
                        </td>
                    </tr>
                    
                    <% if (!isNew && (user.hasPermission(Permission.ADMINISTRATE) || user.hasPermission(Permission.ALLCLIENTS) ) ) { %>

                    <tr>
                    	<td colspan="3">
                        	<div class="linetitleC">
               					<div class="lefttext">
                    				<h2>Información del comercial</h2>
                				</div>
            				</div>
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:160px" class="attributetitle">Comercial asignado
                        </td>
                        <td style="width:auto" class="attributecontent">
                        	<div class="lefttext">
                        		<% if (client.getSalesperson() == null) { %>
                            		No hay comercial asignado
                            	<% } else { %>
                            		<%= client.getSalesperson().getName() %>
                            	<% } %>
                            </div>
                            
                            <% if (user.hasPermission(Permission.ADMINISTRATE) ) { %>
                            <div class="linktext" id="openWindowSetSalesperson">
                                <a href="#windowSetSalesperson">&nbsp;&nbsp;&nbsp;Cambiar</a>
                            </div>
                            <% } %>
                        </td>
                    </tr>
                    
                    <% } %>
                    
                </table>
            </form>
            
            <script type="text/javascript">
            	document.getElementById("number").onkeyup = function(){ checkClientForm(); };
				document.getElementById("number").onchange = function(){ checkClientForm(); };
				document.getElementById("name").onkeyup = function(){ checkClientForm(); };
				document.getElementById("name").onchange = function(){ checkClientForm(); };
				document.getElementById("email").onkeyup = function(){ checkClientForm(); };
				document.getElementById("email").onchange = function(){ checkClientForm(); };
                document.getElementById("submittext").onclick = function(){ 
                	if (checkClientForm()) document.getElementById("clientData").submit(); };
            </script>
            
        </div> <!-- bluebox / -->
        
        <% if (!isNew && user.hasPermission(Permission.ADMINISTRATE)) { // modal window to set salesperson if administrator %>
        
        <div id="windowSetSalesperson" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Seleccione el nuevo comercial</h2>
                	</div>
            	</div>
           
           		<div id="salespersoncontent">
                	
                </div> <!-- salespersoncontent -->
            </div>
            
            <script type="text/javascript">
				document.getElementById("openWindowSetSalesperson").onclick = function(){ 
					updateDiv("salespersoncontent","salespeoplelist.jsp?clientId=<%= clientId %>"); };
            </script>
                    
        </div>
    	
        <% } // modal window %>
        
        <% if (!isNew) { // modal window to upload image %>
        
        <div id="windowUploadImage" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Cambiar logotipo</h2>
                	</div>
            	</div>
           
                <div class="lefttext">
                    Seleccione el fichero de imagen para establecer como logotipo del cliente.
                </div>
                <form id="uploadImage" action="UploadClientImage" method="post" enctype="multipart/form-data">
                	<input type="hidden" name="clientId" value="<%= clientId %>" />
                    <input type="file" name="imageToUpload" id="imageToUpload" onchange="fileSelected('clientImage');" class="fileSelector" />
                    <input type="button" onclick="uploadFile('clientImage')" value="Cargar imagen" class="fileSelectorBtn" id="imageToUploadBtn" />
                </form>
                <div class="progress"><p>&nbsp;</p></div>
            </div>
        </div>
    	
        <% } // modal window to upload image %>

    </div> <!-- main / -->
<%@ include file="footer.jsp" %>
