<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="java.lang.NumberFormatException" %>
<% 	User userS = (User)session.getAttribute("user");

	String budgetIdS = request.getParameter("budgetId");
	if (budgetIdS == null)
		budgetIdS = "";
	
	Integer sectionIdS;
	try {
		sectionIdS = Integer.parseInt(request.getParameter("sectionId"));
	} catch (NumberFormatException e) {
		sectionIdS = null;
	} catch (NullPointerException e) {
		sectionIdS = null;
	}
	
	Integer productIdS;
	Float minPriceS;
	Float maxPriceS;
	try {
		minPriceS = Float.parseFloat(request.getParameter("minPrice"));
	} catch (NumberFormatException e) {
		minPriceS = null;
	} catch (NullPointerException e) {
		minPriceS = null;
	}
	try {
		maxPriceS = Float.parseFloat(request.getParameter("maxPrice"));
	} catch (NumberFormatException e) {
		maxPriceS = null;
	} catch (NullPointerException e) {
		maxPriceS = null;
	}

	String filterS = request.getParameter("filter");
	if (filterS == null)
		filterS = "";
				
	Boolean discontinuedS = (request.getParameter("showDiscontinued") != null
			&& request.getParameter("showDiscontinued").equals("on"));	
%>

        <form id="filterproduct" action="productsearch.jsp" method="post" enctype="multipart/form-data">
		<input type="hidden" name="budgetId" value="<%= budgetIdS %>" />
		<input type="hidden" name="sectionId" value="<%= sectionIdS %>" />
		
			<table>
                <tr>
                	<td style="width:60px" class="attributetitle">Filtrar
                    </td>
                    <td style="width:auto"><input type="text" name="filter" id="filterp" class="textbox" value="<%= filterS %>" />
                    </td>

            </table>
			<table>
                <tr>
                    <td style="width:125px" class="attributetitle">Precio mínimo
                    </td>
                    <td style="width:auto"><input type="text" name="minPrice" id="minPrice" class="textbox" 
                    <% if (minPriceS != null) { %>
                    	value="<%= Float.toString(minPriceS) %>" 
                    <% } %>
                    />
                    </td>
                    <td style="width:125px" class="attributetitle">Precio máximo
                    </td>
                    <td style="width:auto"><input type="text" name="maxPrice" id="maxPrice" class="textbox" 
                    <% if (maxPriceS != null) { %>
                    	value="<%= Float.toString(maxPriceS) %>" 
                    <% } %>
                    />
                    </td>
                    <td style="width:200px" class="attributetitle">Mostrar&nbsp;descatalogados
                    </td>
                    <td style="width:20px"><input type="checkbox" name="showDiscontinued" id="showDiscontinued" class="checkbox" 
                    <% if (discontinuedS) { %>checked="checked"<% } %> 
                    />
                    </td>
                 <% if (userS.hasPermission(Permission.ADMINISTRATE) && budgetIdS.length() < 1) { %>
                    <td style="width:220px" class="attributetitle">
                    	<div class="rightlink">
                    		<a href="#windowUploadCSV">Cargar&nbsp;CSV</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="product.jsp?isNew=1">Crear&nbsp;artículo</a>
                        </div>
                    </td>
                 <% } %>
                </tr>
            </table>
            
        </form>
		
        <script type="text/javascript">
			document.getElementById("minPrice").onkeyup = function(){ filterProducts(); };
			document.getElementById("minPrice").onchange = function(){ filterProducts(); };
			document.getElementById("maxPrice").onkeyup = function(){ filterProducts(); };
			document.getElementById("maxPrice").onchange = function(){ filterProducts(); };
			document.getElementById("filterp").onkeyup = function(){ filterProducts(); };
			document.getElementById("filterp").onchange = function(){ filterProducts(); };
			document.getElementById("showDiscontinued").onchange = function(){ filterProducts(); };
		</script>
                
        <div class="bluebox" id="productlist">
        
        	<%@ include file="productlist.jsp" %>
        
        </div> <!-- bluebox -->
        
        <% if (userS.hasPermission(Permission.ADMINISTRATE) && (budgetIdS == null || budgetIdS.length() < 1)) { %>
        <!-- modal window to upload CSV -->
        
        <div id="windowUploadCSV" class="modalWindow">
            <div>
                <a href="#closeModalWindow" title="Cerrar ventana" class="closeModalWindow"></a>
                
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Cargar CSV</h2>
                	</div>
            	</div>
           
                <div class="lefttext">
                    Seleccione el fichero CSV con el que actualizar los productos
                </div>
                <form id="uploadCSV" action="uploadCSV" method="post" enctype="multipart/form-data">
                    <input type="file" name="csvToUpload" id="csvToUpload" onchange="fileSelected('csv');" class="fileSelector" />
                    <input type="button" onclick="uploadFile('csv')" value="Cargar CSV" class="fileSelectorBtn" id="csvToUploadBtn" />
                </form>
                <div class="progress"><p>&nbsp;</p></div>
            </div>
        </div>
    	
        <!-- modal window to upload CSV / -->
        <% } %>
