                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Cambia tu contraseña</h2>
                	</div>
            	</div>
           
                <form id="changePassword" action="ChangePassword" method="post" enctype="multipart/form-data">
                    <table>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:170px" class="attributetitle">Contraseña anterior
                            </td>
                            <td style="width:auto"><input type="password" name="oldPassword" id="oldPassword" class="textbox" />
                            </td>
                        </tr>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:170px" class="attributetitle">Nueva contraseña
                            </td>
                            <td style="width:auto"><input type="password" name="newPassword" id="newPassword" class="textbox" />
                            </td>
                        </tr>
                        <tr>
                            <td class="tablemargin">
                            </td>
                            <td style="width:170px" class="attributetitle">Confirmar contraseña
                            </td>
                            <td style="width:auto"><input type="password" name="confirmPassword" id="confirmPassword" class="textbox" />
                            </td>
                        </tr>
                        <tr>
                        	<td class="tablemargin">
                        	</td>
                        	<td colspan="2">
                        	    <div class="lefttext" id="infoChangePassword">
				           		<%
				           		Integer messagePwd = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
								if (messagePwd == null)
									messagePwd = 0;
								
								else if (messagePwd == 11) {
								%>
									<div style='color:#444'><br  />Se ha cambiado la contraseña<br /></div>
								<%
								} else if (messagePwd == 12) {
								%>
									<div style='color:#C03'><br  />Introduzca la misma contraseña en los campos<br  />
									"Nueva contraseña" y "Confirmar"<br  /></div>
								<%
								} else if (messagePwd == 13) {
								%>
									<div style='color:#C03'><br  />La contraseña anterior no es válida<br  /></div>
								<%
								} else if (messagePwd == 14) {
								%>
									<div style='color:#C03'><br  />Se ha producido un error<br  /></div>
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
                                    <div id="submitpassword" class="linkpointer">
                                        Cambiar&nbsp;contraseña
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </table>
            </form>
            
            <script type="text/javascript">
                document.getElementById("submitpassword").onclick = function(){ submitPassword(); };
            </script>
            