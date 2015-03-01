<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="budget.Product" %>
<%@ page import="dao.ProductDAO" %>
<%@ page import="database.ProductDB" %>
<%@ page import="java.lang.NumberFormatException" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.LogDB" %>

<% 	User user = (User)session.getAttribute("user");

	String budgetId = request.getParameter("budgetId");
	if (budgetId == null)
		budgetId = "";
	
	Integer sectionId;
	try {
		sectionId = Integer.parseInt(request.getParameter("sectionId"));
	} catch (NumberFormatException e) {
		sectionId = null;
	} catch (NullPointerException e) {
		sectionId = null;
	}
	
	Integer productId;
	Float minPrice;
	Float maxPrice;
	try {
		minPrice = Float.parseFloat(request.getParameter("minPrice"));
	} catch (NumberFormatException e) {
		minPrice = Float.MIN_VALUE;
	} catch (NullPointerException e) {
		minPrice = Float.MIN_VALUE;
	}
	try {
		maxPrice = Float.parseFloat(request.getParameter("maxPrice"));
	} catch (NumberFormatException e) {
		maxPrice = Float.MAX_VALUE;
	} catch (NullPointerException e) {
		maxPrice = Float.MAX_VALUE;
	}

	String filter = request.getParameter("filter");
	if (filter == null)
		filter = "";
				
	Boolean discontinued = (request.getParameter("showDiscontinued") != null
			&& request.getParameter("showDiscontinued").equals("on"));
	
	ProductDAO productDAO = new ProductDB();
	Product[] products = productDAO.getProducts(minPrice, maxPrice, discontinued, filter);
	String cd;
	
	LogDAO logDAOLi = new LogDB();
	logDAOLi.add(LogType.ACTION, user.getName() + " ha listado productos", System.currentTimeMillis());
	String scripts = "";
%>                           
            <table>
                <tr>
                    <th colspan="5" class="tablesuper">
                        Artículos
                    </th>
                </tr>
                
                <tr>
                    <th>
                    </th>
                    <th class="tabletitle">Código
                    </th>
                    <th class="tabletitle">Nombre
                    </th>
                    <th class="tabletitle">Tarifa
                    </th>
                    <th class="tabletitle">
                    </th>
                </tr>
           	<%
			if (products == null || products.length<1) { %>
                <tr>
           		    <td>
                    </td>
                    <td class="tablecontentD" colspan="4">No hay artículos
                    </td>
                </tr>
           	<%
           	} else {
        		cd = "D";
        		DecimalFormat df = new DecimalFormat();
        		df.setGroupingUsed(false);
            	df.setMaximumFractionDigits(2);
            	df.setMinimumFractionDigits(2);
                for (Product product : products) { %>                   
              
                <tr>
                    <td>
                    </td>
                    <% if ( (budgetId == null || budgetId.length()<1) || sectionId == null) { %>
                    <td class="tablemaincontent<%= cd %>"><a href="product.jsp?productId=<%= Integer.toString(product.getProductId()) %>"><%= Integer.toString(product.getProductId()) %></a>
                    <% } else { %>
                    <td class="tablemaincontent<%= cd %>"><%= Integer.toString(product.getProductId()) %>
                    <% } %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= product.getName() %>
                    </td>
                    <td class="tablecontent<%= cd %>"><%= ( product.isCurrentlyDiscontinued() ? "Descatalogado" : df.format(product.getCurrentPrice()) + "&euro;") %>
                    </td>
                    <% if ( (budgetId == null || budgetId.length()<1) || sectionId == null) { %>
                    <td class="tablemaincontent<%= cd %>" style="width:85px"><div class="linktext"><a href="product.jsp?productId=<%= Integer.toString(product.getProductId()) %>">Mostrar</a></div>
                    <% } else { %>
                    <td class="tablemaincontent<%= cd %>" style="width:85px"><div class="linkpointer" id="actionProduct<%= Integer.toString(product.getProductId()) %>section<%= Integer.toString(sectionId) %>">Añadir</div>
                    <% } %>
                    </td>
                </tr>
                <% if ( !(budgetId == null || budgetId.length()<1) && sectionId != null) {
                scripts += ""
                + "	document.getElementById('actionProduct" + Integer.toString(product.getProductId()) + "section" + Integer.toString(sectionId) + "').onclick =  "
                + "		function(){  "
                + "		updateDiv('sections', "
                + "				'AddProduct?productId=" + Integer.toString(product.getProductId()) + "&budgetId=" + budgetId + "&sectionId=" + Integer.toString(sectionId) + "' "
                + "				); "
                + "		}; ";
                } // if script
                
           	if (cd.equals("D"))
           	   	cd = "C";
           	else
               	cd = "D";
       	   		} // for
    	   	} // else
    	   	if (products != null && products.length >= ProductDAO.MAXRESULTS) { %>
            	<tr>
                    <th>
                    </th>
                    <th colspan="4" class="tabletitle">
                        Hay más resultados, utilice los filtros para reducir la lista
                    </th>
                </tr>
         <% } %>
                
                <tr> 
                    <th class="tablemargin">&nbsp;
                    </th>
                </tr>
                
            </table>
            
        <% if ( !(budgetId == null || budgetId.length()<1) && sectionId != null) { %>
        <script type="text/javascript">
        	<%= scripts %> 
        </script>
        <% } %>
        
        