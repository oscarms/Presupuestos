<%@ include file="header.jsp" %>
<%
String email = (String)request.getAttribute("email");
								
if (email == null)
	email = "";
%>
<div id="main">
                
        <div id="windowLogin" class="modalWindow">
            <div>
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Iniciar sesión</h2>
                	</div>
            	</div>
                           
                <div class="lefttext">
                    Introduzca su dirección de correo electrónico y contraseña para iniciar sesión<br />
                    <br  />
                </div>
                <form id="login" action="Login" method="post" enctype="multipart/form-data">
                    <table>
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
                            <td style="width:145px" class="attributetitle">Contraseña
                            </td>
                            <td style="width:auto"><input type="password" name="password" id="password" class="textbox" />
                            </td>
                        </tr>
                        <tr>
                        	<td class="tablemargin">
                        	</td>
                        	<td colspan="2">
                        	
                        	</td>
                        </tr>
                        <tr>
                        	<td class="tablemargin">
                        	</td>
                        	<td colspan="3">
                        		<div class="lefttext" id="error">
                        		<%
								Integer message = (Integer)request.getAttribute("message");

								if (message == null)
									message = 0;
								
								if (message == 1) {
									%>
									<br />
									<div style="color:#C03"><%= email %> ha superado el límite de intentos para iniciar sesión. <br />
				                    Contacte con el administrador.<br  /></div>
									<%
								} else if (message == 2) {
									%>
									<br />
									<div style="color:#C03"><%= email %> no está habilitado para iniciar sesión. <br />
				                    Contacte con el administrador.<br  /></div>
									<%
								} else if (message == 3) {
									%>
									<br />
									<div style="color:#C03">Dirección de correo electrónico o contraseña no válida <br /></div>
									<%
								}
								
				                %>
				                </div>
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
										Iniciar&nbsp;sesión
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </table>
                    <input type="submit" class="hidden" />
                </form>
            </div>
        </div>
    	
        <script type="text/javascript">
			window.location = '#windowLogin';
			document.getElementById("submittext").onclick = function(){ login(); };
			
			// used in the login form
			function login() {
				if ( (document.getElementById("email").value.length > 0) && (document.getElementById("password").value.length > 0) ) {
					document.getElementById("login").submit();
				} else {
					document.getElementById("error").innerHTML = "<div style='color:#C03'><br />No puede dejar la dirección de correo electrónico ni la contraseña en blanco<br /></div>";
				}
			}
        </script>
                        
    </div> <!-- main / -->
    
<%@include file="footer.jsp" %>