<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.Product" %>
<%@ page import="budget.Price" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.ProductDB" %>
<%@ page import="dao.ProductDAO" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ include file="header.jsp" %>
<%
//Create (isNew == 1 && productId == null) Edit (productId != null) Error (productId == null && !isNew))
Integer productId;
try {
	productId = Integer.parseInt(request.getParameter("productId"));
} catch (NumberFormatException e) {
	productId = null;
} catch (NullPointerException e) {
	productId = null;
}
Boolean isNew = (request.getParameter("isNew") != null
&& request.getParameter("isNew").equals("1"));

User user = (User)session.getAttribute("user");
boolean isAdministrator = user.hasPermission(Permission.ADMINISTRATE);

LogDAO logDAO = new LogDB();
ProductDAO productDAO = new ProductDB();
DecimalFormat df = new DecimalFormat();
df.setGroupingUsed(false);
df.setMaximumFractionDigits(2);
df.setMinimumFractionDigits(2);

if ( productId == null && !isNew ) {
	// ERROR, log and logout user
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a product.jsp sin argumentos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}

if ( isNew && !isAdministrator ) {
	// ERROR, log and logout user
	logDAO.add(LogType.WARNING, user.getName() + " ha intentado crear un artículo sin permisos", System.currentTimeMillis());
	response.sendRedirect("Logout");
	return;
}

// productId: if isNew generate a new productId using a database query
// only administrators are able to edit the product
Product product = null;
String name = "";
String description = "";
String currentPrice = "";
String costPrice = ""; // only administrator
String profitMargin = "Se calculará a partir del precio de coste y tarifa: (Tarifa - Precio de coste) / Tarifa"; // only administrator

if ( productId != null ) {
	// get productId from database and show data
	product = productDAO.getProduct(productId);
	if (product == null) {
		// ERROR, log and logout user
		logDAO.add(LogType.WARNING, user.getName() + 
				" ha intentado acceder a product.jsp con productId no existente", System.currentTimeMillis());
		response.sendRedirect("Logout");
		return;
	}

	name = product.getName();
	if (name == null)
		name = "";
	
	description = product.getDescription();
	if (description == null)
		description = "";
	
	// current price description
	if (product.isCurrentlyDiscontinued()) {
		currentPrice = "El artículo está descatalogado";
	} else {
		currentPrice = df.format(product.getCurrentPrice()) + "&euro;";
	}
	
	// cost price and profitMargin only if administrator
	if (isAdministrator) {
		costPrice = df.format(product.getCostPrice()).replace(",",".");
		profitMargin = df.format(product.getCurrentProfitMargin()) + "%";
	}

} else { // isNew
	// retrieve a free productId from database
	productId = productDAO.newProductId();
}

// retrieve message
Integer message = (request.getParameter("message") != null ? Integer.parseInt(request.getParameter("message")) : 0);
if (message == null)
	message = 0;


logDAO.add(LogType.ACTION, user.getName() + " ha visualizado el producto " + productId, System.currentTimeMillis());

%>
<%
if (message == 1) {
%>
	<script type="text/javascript">
		setMessage("Información del artículo actualizada",false);
    </script>
<%
} else if (message == 2) {
%>
	<script type="text/javascript">
		setMessage("Artículo creado",false);
    </script>
<%
} else if (message == 3) {
%>
	<script type="text/javascript">
		setMessage("El código de artículo ya existe",true);
    </script>
<%
} else if (message == 6) {
	%>
	<script type="text/javascript">
		setMessage("Se ha producido un error",true);
    </script>
	<%
} else if (message == 4) {
%>
	<script type="text/javascript">
		setMessage("Tarifa añadida",false);
    </script>
<%
} else if (message == 5) {
%>
	<script type="text/javascript">
		setMessage("Producto descatalogado",false);
    </script>
<%
} else if (message == 7) {
%>
	<script type="text/javascript">
		setMessage("Se ha producido un error actualizando la tarifa",true);
    </script>
	<%
	}
%>

    <div id="main">
    	
        <div class="bluebox">
            
            <form id="productData" action="SaveProduct" method="post" enctype="multipart/form-data">
             <% if (!isNew) { %>
            	<input type="hidden" name="productId" id="productId" value="<%= productId %>" />
            	<input type="hidden" name="isNew" value="0" />
             <% } %>
             	<input type="hidden" name="isNew" value="1" />
                <table>
                	<tr>
                        <th colspan="4" class="tablesuper">
                            Detalles del artículo
                        </th>
                        <% if (!isNew) { %>
	                        <td style="width:300px" class="superattribute">
	                        	<div class="lefttext">
	                        		Imagen del artículo
	                            </div>
	                            <% if (isAdministrator) { %>
		                            <div class="rightlink">
		                            	<a href="#windowUploadImage">Cambiar</a>
		                            </div>
	                            <% } %>
	                        </td>
                        <% } %>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:140px" class="attributetitle">Código de artículo
                        </td>
                        <% if (!isNew) { %>
                        	<td style="width:auto" class="attributecontent"><%= productId %>
                        <% } else { %>
                        	<td style="width:auto"><input type="text" id="productId" name="productId"
                        		class="textbox" value="<%= productId %>" />
                        <% } %>
                        </td>
                        <td class="tablemargin">
                        </td>
                        <% if (!isNew) { %>
	                        <td style="width:300px" rowspan="<% if(isAdministrator) { %>8<% } else { %>5<% } %>">
	                        	<img src="Download?productImage=<%= productId %>" alt="Imagen del producto" 
	                        		class="image" id="productimage" />
	                        </td>
                        <% } %>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:140px" class="attributetitle">Nombre
                        </td>
                        <% if (isAdministrator) { %>
                        	<td style="width:auto"><input type="text" name="name" id="name" class="textbox" value="<%= name %>" />
                       	<% } else { %>
                        	<td style="width:auto" class="attributecontent"><%= name %>
                        <% } %>
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:140px" class="attributetitle">Descripción
                        </td>
                        <% if (isAdministrator) { %>
                        	<td style="width:auto"><textarea name="description" id="description" class="bigtextbox"><%= description %></textarea>
                        <% } else { %>
                        	<td style="width:auto" class="attributecontent"><%= description %>
                        <% } %>
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <tr>
                    	<td>&nbsp;
                        </td>
                    </tr>
                    <tr>
                        <td class="tablemargin">
                        </td>
                        <td style="width:140px" class="attributetitle">Tarifa actual
                        </td>
                        <% if (isNew) { %>
                        	<td style="width:auto" class="attributecontent">Añadir después de crear el artículo
                        <% } else { %>
                        	<td style="width:auto" class="attributecontent"><%= currentPrice %>
                        <% } %>
                        </td>
                        <td class="tablemargin">
                        </td>
                    </tr>
                    <% if (isAdministrator) { %>
	                    <tr>
	                        <td class="tablemargin">
	                        </td>
	                        <td style="width:140px" class="attributetitle">Precio de coste
	                        </td>
	                        <td style="width:auto"><input type="text" name="costPrice" id="costPrice" class="textbox" value="<%= costPrice %>" />
	                        </td>
	                        <td class="tablemargin">
	                        </td>
	                    </tr>
	                    <tr>
	                        <td class="tablemargin">
	                        </td>
	                        <td style="width:140px" class="attributetitle">Beneficio
	                        </td>
	                        <td style="width:auto" class="attributecontent"><%= profitMargin %>
	                        </td>
	                        <td class="tablemargin">
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
	                                	<% if (isNew) { %>
	                                		Crear&nbsp;artículo
	                                	<% } else { %>
	                                    	Guardar&nbsp;cambios
	                                    <% } %>
	                                </div>
	                            </div>
	                        </td>
	                    </tr>

                    <% } %>
                </table>
            </form>
            
            <script type="text/javascript">
				<% if (isNew) { %>
				document.getElementById("productId").onkeyup = function(){ checkProductForm(); };
				<% } %>
				<% if (isAdministrator) { %>
				document.getElementById("name").onkeyup = function(){ checkProductForm(); };
				document.getElementById("costPrice").onkeyup = function(){ checkProductForm(); };
                document.getElementById("submittext").onclick = function(){ if (checkProductForm()) document.getElementById("productData").submit(); };
                <% } %>
            </script>
                
            <% if (!isNew) { 
            	String cd; %>
                
			<div class="linetitleC">
                <div class="lefttext">
                    <h2>Historial de tarifas</h2>
                </div>
            </div>
        
        	<table>
                <tr>
                    <th class="tablemargin">
                    </th>
                    <th style="width:15%" class="tabletitle">Desde
                    </th>
                    <th style="width:15%" class="tabletitle">Tarifa
                    </th>
                    <th class="tablemargin">
                    </th>
                    <th style="width:15%" class="tabletitle">Desde
                    </th>
                    <th style="width:15%" class="tabletitle">Tarifa
                    </th>
                    <th class="tablemargin">
                    </th>
                    <th style="width:15%" class="tabletitle">Desde
                    </th>
                    <th style="width:15%" class="tabletitle">Tarifa
                    </th>
                </tr>
            <% if (product.getPrices() == null || product.getPrices().length < 1) { %>
            	<tr>
                    <td class="tablemargin">
                    </td>
                    <td class="tablecontentD" colspan="2">No hay precios
                    </td>
                    <td class="tablemargin">
                    </td>
                    <td class="tablecontentD">
                    </td>
                    <td class="tablecontentD">
                    </td>
                    <td class="tablemargin">
                    </td>
                    <td class="tablecontentD">
                    </td>
                    <td class="tablecontentD">
                    </td>
                </tr>
                
           	<% } else {
        		cd = "D";
        		Price[] prices = product.getPrices();
        		int rows = (prices.length + 2) / 3;
        		for (int i = 0; i < rows; i++) { %>
                
                <tr>
                    <td class="tablemargin">
                    </td>
                    <td class="tablecontent<%= cd %>">
	                    <% if (i < prices.length) { %>
	                    	<%= prices[i].getDateString() %>
	                    <% } %>
                    </td>
                    <td class="tablecontent<%= cd %>">
                    	<% if (i < prices.length) { %>
	                    	<%= prices[i].isDiscontinued() ? "Descatalogado" : df.format(prices[i].getPrice()) + "&euro;" %>
	                    <% } %>
                    </td>
                    <td class="tablemargin">
                    </td>
                    <td class="tablecontent<%= cd %>">
	                    <% if (rows+i < prices.length) { %>
	                    	<%= prices[rows+i].getDateString() %>
	                    <% } %>
                    </td>
                    <td class="tablecontent<%= cd %>">
                    	<% if (rows+i < prices.length) { %>
	                    	<%= prices[rows+i].isDiscontinued() ? "Descatalogado" : df.format(prices[rows+i].getPrice()) + "&euro;" %>
	                    <% } %>
                    </td>
                    <td class="tablemargin">
                    </td>
                    <td class="tablecontent<%= cd %>">
	                    <% if (2*rows+i < prices.length) { %>
	                    	<%= prices[2*rows+i].getDateString() %>
	                    <% } %>
                    </td>
                    <td class="tablecontent<%= cd %>">
                    	<% if (2*rows+i < prices.length) { %>
	                    	<%= prices[2*rows+i].isDiscontinued() ? "Descatalogado" : df.format(prices[2*rows+i].getPrice()) + "&euro;" %>
	                    <% } %>
                    </td>
                </tr>
              	<%  
	           	if (cd.equals("D"))
	           	   	cd = "C";
	           	else
	               	cd = "D";
	       	   		} // for
	    	   	} // else %>
	    	   	
                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
            </table>
            
			<% if (isAdministrator) { 
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    		dateFormat.setLenient(false);
	    		Date date = new Date( System.currentTimeMillis() + 86400000 );
				String from = dateFormat.format(date); // from tomorrow
			%>
	        	<form id="priceData" action="UpdatePrice" method="post" enctype="multipart/form-data">
	            	<input type="hidden" name="productId" value="<%= productId %>" />
	                <table>
	                	<tr>
	                        <th colspan="9" class="tablesuper">
	                            Añadir tarifa
	                        </th>
	                    </tr>
	                    <tr>
	                        <td class="tablemargin">
	                        </td>
	                        <td style="width:95px" class="attributetitle">Nueva tarifa
	                        </td>
	                        <td style="width:auto"><input type="text" name="newPrice" id="newPrice" class="textbox" value="" />
	                        </td>
	                        <td class="tablemargin">
	                        </td>
							<td style="width:95px" class="attributetitle">Descatalogado
	                        </td>
	                        <td style="width:20px"><input type="checkbox" name="discontinued" id="discontinued" class="checkbox" />
	                        </td>
	                        <td class="tablemargin">
	                        </td>
	                        <td style="width:130px" class="attributetitle">Fecha aplicación
	                        </td>
	                        <td style="width:auto"><input type="text" name="date" id="date" class="textbox" value="<%= from %>" />
	                        </td>
	                    </tr>
	                    <tr>
	                        <td colspan="9" class="attributetitle">
	                            <div class="rightlink">
	                                <div id="submitprice" class="linkpointer">
	                                </div>
	                            </div>
	                        </td>
	                    </tr>
	                </table>
	            </form>
	            
	            <script type="text/javascript">
					document.getElementById("newPrice").onkeyup = function(){ formatPriceFormA(); };
					document.getElementById("discontinued").onchange = function(){ formatPriceFormB(); };
					document.getElementById("date").onkeyup = function(){ formatPriceFormC(); };
	                document.getElementById("submitprice").onclick = function(){ 
	                	var question;
	                	if (document.getElementById("discontinued").checked) {
	                		question = 'Se va descatalogar el producto a partir de la fecha ' 
	        					+ document.getElementById("date").value 
	        					+ '. Estos datos no se podrán modificar. ¿Seguro que son correctos?';
	                	} else {
	                		question = 'Se va a añadir la tarifa ' 
        						+ getNumber(document.getElementById("newPrice").value)
        						+ ' que se hará efectiva a partir del ' 
        						+ document.getElementById("date").value 
        						+ '. Estos datos no se podrán modificar. ¿Seguro que son correctos?';
	                	}
	                	if ( checkPriceForm() && confirm(question) ) 	
	                			document.getElementById("priceData").submit(); };
	            </script>
        
        	<% } // Show form if isAdministrator %>
        <% } // Show price list if !isNew %>

        </div> <!-- bluebox / -->
        
        <% if (isAdministrator && !isNew) { %>
        <!-- modal window to upload image -->
        
	        <div id="windowUploadImage" class="modalWindow">
	            <div>
	                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
	                
	                <div class="linetitleA">
	            		<div class="lefttext">
	            			<h2>Cambiar imagen del producto</h2>
	                	</div>
	            	</div>
	           
	                <div class="lefttext">
	                    Seleccione el fichero de imagen para establecer como imagen del producto.
	                </div>
	                <form id="uploadImage" action="UploadProductImage" method="post" enctype="multipart/form-data">
	                	<input type="hidden" name="productId" value="<%= productId %>" />
	                    <input type="file" name="imageToUpload" id="imageToUpload" onchange="fileSelected('productImage');" 
	                    	class="fileSelector" />
	                    <input type="button" onclick="uploadFile('productImage')" value="Cargar imagen" 
	                    	class="fileSelectorBtn" id="imageToUploadBtn" />
	                </form>
	                <div class="progress"><p>&nbsp;</p></div>
	            </div>
	        </div>
    	
        <!-- modal window to upload image / -->
        <% } %>

    </div> <!-- main / -->

<%@ include file="footer.jsp" %>