<%@ page import="budget.User" %>
<%@ page import="budget.Budget" %>
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
<%@ page import="java.text.DecimalFormat" %>
<%@ include file="header.jsp" %>
<%
String budgetId = request.getParameter("budgetId");
User user = (User)session.getAttribute("user");
LogDAO logDAO = new LogDB();
if ( budgetId == null || budgetId.length() < 1 ) {
	// ERROR, log and logout user
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a budget.jsp sin argumentos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}
BudgetDAO budgetDAO = new BudgetDB();
Budget budget = budgetDAO.getBudget(budgetId);

if (budget == null) {
	// ERROR, log and logout user
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a un presupuesto inexistente", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}

boolean isOffer = budget.isOffer();
boolean isCreated = (!isOffer && budget.getCreationDate() > 0) || (isOffer && budget.getSigner() != null);
// created if is a signed offer or is a budget with a creation date

if (isCreated && !user.hasPermission(Permission.ALLCLIENTS) && !budget.getSalesperson().equals(user) ) {
	// requieres budget visible by the user (is his client or can view all clients)
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a un presupuesto sin permisos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}
if (!isCreated && budget.getAuthor() != null && !budget.getAuthor().equals(user) && !user.hasPermission(Permission.ADMINISTRATE) ) {
	// only the author can view a budget not finished
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a un presupuesto inacabado sin permisos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}
if (isCreated && isOffer && !user.hasPermission(Permission.VIEWOFFERS) ) {
	// it is an offer and also requieres viewoffers
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a una oferta sin permisos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}

logDAO.add(LogType.ACTION, user.getName() + " ha visualizado el presupuesto " + budgetId, System.currentTimeMillis());

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

User author;
if (budget.getAuthor() == null) {
	// error
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a un presupuesto sin autor", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
} else
	author = budget.getAuthor();

String expirationDate;
if (budget.getExpirationDate() < 0)
	expirationDate = "No hay límite";
else
	expirationDate = budget.getExpirationDateString();

String creationDate;
if (!isCreated)
	creationDate = "Se fija al crearlo";
else
	creationDate = budget.getCreationDateString();
	
String note = budget.getNote();
if (note == null || note.length() < 1)
	note = "";

String signer = "Oferta no firmada";
if (isOffer && budget.getSigner() != null)
	signer = budget.getSigner().getName();

%>    
    <div id="main">
    
    <% if (isCreated) { %>
    	<div id="mainleft">
    <% } %>
    
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
                        <td class="attributecontent"><%= client %>
                        </td>
                    </tr>
                    <tr>
                    	<td class="attributetitle">Ref de obra
                        </td>
                        <td class="attributecontent"><%= constructionRef %>
                        </td>
                    </tr>
                    <tr>
                        <td class="attributetitle">Comercial
                        </td>
                        <td class="attributecontent"><%= salesperson %>
                        </td>
                    	<td class="attributetitle">Válido
                        </td>
                        <td class="attributecontent"><%= expirationDate %>
                        </td>
                    </tr>
                    <tr>
                        <td class="attributetitle">Creado por
                        </td>
                        <td class="attributecontent"><%= author.getName() %>
                        </td>
                    	<td class="attributetitle">Creado
                        </td>
                        <td class="attributecontent"><%= creationDate %>
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

            <!-- budget -->
                            
                <div class="bluebox">
                    <table>
                    <%
                    	int totalColspan = 4;
                    	int sectionColspan = 6;
                    	if (isOffer) {
                    		totalColspan = 8;
                    		sectionColspan = 10;
                    		
                    		if (user.hasPermission(Permission.ADMINISTRATE))
                    			sectionColspan = 12;
                    	}
                    	String cd;
                    	DecimalFormat df = new DecimalFormat();
                    	df.setGroupingUsed(false);
                    	df.setMaximumFractionDigits(2);
                    	df.setMinimumFractionDigits(2);
                    	DecimalFormat df0 = new DecimalFormat();
                    	df0.setGroupingUsed(false);
                    	df0.setMaximumFractionDigits(2);
                    	Section[] sections = budget.getSections();
                    	SectionProduct[] products;
                    	if (sections == null || sections.length < 1) { %>
                    	<tr>
                        	<th class="tablesuper">
                            	No hay capítulos
                            </th>
                        </tr>
                        <%
                    	} else {
                    		for (Section section : sections) { %>
                    			<tr>
		                        	<th colspan="<%= sectionColspan %>" class="tablesuper">
		                        	<% if (section.getName() != null) { %>
		                            	<%= section.getName() %>
		                            <% } %>
		                            </th>
		                        </tr>
		                        
		                        <tr>
		                        	<th>
		                            </th>
		                            <th class="tabletitle">Código
		                            </th>
		                            <th class="tabletitle">Nombre
		                            </th>
		                            <th class="tabletitle">Cantidad
		                            </th>
		                            <th class="tabletitle">Tarifa unidad
		                            </th>
		                            <% if (isOffer) { %>
		                            <th class="tabletitle">Dto. 1
		                            </th>
		                            <th class="tabletitle">Dto. 2
		                            </th>
		                            <th class="tabletitle">Dto. 3
		                            </th>
		                            <th class="tabletitle">P. Neto
		                            </th>
		                            <% } %>
		                            <th class="tabletitle">Total
		                            </th>
		                            <% if (isOffer && user.hasPermission(Permission.ADMINISTRATE)) { %>
		                            <th class="tabletitle">P. Coste
		                            </th>
		                            <th class="tabletitle">Beneficio
		                            </th>
		                            <% } %>
		                        </tr>
                    		<%	
                    			products = section.getProducts();
                    			if (products == null || products.length < 1) { %>
                    				<tr>
			                        	<td>
			                            </td>
			                            <td class="tablecontentD" colspan="<%= sectionColspan - 1 %>">
			                            	No hay productos en la sección
			                            </td>
			                        </tr>
                    			<%
                    			} else {
                    				cd = "D";
                    				for (SectionProduct product : products) { %>
                    					<tr>
				                        	<td>
				                            </td>
				                            <td class="tablemaincontent<%= cd %>">
				                            	<a href="product.jsp?productId=<%= product.getProductId() %>">
				                            	<%= product.getProductId() %></a>
				                            </td>
				                            <td class="tablecontent<%= cd %>"><%= product.getName() %>
				                            </td>
				                            <td class="tablecontent<%= cd %>"><%= df0.format(product.getQuantity()) %>
				                            </td>
				                            <td class="tablecontent<%= cd %>">
				                            <% if (!isCreated) { %>
				                            <%= df.format(product.getCurrentPrice()) %>&euro;
				                            <% } else { %>
				                            <%= df.format(product.getPrice(budget.getCreationDate())) %>&euro;
				                            <% } %>
				                            </td>
				                            <% if (isOffer) { %>
				                            <th class="tablecontent<%= cd %>"><%= df.format(product.getDiscount1()) %>%
				                            </th>
				                            <th class="tablecontent<%= cd %>"><%= df.format(product.getDiscount2()) %>%
				                            </th>
				                            <th class="tablecontent<%= cd %>"><%= df.format(product.getDiscount3()) %>%
				                            </th>
				                            <th class="tablecontent<%= cd %>">
				                            <% if (!isCreated) { %>
				                            <%= df.format(product.getCurrentNetPrice()) %>&euro;
				                            <% } else { %>
				                            <%= df.format(product.getNetPrice(budget.getCreationDate())) %>&euro;
				                            <% } %>
				                            </th>
				                            <% } %>
				                            <td class="tablecontent<%= cd %>">
				                            <% if (!isCreated) { %>
				                            <%= df.format(product.getCurrentTotal()) %>&euro;
				                            <% } else { %>
				                            <%= df.format(product.getTotal(budget.getCreationDate())) %>&euro;
				                            <% } %>
				                            </td>
				                            <% if (isOffer && user.hasPermission(Permission.ADMINISTRATE)) { %>
				                            <th class="tablecontent<%= cd %>"><%= df.format(product.getCostPrice()) %>&euro;
				                            </th>
				                            <th class="tablecontent<%= cd %>">
				                            <% if (!isCreated) { %>
				                            <%= df.format(product.getCurrentProfitMargin()) %>%
				                            <% } else { %>
				                            <%= df.format(product.getProfitMargin(budget.getCreationDate())) %>%
				                            <% } %>
				                            </th>
				                            <% } %>
				                        </tr>
                    				<%
	                    				if (cd.equals("D"))
	                    		           	   cd = "C";
	                    		           else
	                    		               cd = "D";
                    				} // for products
                    			} // else products
                    			
                    			// section total
                    			if (budget.hasGlobalTotal()) { %>
                    				<tr> 
		                            	<th colspan="<%= totalColspan %>">
		                                </th>
		                                <th class="tabletitle">Subtotal
		                                </th>
		                                <th class="tabletitle">
		                                <% if (isCreated) { %>
			                                <%= df.format(section.getTotal(budget.getCreationDate())) %>&euro;
		                                <% } else { %>
		                                	<%= df.format(section.getCurrentTotal()) %>&euro;
		                                <% } %>
		                                </th>
		                            </tr>
		                            <tr> 
		                                <th class="tablemargin">&nbsp;
		                                </th>
		                            </tr>
                    			<%	
                    			} else { %>
                    				<tr>
			                        	<th colspan="<%= totalColspan %>">
			                            </th>
			                            <th class="tabletitle">Importe
			                            </th>
			                            <th class="tabletitle">
										<% if (isCreated) { %>
			                                <%= df.format(section.getTotal(budget.getCreationDate())) %>&euro;
		                                <% } else { %>
		                                	<%= df.format(section.getCurrentTotal()) %>&euro;
		                                <% } %>
			                            </th>
			                        </tr>
			                        <tr>
			                        	<th colspan="<%= totalColspan %>">
			                            </th>
			                            <th class="tabletitle">Impuestos
			                            </th>
			                            <th class="tabletitle"><%= df.format(budget.getTaxRate()) %>%
			                            </th>
			                        </tr>
			                        <tr>
			                        	<th colspan="<%= totalColspan %>">
			                            </th>
			                            <th class="tabletitlebig">TOTAL
			                            </th>
			                            <th class="tabletitlebig">
			                            <% if (isCreated) { %>
			                                <%= df.format(section.getTotal(budget.getCreationDate()) * (budget.getTaxRate()/100 + 1) ) %>&euro;
		                                <% } else { %>
		                                	<%= df.format(section.getCurrentTotal() * (budget.getTaxRate()/100 + 1) ) %>&euro;
		                                <% } %>
			                            </th>
			                        </tr>
			                        <tr> 
		                                <th class="tablemargin">&nbsp;
		                                </th>
		                            </tr>
                    			<%	
                    			}

                    		} // for sections

                    		// Budget Total
                    		
                    		if (budget.hasGlobalTotal()) { %>
                    		
	                    	<tr> 
	                            <th>&nbsp;
	                            </th>
	                        </tr>
	                        <tr> 
	                            <th>&nbsp;
	                            </th>
	                        </tr>
	                        
	                        <tr>
	                        	<th colspan="<%= totalColspan %>">
	                            </th>
	                            <th class="tabletitle">Importe
	                            </th>
	                            <th class="tabletitle">
									<% if (isCreated) { %>
		                                <%= df.format(budget.getTotal()) %>&euro;
	                                <% } else { %>
	                                	<%= df.format(budget.getCurrentTotal()) %>&euro;
	                                <% } %>
	                            </th>
	                        </tr>
	                        <tr>
	                        	<th colspan="<%= totalColspan %>">
	                            </th>
	                            <th class="tabletitle">Impuestos
	                            </th>
	                            <th class="tabletitle"><%= df.format(budget.getTaxRate()) %>%
	                            </th>
	                        </tr>
	                        <tr>
	                        	<th colspan="<%= totalColspan %>">
	                            </th>
	                            <th class="tabletitlebig">TOTAL
	                            </th>
	                            <th class="tabletitlebig">
	                            	<% if (isCreated) { %>
		                                <%= df.format(budget.getTotalPlusTaxes()) %>&euro;
	                                <% } else { %>
	                                	<%= df.format(budget.getCurrentTotalPlusTaxes()) %>&euro;
	                                <% } %>
	                            </th>
	                        </tr>
                        
                    	<%
                    		} // hasGlobalTotal
                    	} // else sections
                    %>

                    </table>
                
            <% if (isCreated) { %>                 
            	</div> <!--bluebox-->
            
                <div class="linetitleA">
                    <div class="linktext">
                        <a href="NewBudget?action=template&amp;budgetId=<%= budgetId %>">
                        <% if (isOffer) { %>
                        	Reutilizar como presupuesto
                        <% } else { %>
                        	Reutilizar presupuesto
                        <% } %>
                        </a>
                    </div>
                    <% if (user.hasPermission(Permission.CREATEOFFERS)) { %>
                    <div class="rightlink">
                        <a href="NewBudget?isOffer=1&amp;action=template&amp;budgetId=<%= budgetId %>">
                        <% if (isOffer) { %>
                        	Reutilizar oferta
                        <% } else { %>
                        	Reutilizar como oferta
                        <% } %>
                        </a>
                    </div>
                    <% } %>
                </div>
            <% } // if not isCreated bluebox is closed where greybox closes %> 
            <!-- budget / -->
            
            <!-- print info -->
            
            <% if (isCreated) { %> 
            <div class="greybox">
            	<!-- cover -->
                <div class="linetitleA">
                    <div class="lefttext">
                        <h2>Portada</h2>
                    </div>
                    <div class="rightlink">
                    </div>
                </div>
               
                <div id="coverlist"> <!-- to update when adding covers -->
	                <%@ include file="covers.jsp" %>
            	</div>
                
                <!-- cover / -->
            <% } // if isCreated show covers %>  
                
                <!-- note -->
                
                <div class="linetitle<% if (isCreated) { %>B<% } else { %>C<% } %>">
                    <div class="lefttext">
                        <h2>Nota final</h2>
                    </div>
                </div>
                <div>
                	<%= note %>
               	</div>
                
                <!-- note / -->
                
            <% if (isCreated) { %>                    
                <!-- attachments -->
                
                <div class="linetitleB">
                    <div class="lefttext">
                        <h2>Anexos</h2>
                    </div>
                    <div class="rightlink">
            			<a href="#windowUploadAttachment"><img src="images/add.png" alt="Añadir" class="imgbuttonbig" id="addattachment" /></a>
            		</div>
                </div>
                
                <div id="attachmentlist"> <!-- to update when add/sort/remove -->
	                <%@ include file="attachments.jsp" %>
                </div>
                
                <!-- attachments / -->
                             
                <div class="linetitleB">
                    <div class="linktext">
                        <a href="Download?budgetId=<%= budgetId %>">Descargar como PDF</a>
                    </div>
                    <% if (budget.getClient().getEmail() != null && budget.getClient().getEmail().length() > 0) { %>
                    <div class="rightlink">
                        <a href="#windowSendEmail">Enviar por e-mail</a>
                    </div>
                    <% } %>
                </div>
                
            <% } // if isCreated show attachments and options %>      
            
            </div>  <!-- box / -->
            
            <% 
            if (!isCreated) {
            	// Links to Modify, Create budget / Create offer, Sign offer %>
            <div class="linetitleA">
		        <div class="linktext">
		        	<% if (budget.getCreationDate() <= 0 || user.hasPermission(Permission.ADMINISTRATE)) {
		        		// not created or offer waiting for sign and user is administrator %>
			        	<a href="createbudget.jsp?budgetId=<%= budgetId %>">
			        	Editar <% if (isOffer) { %>oferta<% } else { %>presupuesto<% } %></a>
		        	<% } %>
		        </div>
		    	<div class="rightlink">
		    		<% if (budget.isValid()) {
		    			if (isOffer && user.hasPermission(Permission.ADMINISTRATE) && budget.getCreationDate() > 0) { %>
		    				<a href="SignOffer?budgetId=<%= budgetId %>">Firmar oferta</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		    			<% }
		    			if (user.equals(author) && !isOffer) { %>
		    				<a href="CreateBudget?budgetId=<%= budgetId %>">Crear presupuesto</a>
		    			<% }
		    			if (user.equals(author) && isOffer && budget.getCreationDate() <= 0) { %>
	    					<a href="CreateBudget?budgetId=<%= budgetId %>">Crear borrador de oferta</a>
		    			<% }
		    		} // if isValid %>
            <%
            %>	
		    	</div>
			</div>            	
            <% } // !isCreated links %>
            
            <!-- print info / -->
	<% if (isCreated) { %>
        </div> <!-- mainleft -->
       
        <!-- right side -->
        <div id="mainright">
        
        	<div class="linetitleA">
            	<div class="lefttext">
            		<h2>Documentos</h2>
                </div>
                <div class="rightlink">
            		<a href="#windowUploadDocument"><img src="images/add.png" alt="Añadir" class="imgbuttonbig" id="adddocument" /></a>
            	</div>
            </div>
           
           <div id="documentlist"> <!-- to update when add/remove -->
	           <%@ include file="documents.jsp" %>
            </div>
            
            <div class="linetitleB">
            	<div class="lefttext">
            		<h2>Notas</h2>
                </div>
                <div class="rightlink">
            		<img src="images/add.png" alt="Añadir" class="imgbuttonbig" id="addnote" />
            	</div>
            </div>
            
            <form id="addnoteForm" action="AddNote" method="post" enctype="multipart/form-data">
                <textarea name="text" class="addnotetext" id="addnotetext"></textarea>
                <input type="hidden" name="budgetId" value="<%= budgetId %>" />
            </form>
            
            <div id="noteslist"> <!-- to update when adding notes -->
	            <%@ include file="notes.jsp" %>
	            <script type="text/javascript">
					// add listener for adding notes
					document.getElementById("addnote").onclick = function(){ addNote(); };
                </script>
            </div>
            
            <div class="bluebox">
            <details>
                <summary class="tablesuper">
                        Versiones
                </summary>
                
                    <table>
                        <tr> 
                            <th class="tabletitle">Tipo
                            </th>
                            <th class="tabletitle">Número
                            </th>
                            <th class="tabletitle">Fecha
                            </th>
                        </tr>
                        
                        <%
		                	// retrieve family and show
		                	Budget[] family = budgetDAO.getFamily(budgetId);
                        	String familyId = budgetId;
                     
                        	if (family == null || family.length<1) { %>
                        		<tr>
		                            <td class="tablecontentA" colspan="3">No disponible
		                            </td>
		                        </tr>
                        <%	} else {
                        		for (Budget familiar: family) {
                        		// check if the user can view the budget
                        		if((user.equals(familiar.getSalesperson()) || user.hasPermission(Permission.ALLCLIENTS)) 
                						&& ( (familiar.isOffer() && user.hasPermission(Permission.VIEWOFFERS) )
                								|| !familiar.isOffer() ) ) {
                        		
                        		%>
                        			<tr>
			                            <td class="tablecontentA">
			                            	<% if (familiar.isOffer()) { %>
			                            		Oferta
			                            	<% } else { %>
			                            		Presupuesto
			                            	<% } %>
			                            </td>
			                            <td class="tablemaincontentA">
			                            	<a href="budget.jsp?budgetId=<%= familiar.getBudgetId() %>">
			                            	<%= familiar.getBudgetId() %></a>
			                            </td>
			                            <td class="tablecontentA"><%= familiar.getCreationDateString() %>
			                            </td>
			                        </tr>
			                        
			            <%			} // end if permission           
                        		} // end for
                        } // end if
		                %>
                    </table>
                
                </details>
                
                <div class="linetitleC">
                    <div class="linktext">
                        <a href="NewBudget?action=modify&amp;budgetId=<%= budgetId %>">
                        <% if (isOffer) { %>
                        Presupuestar
                        <% } else { %>
                        Modificar
                        <% } %>
                        </a>
                    </div>
                    <% if (user.hasPermission(Permission.CREATEOFFERS)) { %>
                    <div class="rightlink">
                        <a href="NewBudget?isOffer=1&amp;action=modify&amp;budgetId=<%= budgetId %>">
                        <% if (isOffer) { %>
                        Modificar
                        <% } else { %>
                        Ofertar
                        <% } %>
                        </a>
                    <% } %>
                    </div>
                </div>
                
            </div> <!--bluebox-->
            
        </div> <!-- mainright / -->
        
        <!-- modal window to upload documents -->
        
        <div id="windowUploadDocument" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Cargar documentos</h2>
                	</div>
            	</div>
           
                <div class="lefttext">
                    Seleccione el fichero a cargar para añadirlo a los documentos <% if (isOffer) { %>de la oferta<% } else { %>del presupuesto<% } %>.
                </div>
                <form id="uploadDocument" action="UploadDocument" method="post" enctype="multipart/form-data">
                	<input type="hidden" name="budgetId" value="<%= budgetId %>" />
                    <input type="file" name="documentToUpload" id="documentToUpload" onchange="fileSelected('document');" class="fileSelector" />
                    <input type="button" onclick="uploadFile('document')" value="Cargar documento" class="fileSelectorBtn" id="documentToUploadBtn" />
                </form>
                <div class="progress"><p>&nbsp;</p></div>
            </div>
        </div>
    	
        <!-- modal window to upload documents / -->
         
        <!-- modal window to upload covers -->
        
        <div id="windowUploadCover" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Cargar portadas</h2>
                	</div>
            	</div>
           
                <div class="lefttext">
                    Seleccione el fichero de imagen a cargar para usarla en la portada <% if (isOffer) { %>de la oferta<% } else { %>del presupuesto<% } %>.
                </div>
                <form id="uploadCover" action="UploadCover" method="post" enctype="multipart/form-data">
                	<input type="hidden" name="budgetId" value="<%= budgetId %>" />
                    <input type="file" name="coverToUpload" id="coverToUpload" onchange="fileSelected('cover');" class="fileSelector" />
                    <input type="button" onclick="uploadFile('cover')" value="Cargar portada" class="fileSelectorBtn" id="coverToUploadBtn" />
                </form>
                <div class="progress"><p>&nbsp;</p></div>
            </div>
        </div>
    	
        <!-- modal window to upload covers / -->
        
        <!-- modal window to upload attachments -->
        
        <div id="windowUploadAttachment" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Cargar adjuntos</h2>
                	</div>
            	</div>
           
                <div class="lefttext">
                    Seleccione el fichero PDF para añadirlo al final <% if (isOffer) { %>de la oferta<% } else { %>del presupuesto<% } %>.
                </div>
                <form id="uploadAttachment" action="UploadAttachment" method="post" enctype="multipart/form-data">
                	<input type="hidden" name="budgetId" value="<%= budgetId %>" />
                    <input type="file" name="attachmentToUpload" id="attachmentToUpload" onchange="fileSelected('attachment');" class="fileSelector" />
                    <input type="button" onclick="uploadFile('attachment')" value="Cargar adjunto" class="fileSelectorBtn" id="attachmentToUploadBtn" />
                </form>
                <div class="progress"><p>&nbsp;</p></div>
            </div>
        </div>
    	
        <!-- modal window to upload attachments / -->
        
        <!-- modal window to send email -->
        <% if (budget.getClient().getEmail() != null && budget.getClient().getEmail().length() > 0) { %>
        <div id="windowSendEmail" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Enviar por e-mail</h2>
                	</div>
            	</div>
                <table>
                	<tr>
                    	<td>
                    		<div class="lefttext">
			                    <% if (isOffer) { %>La oferta<% } else { %>El presupuesto<% } %> se enviará 
			                    como adjunto en un mensaje de correo electrónico, usando como remitente la 
			                    dirección de correo electrónico 
			                    <% if (budget.getSalesperson().hasEmailPassword()) { %>
			                    <%= budget.getSalesperson().getEmail() %><% } else { %>
			                    la de la aplicación<% } %>, y como nombre 
			                    <%= budget.getSalesperson().getName() %>. <br />
			                    El destinatario será <%= budget.getClient().getName() %> (
			                    <%= budget.getClient().getEmail() %>).
                    		</div>
                    	</td>
                    </tr>
                    <tr>
                    	<td>
                    		<br />
                    		<div id="infoEmail">
                    		</div>
                    	</td>
                    </tr>
                    <tr>
                    	<td>
                    		<div class="linktext">
		                        <a href="#closeModalWindow">Cancelar</a>
		                    </div>
                   			<div class="rightlink" id="emailBudget">
                   				<div class="linkpointer">
                       				Enviar e-mail
                       			</div>
                   			</div>
                    	</td>
                    </tr>
                </table>
                <script type="text/javascript">
                	document.getElementById("emailBudget").onclick = 
                		function(){ 
                			updateDiv("infoEmail", "EmailBudget?budgetId=<%= budgetId %>");
                		};
                </script>
	        </div>
	    </div>
    	
    <!-- modal window to send email / -->
    <% } // email window %>

    <% } // only show right side if isCreated %>         
    </div> <!-- main / -->

<%@ include file="footer.jsp" %>