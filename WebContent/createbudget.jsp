<%@ page import="budget.User" %>
<%@ page import="budget.Budget" %>
<%@ page import="budget.Annotation" %>
<%@ page import="budget.Attachment" %>
<%@ page import="budget.Document" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.Client" %>
<%@ page import="budget.Salesperson" %>
<%@ page import="budget.SectionProduct" %>
<%@ page import="budget.Section" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%@ page import="dao.FileType" %>
<%@ page import="storage.FileLocalStorage" %>
<%@ page import="dao.FileDAO" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.io.File" %>
<%@ include file="header.jsp" %>
<%
//retrieve parameters and the new budget
String budgetId = request.getParameter("budgetId");
User user = (User)session.getAttribute("user");
BudgetDAO budgetDAO = new BudgetDB();
LogDAO logDAO = new LogDB();
Budget budget = budgetDAO.getBudget(budgetId);
boolean isOffer = budget.isOffer();

String client;
if (budget.getClient() == null)
	client = "No hay cliente";
else
	client = budget.getClient().getName();

String constructionRef = budget.getConstructionRef();

String salesperson;
if (budget.getSalesperson() == null)
	salesperson = "No hay comercial";
else
	salesperson = budget.getSalesperson().getName();

String author;
if (budget.getAuthor() == null) {
	// error
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a un presupuesto sin autor", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
} else
	author = budget.getAuthor().getName();

String expirationDate;
if (budget.getExpirationDate() < 0)
	expirationDate = "No hay límite";
else
	expirationDate = budget.getExpirationDateString();

String creationDate = "Se fija al crearlo";

String note = budget.getNote();
if (note == null || note.length() < 1)
	note = "";

String signer = "Oferta no firmada";
if (isOffer && budget.getSigner() != null)
	signer = budget.getSigner().getName();

// add messages (Attribute messages)
Integer[] messages = (Integer[])request.getAttribute("messages");
/*
 * Integer[]: Empty
 * if the budget is correct. Errors:
 * -1 if isExpired, -2 if not have author,
 * -3 if not have client, -4 if the client
 * does not have salesperson, -5 if there
 * is an empty section, -6 if there are
 * no sections, -13 if the client is not
 * active, -14 if there are discontinued 
 * products. Advertises: 7 if there is
 * a product with no quantity, 8 if there
 * is no final note, 9 if there is no tax
 * rate, 10 if there is a section with no
 * name, 11 if there is any discount negative,
 * 12 if there is no construction reference,
 * 15 if the salesperson is not enabled.
 */
 
// Show creation errors (List errors)
// List<String> errors

// Show message (from SignOffer, CreateBudget and SetClient)
Integer message = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
String[] errors = (String[])request.getAttribute("errors");

logDAO.add(LogType.ACTION, user.getName() + " ha accedido a editar el presupuesto " + budgetId, System.currentTimeMillis());

// Show message
if (message != null) {
	if (message == 1) { %>
		<script type="text/javascript">
			setMessage("No se ha podido firmar la oferta. Compruebe los datos.",true);
	    </script>
	    <%
	}
	else if (message == 2) { %>
		<script type="text/javascript">
			setMessage("No se ha podido crear <% if (isOffer) { %>la oferta<% } else { %>el presupuesto<% } %>. Compruebe los datos.",true);
	    </script>
	    <%
	}
	else if (message == 3) { %>
		<script type="text/javascript">
			setMessage("No se ha podido establecer el cliente. Puede que el cliente no siga activo.",true);
	    </script>
		<%
	}
	else if (message == 4) { %>
		<script type="text/javascript">
			setMessage("Se ha producido un error.",true);
	    </script>
		<%
	}
	
}
%>

	<div class="info">
		<%
		// Show errors
		if (errors != null && errors.length < 0) { %>
			<div style="color:#C03">
	    		Se han producido los siguentes errores al crear <% if (isOffer) { %>la oferta<% } else { %>el presupuesto<% } %>: 
	   		</div>
	   		<%
			for (String error : errors) { %>
				<div style="color:#C03">
			    	<%= error %>
			    </div>
			    <%
			}
		}
		
		// Show messages
		if (messages != null && messages.length > 0) { %>
			<div style="color:#444">
			<% if (budget.isValid()) { %>Se ha validado con éxito<% } else { %>No se ha podido validar<% } %>
			<% if (isOffer) { %>la oferta<% } else { %>el presupuesto<% } %> con las siguientes observaciones: 
   			</div>
   			<%
			for (Integer messageNum : messages) {
				%>
				<div style="color:<% if (messageNum < 0) { %>#C03<% } else { %>#444<% } %>">
				<%
				switch(messageNum) {
					case -1: %>La fecha de validez no existe o ha pasado<% break;
					case -2: %>No hay autor<% break;
					case -3: %>No hay un cliente asignado<% break;
					case -4: %>El cliente no tiene un comercial asignado<% break;
					case -5: %>Hay capítulos sin productos<% break;
					case -6: %>Debe crear algún capítulo con productos<% break;
					case -13: %>El cliente no está activo, puede que sea un cliente antiguo<% break;
					case -14: %>Hay productos descatalogados<% break;
					case 7: %>Hay productos sin cantidad<%; break;
					case 8: %>La nota final está en blanco<%; break;
					case 9: %>No hay impuestos establecidos<%; break;
					case 10: %>Hay capítulos sin nombre<%; break;
					case 11: %>Hay descuentos negativos<%; break;
					case 12: %>La referencia de obra está en blanco<%; break;
					case 15: %>El comercial asignado al cliente está deshabilitado y no puede acceder a la aplicación<%; break;
					default: break;
				}
				%>
				</div>
				<%
			}
		}
		%>
    </div>

    <div id="main">
			
            <script type="text/javascript">

				// avoid loosing data when leaving without submitting all onchanges
				window.onbeforeunload = function(){ document.getElementById("tax").focus(); };
				window.onbeforeunload = function(){ document.getElementById("note").focus(); };
				window.onbeforeunload = function(){ document.getElementById("tax").focus(); };
				window.onbeforeunload = function(){ document.getElementById("note").focus(); };
				
				// check data validity every 15 seconds to show not saved data information
				setInterval("checkBudgetForm()", 15000);

				
			</script>
            
        	<div id="attributes">
            	<table>
                	<tr>
                    	<td class="attributetitle">
                    	<% if (isOffer) { %>Oferta<% } else { %>Presupuesto<% } %>
                        </td>
                        <td class="attributecontent" id="budgetid"><%= budgetId %>
                        </td>
                    </tr>
                    <tr>
                    	<td class="attributetitle">Cliente
                        </td>
                        <td class="attributecontent">
                        	<div class="lefttext">
                            	<%= client %>
                            </div>
                        	<div class="rightlink" id="openWindowSetClient">
                                <a href="#windowSetClient">Cambiar&nbsp;&nbsp;&nbsp;</a>
                            </div>
                        </td>
                    </tr>
                    <tr>
                    	<td class="attributetitle">Ref de obra
                        </td>
                        <td>
                        	<form id="constructionForm" action="SetConstructionRef" method="post" enctype="multipart/form-data">
            					<input type="hidden" name="budgetId" value="<%= budgetId %>" />
                                <input type="text" name="construction" id="construction" class="textbox" value="<%= constructionRef %>" />
                            </form>
                            <script type="text/javascript">
								document.getElementById("construction").onkeyup = function(){ sendForm("info", "SetConstructionRef" ,"constructionForm"); };
								document.getElementById("construction").onchange = function(){ sendForm("info", "SetConstructionRef" ,"constructionForm"); };
							</script>
                        </td>
                    </tr>
                    <tr>
                        <td class="attributetitle">Comercial
                        </td>
                        <td class="attributecontent"><%= salesperson %>
                        </td>
                    	<td class="attributetitle">Válido
                        </td>
                        <td>
                        	<form id="expirationForm" action="SetExpirationDate" method="post" enctype="multipart/form-data">
            					<input type="hidden" name="budgetId" value="<%= budgetId %>" />
                                <input type="text" name="expiration" id="expiration" class="textbox" value="<%= budget.getExpirationDate() > 0 ? budget.getExpirationDateString() : "" %>" />
                            </form>
                            <script type="text/javascript">
								//document.getElementById("expiration").onkeyup = function(){ checkExpirationDate(); };
								document.getElementById("expiration").onchange = function(){ if (checkExpirationDate()) sendForm("info", "SetExpirationDate" ,"expirationForm"); };
								document.getElementById("expiration").onkeyup = function(){ if (checkExpirationDate()) sendForm("info", "SetExpirationDate" ,"expirationForm"); };
							</script>
                        </td>
                    </tr>
                    <tr>
                        <td class="attributetitle">Creado por
                        </td>
                        <td class="attributecontent"><%= author %>
                        </td>
                    	<td class="attributetitle">Creado
                        </td>
                        <td class="attributecontent"><% if (isOffer) { %>Fecha firma oferta<% } else { %>Fecha creación presupuesto<% } %>
                        </td>
                    </tr>
               	<% if (isOffer) { %>
                	<tr>
                        <td class="attributetitle">Firmado por
                        </td>
                        <td class="attributecontent"><%= signer %>
                        </td>
                    </tr>
                <% } %>
                </table>
            </div>
                   
       <div class="bluebox">
                   
            <div id="sections">
            	<%@ include file="createsections.jsp" %>
            </div> <!-- sections -->
            
            <!-- total and tax -->
                
                <div class="linetitleC">
                    <div class="lefttext">
                        <h2>Importe total e impuestos</h2>
                    </div>
                </div>
                <table>
                	<tr>
                    	<th class="tablemargin">
                        </th>
                        <th class="attributetitle">Impuestos&nbsp;(%)
                        </th>
                        <th>
                        	<form id="taxForm" action="SetTaxRate" method="post" enctype="multipart/form-data">
                                <input type="hidden" name="budgetId" value="<%= budgetId %>" />
                                <input type="text" name="tax" id="tax" class="textbox" value="<%= df.format(budget.getTaxRate()).replace(",",".") %>" />
                            </form>
                            <script type="text/javascript">
								//document.getElementById("tax").onkeyup = function(){ checkTaxRate(); };
                                document.getElementById("tax").onkeyup = function(){ 
																			if (checkTaxRate()) { 
																				sendForm("sections", "SetTaxRate" ,"taxForm"); 
																			} };
								document.getElementById("tax").onchange = function(){ 
																			if (checkTaxRate()) { 
																				sendForm("sections", "SetTaxRate" ,"taxForm"); 
																			} };
                            </script>
                        </th>
                        <th class="tablemargin">
                        </th>
                        <th class="attributetitle">Total&nbsp;global
                        </th>
                        <td style="width:20px">
                        	<form id="globalTotalForm" action="SetGlobalTotal" method="post" enctype="multipart/form-data">
                        		<input type="hidden" name="budgetId" value="<%= budgetId %>" />
                        		<input type="checkbox" name="globalTotal" id="globalTotalChk" class="checkbox" 
                        		<% if (budget.hasGlobalTotal()) { %>
                        		checked="checked"
                        		<% }%>
                        		 />
                            </form>
                        	<script type="text/javascript">
                                document.getElementById("globalTotalChk").onchange = function(){ sendForm("sections", "SetGlobalTotal" ,"globalTotalForm"); setGlobalTotalMessage(); };
                            </script>
                        </td>
                        <th class="attributecontent" id="globalTotalDescription">
                        	<% if (budget.hasGlobalTotal()) { %>
                        		Desactívelo&nbsp;para&nbsp;que&nbsp;cada&nbsp;capítulo&nbsp;sea&nbsp;como&nbsp;un&nbsp;presupuesto&nbsp;independiente
                        	<% } else { %>
                        		Actívelo&nbsp;para&nbsp;que&nbsp;todos&nbsp;los&nbsp;capítulos&nbsp;se&nbsp;sumen&nbsp;al&nbsp;final
                        	<% } %>
                        </th>
                    </tr>
                    <tr>
                    	<th class="tablemargin">
                        </th>
                        <th><br />
                        </th>
                    </tr>
                </table>
                
            <!-- total and tax / -->         
            	
            <!-- note -->
                
                <div class="linetitleC">
                    <div class="lefttext">
                        <h2>Nota final</h2>
                    </div>
                </div>
                <table>
                	<tr>
                    	<th class="tablemargin">
                        </th>
                        <th>
                        	<form id="noteForm" action="SetNote" method="post" enctype="multipart/form-data">
                                <input type="hidden" name="budgetId" value="<%= budgetId %>" />
                                <textarea name="note" id="note" class="bigtextbox"><%= note %></textarea>
                            </form>
                            <script type="text/javascript">
                                document.getElementById("note").onkeyup = function(){ sendForm("info", "SetNote" ,"noteForm"); };
                                document.getElementById("note").onchange = function(){ sendForm("info", "SetNote" ,"noteForm"); };
                            </script>
                        </th>
                    </tr>
                    <tr>
                    	<th class="tablemargin">
                        </th>
                        <th><br />
                        </th>
                    </tr>
                </table>
                
            <!-- note / -->
                 
        </div> <!-- bluebox -->
            
        <div class="linetitleA">
        	<% if (budget.isValid()) { %>
        	<div class="linktext">
        		<a href="budget.jsp?budgetId=<%= budgetId %>">
        			<% if (budget.isOffer()) { %>Previsualizar oferta<% } else { %>Previsualizar presupuesto<% } %>
        		</a>
        	</div>
        	<% } %>
        	<div class="rightlink">
                <div class="linkpointer" id="checkBudgetLink">
                    <% if (budget.isOffer()) { %>Comprobar oferta<% } else { %>Comprobar presupuesto<% } %>
                </div>
            </div>
            <script type="text/javascript">
				document.getElementById("checkBudgetLink").onclick = function(){ if(checkBudgetForm()) location.href="CheckBudget?budgetId=<%= budgetId %>"; };
			</script>
		    		
        </div>
                    
        <!-- modal window to set client -->
        
        <div id="windowSetClient" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Seleccione el cliente</h2>
                	</div>
            	</div>
           
           		<div id="clientcontent">
                	
                </div> <!-- clientcontent -->
            </div>
            
            <script type="text/javascript">
				document.getElementById("openWindowSetClient").onclick = function(){ updateDiv("clientcontent","clientsearch.jsp?budgetId=<%= budgetId %>"); };
            </script>
                    
        </div>
    	
        <!-- modal window to set client / -->
            
    </div> <!-- main / -->
    
<%@ include file="footer.jsp" %>
