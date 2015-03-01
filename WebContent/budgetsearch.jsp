<%@ page import="budget.User" %>
<%@ page import="budget.Permission" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.util.Date" %>

	    	<% 	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    		dateFormat.setLenient(false);
	    		Date date;
	    		String fromS = request.getParameter("from");
	    		try {
	    			date = dateFormat.parse(fromS);
	    		} catch (ParseException e) {
	    			date = new Date(System.currentTimeMillis() - 2592000000L );
	    		} catch (NullPointerException e) {
	    			date = new Date(System.currentTimeMillis() - 2592000000L );
	    		}
	    		fromS = dateFormat.format(date);
				
				String toS = request.getParameter("to");
	    		try {
	    			date = dateFormat.parse(toS);
	    		} catch (ParseException e) {
	    			date = new Date( System.currentTimeMillis() );
	    		} catch (NullPointerException e) {
	    			date = new Date( System.currentTimeMillis() );
	    		}
	    		toS = dateFormat.format(date);
				
				String filterS = request.getParameter("filter");
				if (filterS == null)
					filterS = "";
				
				Boolean expiredS = (request.getParameter("showExpired") != null
						&& request.getParameter("showExpired").equals("on"));
				
				User userS = (User)session.getAttribute("user");
			%>
			    
        <form id="filterbudget" action="budgetsearch.jsp" method="post" enctype="multipart/form-data">
            <table>
                <tr>
                    <td style="width:50px" class="attributetitle">Desde
                    </td>
                    <td style="width:80px"><input type="text" name="from" id="from" class="textbox" value="<%= fromS %>" />
                    </td>
                    <td style="width:54px" class="attributetitle">Hasta
                    </td>
                    <td style="width:80px"><input type="text" name="to" id="to" class="textbox" value="<%= toS %>" />
                    </td>
                    <td style="width:56px" class="attributetitle">Filtrar
                    </td>
                    <td style="width:auto"><input type="text" name="filter" id="filter" class="textbox" value="<%= filterS %>" />
                    </td>
                    <td style="width:140px" class="attributetitle">Mostrar&nbsp;vencidos
                    </td>
                    <td style="width:20px"><input type="checkbox" name="showExpired" id="showExpired" class="checkbox" 
                    <% if (expiredS) { %>checked="checked"<% } %> />
                    </td>
                    <td style="width:
                    	<% if (userS.hasPermission(Permission.CREATEOFFERS)) { %>
                    		260px<% } else { %>130px<% } %>" class="attributetitle">
                    	<div class="rightlink">
                    		<a href="NewBudget">Crear&nbsp;presupuesto</a>
                    		<% if (userS.hasPermission(Permission.CREATEOFFERS)) { %>
                    		&nbsp;&nbsp;&nbsp;&nbsp;<a href="NewBudget?isOffer=1">Crear&nbsp;oferta</a><% } %>
                        </div>
                    </td>
                </tr>
            </table>
        </form>
		
        <script type="text/javascript">
			document.getElementById("from").onkeyup = function(){ filterBudgets(); };
			document.getElementById("from").onchange = function(){ filterBudgets(); };
			document.getElementById("to").onkeyup = function(){ filterBudgets(); };
			document.getElementById("to").onchange = function(){ filterBudgets(); };
			document.getElementById("filter").onkeyup = function(){ filterBudgets(); };
			document.getElementById("to").onchange = function(){ filterBudgets(); };
			document.getElementById("showExpired").onchange = function(){ filterBudgets(); };
		</script>
                
        <div class="bluebox" id="budgetlist">
        
        	<%@ include file="budgetlist.jsp" %>
        
        </div> <!-- bluebox -->
              