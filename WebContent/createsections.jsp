<%@ page import="budget.User" %>
<%@ page import="budget.Budget" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.SectionProduct" %>
<%@ page import="budget.Section" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%@ page import="java.text.DecimalFormat" %>
<%
// initializate and get parameters
User userS = (User)session.getAttribute("user");
BudgetDAO budgetDAOS = new BudgetDB();
LogDAO logDAOS = new LogDB();
String budgetIdS = request.getParameter("budgetId");

Budget budgetS = budgetDAOS.getBudget(budgetIdS);

logDAOS.add(LogType.ACTION, userS.getName() + " ha visitado la edición del presupuesto " + budgetIdS, System.currentTimeMillis());

boolean isOfferS = budgetS.isOffer();
Section[] sections = budgetS.getSections();
SectionProduct[] products;
SectionProduct product;
boolean isAdministrator = userS.hasPermission(Permission.ADMINISTRATE);


// table draw variables
DecimalFormat df = new DecimalFormat();
df.setGroupingUsed(false);
df.setMaximumFractionDigits(2);
df.setMinimumFractionDigits(2);
DecimalFormat df0 = new DecimalFormat();
df0.setGroupingUsed(false);
df0.setGroupingUsed(false);
df0.setMaximumFractionDigits(2);

String cd;
int totalColspan = 4;
int sectionColspan = 6;
if (isOfferS) {
	totalColspan = 8;
	sectionColspan = 10;
	
	if (isAdministrator)
		sectionColspan = 12;
}
boolean firstS;
boolean lastS;
boolean firstP;
boolean lastP;
 %>

                    <table>
                    	<%
                    	if (sections == null || sections.length < 1) { %>
                    	<tr>
                        	<th class="tablesuper">
                            	No hay capítulos
                            </th>
                        </tr>
                        <%
                    	} else {
                    		Section section;
                    		firstS = true;
                    		lastS = false;
                    		for (int i = 0; i < sections.length; i++) { 
                    			section = sections[i];
                    			lastS = (i == sections.length - 1);
                    		%>
                        <tr>
                            <th colspan="<%= sectionColspan %>" class="tablesuper">
                                <form id="nameSection<%= Integer.toString(section.getSectionId()) %>Form" action="RenameSection" method="post" enctype="multipart/form-data">
                                    <input type="hidden" name="budgetId" value="<%= budgetIdS %>" />
                                    <input type="hidden" name="sectionId" value="<%= Integer.toString(section.getSectionId()) %>" />
                                    <input type="text" name="name" id="nameSection<%= Integer.toString(section.getSectionId()) %>" class="sectiontextbox" value="<%= section.getName() %>" />
                                </form>
                                <script type="text/javascript">
                                	document.getElementById("nameSection<%= Integer.toString(section.getSectionId()) %>").onkeyup = function(){ sendForm("info", "RenameSection" ,"nameSection<%= Integer.toString(section.getSectionId()) %>Form"); };
                                	document.getElementById("nameSection<%= Integer.toString(section.getSectionId()) %>").onchange = function(){ sendForm("info", "RenameSection" ,"nameSection<%= Integer.toString(section.getSectionId()) %>Form"); };
                                </script>
                            </th>
                            <th>
                            	<div class="rightlink">
                                    <img src="images/up.png" alt="Mover al anterior" class="<% if (firstS) { %>imgbuttonsmalldisabled<% } else { %>imgbuttonsmall<% } %>" id="section<%= Integer.toString(section.getSectionId()) %>sortup" />
                                    <img src="images/down.png" alt="Mover al posterior" class="<% if (lastS) { %>imgbuttonsmalldisabled<% } else { %>imgbuttonsmall<% } %>" id="section<%= Integer.toString(section.getSectionId()) %>sortdown" />
                                    <img src="images/bin.png" alt="Eliminar" class="<% if (section.getProducts() == null || section.getProducts().length < 1) { %>imgbuttonsmall<% } else { %>imgbuttonsmalldisabled<% } %>" id="section<%= Integer.toString(section.getSectionId()) %>remove" />
                                </div>
                                <script type="text/javascript">
									// add listeners for sorting and removing attachments
									document.getElementById("section<%= section.getSectionId() %>sortup").onclick = function(){ updateDiv('sections','Sort?sectionId=<%= Integer.toString(section.getSectionId()) %>&budgetId=<%= budgetIdS %>&sort=-1'); };
									document.getElementById("section<%= section.getSectionId() %>sortdown").onclick = function(){ updateDiv('sections','Sort?sectionId=<%= Integer.toString(section.getSectionId()) %>&budgetId=<%= budgetIdS %>&sort=1'); };
									document.getElementById("section<%= section.getSectionId() %>remove").onclick = function(){ updateDiv('sections','Remove?sectionId=<%= Integer.toString(section.getSectionId()) %>&budgetId=<%= budgetIdS %>'); };
								</script>
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
                            <% if (isOfferS) { %>
                            <th class="tabletitle">Dto. 1 (%)
                            </th>
                            <th class="tabletitle">Dto. 2 (%)
                            </th>
                            <th class="tabletitle">Dto. 3 (%)
                            </th>
                            <th class="tabletitle">P. Neto (&euro;)
                            </th>
                            <% } %>
                            <th class="tabletitle">Total
                            </th>
                            <% if (isOfferS && isAdministrator) { %>
                            <th class="tabletitle">P. Coste
                            </th>
                            <th class="tabletitle">Beneficio (%)
                            </th>
                            <% } %>
                            <th class="tabletitle" style="width:60px;">
                            </th>
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
                    				firstP = true;
                    				for (int p = 0; p < products.length; p++) {
                    					product = products[p];
                    					lastP = (p == products.length - 1);
                    					%>
                        <tr>
                            <td>
                            </td>
                            <td class="tablemaincontent<%= cd %>"><a href="product.jsp?productId=<%= Integer.toString(product.getProductId()) %>"><%= Integer.toString(product.getProductId()) %></a>
                            </td>
                            <td class="tablecontent<%= cd %>"><%= product.getName() %>
                            </td>
                            <td class="tablecontent<%= cd %>">
                            	<form id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantityForm" action="SetProductQuantity" method="post" enctype="multipart/form-data">
                                    <input type="hidden" name="budgetId" value="<%= budgetIdS %>" />
                                    <input type="hidden" name="sectionId" value="<%= Integer.toString(section.getSectionId()) %>" />
                                    <input type="hidden" name="productId" value="<%= Integer.toString(product.getProductId()) %>" />
                                    <input type="text" name="quantity" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity" class="smalltextbox" value="<%= df0.format(product.getQuantity()).replace(",",".") %>" />
                                </form>
                                <script type="text/javascript">
									//document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity").onkeyup = function(){ checkPositiveFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity"); };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity").onkeyup = function(){
																										if (checkPositiveFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity")) {
																											calculateBudget(); 
																											sendForm("info", "SetProductQuantity" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantityForm");
																										} };									
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity").onchange = function(){
																										if (checkPositiveFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantity")) {
																											calculateBudget(); 
																											sendForm("info", "SetProductQuantity" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>quantityForm");
																										} };
                                </script>
                            </td>
                            <td class="tablecontent<%= cd %>" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>price"><%= df.format(product.getCurrentPrice()) %>&euro;
                            </td>
                            <% if (isOfferS) { %>
                            <td class="tablecontent<%= cd %>">
                                <form id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts" action="SetDiscounts" method="post" enctype="multipart/form-data">
                                    <input type="hidden" name="budgetId" value="<%= budgetIdS %>" />
                                    <input type="hidden" name="sectionId" value="<%= Integer.toString(section.getSectionId()) %>" />
                                    <input type="hidden" name="productId" value="<%= Integer.toString(product.getProductId()) %>" />
                                	<input type="text" name="discount1" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1" class="smalltextbox" value="<%= df.format(product.getDiscount1()).replace(",",".") %>" />
                            </td>
                            <td class="tablecontent<%= cd %>">
                                	<input type="text" name="discount2" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2" class="smalltextbox" value="<%= df.format(product.getDiscount2()).replace(",",".") %>" />
                            </td>
                            <td class="tablecontent<%= cd %>">
                                	<input type="text" name="discount3" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3" class="smalltextbox" value="<%= df.format(product.getDiscount3()).replace(",",".") %>" />
                                </form>
                            </td>
                            <td class="tablecontent<%= cd %>">
                                <input type="text" name="netPrice" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice" class="smalltextbox" value="<%= df.format(product.getCurrentNetPrice()).replace(",",".") %>" />
                                <script type="text/javascript"> // 4 textboxes que hacen 3xcheckFloat 1xsetNetPriceDiscounts y luego calculateBudget y SetDiscounts
									//document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1").onkeyup = function(){ checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1"); };
									//document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2").onkeyup = function(){ checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2"); };
									//document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3").onkeyup = function(){ checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3"); };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice").onkeyup = function(){ checkPositiveFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice"); };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice").onclick = function() { setMessage("Esta casilla no se guarda automáticamente. Seleccione otra casilla para guardarla",true); };
									
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1").onkeyup = function(){
																										if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1")) {
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2").onkeyup = function(){
																										if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2")) {
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3").onkeyup = function(){
																										if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3")) {
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
									//document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice").onkeyup = function(){
									//																	if (checkPositiveFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice")) {
									//																		setNetPriceDiscounts(<%= Integer.toString(section.getSectionId()) %>,<%= Integer.toString(product.getProductId()) %>);
									//																		calculateBudget();
									//																		sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
									//																	} };

									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1").onchange = function(){
																										if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount1")) {
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2").onchange = function(){
																										if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount2")) {
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3").onchange = function(){
																										if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discount3")) {
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice").onchange = function(){
																										if (checkPositiveFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>netPrice")) {
																											setNetPriceDiscounts(<%= Integer.toString(section.getSectionId()) %>,<%= Integer.toString(product.getProductId()) %>);
																											calculateBudget();
																											sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																										} };
                            	</script>
                            </td>
                            <% } // if isOfferS %>
                            <td class="tablecontent<%= cd %>" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>total"><%= df.format(product.getCurrentTotal()) %>&euro;
                            </td>
                            <% if (isOfferS && isAdministrator) { %>
                            <td class="tablecontent<%= cd %>" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>costPrice"><%= df.format(product.getCostPrice()) %>&euro;
                            </td>
                            <td class="tablecontent<%= cd %>">
                                <input type="text" name="profitMargin" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin" class="smalltextbox" value="<%= df.format(product.getCurrentProfitMargin()).replace(",",".") %>" />
                                <script type="text/javascript"> // textbox que hace setProfitMarginDiscounts y luego calculateBudget y SetDiscounts
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin").onkeyup = function(){ checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin"); };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin").onclick = function() { setMessage("Esta casilla no se guarda automáticamente. Seleccione otra casilla para guardarla",true); };

									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin").onchange = function(){
																											if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin")) {
																												setProfitMarginDiscounts(<%= Integer.toString(section.getSectionId()) %>,<%= Integer.toString(product.getProductId()) %>);
																												calculateBudget(); 
																												sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
																											} };
									//document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin").onkeyup = function(){
									//																		if (checkFloat("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>profitMargin")) {
									//																			setProfitMarginDiscounts(<%= Integer.toString(section.getSectionId()) %>,<%= Integer.toString(product.getProductId()) %>);
									//																			calculateBudget(); 
									//																			sendForm("info", "SetDiscounts" ,"section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>discounts");
									//																		} };
                            	</script>
                            </td>
                            <% } %>
                            <td class="tablecontent<%= cd %>">
                            	<div class="rightlink">
                                    <img src="images/up.png" alt="Mover al anterior" class="<% if (firstP) { %>imgbuttonsmalldisabled<% } else { %>imgbuttonsmall<% } %>" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>sortup" />
                                    <img src="images/down.png" alt="Mover al posterior" class="<% if (lastP) { %>imgbuttonsmalldisabled<% } else { %>imgbuttonsmall<% } %>" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>sortdown" />
                                    <img src="images/bin.png" alt="Eliminar" class="imgbuttonsmall" id="section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>remove" />
                                </div>
                                <script type="text/javascript">
									// add listeners for sorting and removing attachments	
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>sortup").onclick = function(){ updateDiv('sections','Sort?sectionId=<%= Integer.toString(section.getSectionId()) %>&budgetId=<%= budgetIdS %>&productId=<%= Integer.toString(product.getProductId()) %>&sort=-1'); };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>sortdown").onclick = function(){ updateDiv('sections','Sort?sectionId=<%= Integer.toString(section.getSectionId()) %>&budgetId=<%= budgetIdS %>&productId=<%= Integer.toString(product.getProductId()) %>&sort=1'); };
									document.getElementById("section<%= Integer.toString(section.getSectionId()) %>product<%= Integer.toString(product.getProductId()) %>remove").onclick = function(){ updateDiv('sections','Remove?sectionId=<%= Integer.toString(section.getSectionId()) %>&budgetId=<%= budgetIdS %>&productId=<%= Integer.toString(product.getProductId()) %>'); };
								</script>
                            </td>
                        </tr>
                        <%
	                    				if (cd.equals("D"))
	                    		           	   cd = "C";
	                    		           else
	                    		               cd = "D";
                        				
                        				firstP = false;
                    				} // for products
                    			} // else products
                    			
                    			// section total
                    			if (budgetS.hasGlobalTotal()) { %>
                    				<tr> 
                    					<th>
                    					</th>
		                            	<th colspan="<%= Integer.toString(totalColspan - 1) %>" class="attributecontent">
			                                <div class="linktext" id="addProductSection<%= Integer.toString(section.getSectionId()) %>">
			                                    <a href="#windowAddProductSection<%= Integer.toString(section.getSectionId()) %>">Añadir artículo</a>
			                                </div>
			                                <!-- modal window outside the table -->
			                            </th>
		                                <th class="tabletitle">Subtotal
		                                </th>
		                                <th class="tabletitle" id="section<%= section.getSectionId() %>total">
		                                	<%= df.format(section.getCurrentTotal()) %>&euro;
		                                </th>
		                            </tr>
		                            <tr> 
		                                <th class="tablemargin">&nbsp;
		                                </th>
		                            </tr>
                    			<%	
                    			} else { %>
                    				<tr>
                    					<th>
                    					</th>
                    					<th colspan="<%= Integer.toString(totalColspan - 1) %>" class="attributecontent">
			                                <div class="linktext" id="addProductSection<%= Integer.toString(section.getSectionId()) %>">
			                                    <a href="#windowAddProductSection<%= Integer.toString(section.getSectionId()) %>">Añadir artículo</a>
			                                </div>
			                                <!-- modal window outside the table -->
			                            </th>
			                            <th class="tabletitle">Importe
			                            </th>
			                            <th class="tabletitle" id="section<%= section.getSectionId() %>total">
		                                	<%= df.format(section.getCurrentTotal()) %>&euro;
			                            </th>
			                        </tr>
			                        <tr>
			                        	<th colspan="<%= totalColspan %>">
			                            </th>
			                            <th class="tabletitle">Impuestos
			                            </th>
			                            <th class="tabletitle" id="section<%= section.getSectionId() %>taxes"><%= df.format(budgetS.getTaxRate()) %>%
			                            </th>
			                        </tr>
			                        <tr>
			                        	<th colspan="<%= totalColspan %>">
			                            </th>
			                            <th class="tabletitlebig">TOTAL
			                            </th>
			                            <th class="tabletitlebig" id="section<%= section.getSectionId() %>totalPlusTaxes">
		                                	<%= df.format(section.getCurrentTotal() * (1+budgetS.getTaxRate()/100)) %>&euro;
			                            </th>
			                        </tr>
			                        <tr> 
		                                <th class="tablemargin">&nbsp;
		                                </th>
		                            </tr>
                    			<%	
                    			}
                            
                    			firstS = false;
                    		} // for sections
                    	} // else sections
						%>
						<tr> 
                        	<th colspan="3" class="tablesuper">
                            	<div class="linktext" id="createSectionLink">
                                	Añadir capítulo
                                </div>
                            	<script type="text/javascript">
                                	document.getElementById("createSectionLink").onclick = function(){ updateDiv("sections", "AddSection?budgetId=<%= budgetIdS %>"); };
                            	</script>
                            </th>
                        </tr>
							<%
                    		// Budget Total
                    		
                    		if (sections != null && sections.length > 0 && budgetS.hasGlobalTotal()) { %>

	                        
	                        <tr>
	                        	<th colspan="<%= totalColspan %>">
	                            </th>
	                            <th class="tabletitle">Importe
	                            </th>
	                            <th class="tabletitle" id="budgetTotal">
	                                	<%= df.format(budgetS.getCurrentTotal()) %>&euro;
	                            </th>
	                        </tr>
	                        <tr>
	                        	<th colspan="<%= totalColspan %>">
	                            </th>
	                            <th class="tabletitle">Impuestos
	                            </th>
	                            <th class="tabletitle"  id="budgetTaxes"><%= df.format(budgetS.getTaxRate()) %>%
	                            </th>
	                        </tr>
	                        <tr>
	                        	<th colspan="<%= totalColspan %>">
	                            </th>
	                            <th class="tabletitlebig">TOTAL
	                            </th>
	                            <th class="tabletitlebig" id="budgetTotalPlusTaxes">
	                                	<%= df.format(budgetS.getCurrentTotalPlusTaxes()) %>&euro;
	                            </th>
	                        </tr>
                        
                    	<%
                    		} // hasGlobalTotal
                    %>

                    </table>
                    
                    <!-- modal window to add product to section -->
    
    				<%
    				if (sections != null)
    				for (Section section : sections) {
    				%>
                    <div id="windowAddProductSection<%= Integer.toString(section.getSectionId()) %>" class="modalWindow">
                        <div>
                            <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                         
                            <div class="linetitleA">
                                <div class="lefttext">
                                    <h2>Seleccione el producto</h2>
                                </div>
                            </div>
                       
                            <div id="productcontentsection<%= Integer.toString(section.getSectionId()) %>">
                        
                            </div>
                        </div>
                        
                        <script type="text/javascript">
                            document.getElementById("addProductSection<%= Integer.toString(section.getSectionId()) %>").onclick = function(){ updateDiv("productcontentsection<%= Integer.toString(section.getSectionId()) %>","productsearch.jsp?budgetId=<%= budgetIdS %>&sectionId=<%= Integer.toString(section.getSectionId()) %>"); };
                        </script>
                        
                    </div>
                    <%
                    }
                    %>
                    <!-- modal window to add product to section / -->
                  