		<form id="filterclient" action="clientsearch.jsp" method="post" enctype="multipart/form-data">
			<% 	String budgetIdS = request.getParameter("budgetId");
				if (budgetIdS == null)
					budgetIdS = "";
				
				String filterS = request.getParameter("filter");
				if (filterS == null)
					filterS = "";
				
				Boolean inactiveS = (request.getParameter("showInactive") != null
						&& request.getParameter("showInactive").equals("on"));				
			%>
			<input type="hidden" name="budgetId" value="<%= budgetIdS %>" />
            <table>
                <tr>
                    <td style="width:40px" class="attributetitle">Filtrar
                    </td>
                    <td style="width:auto"><input type="text" name="filter" id="filterc" class="textbox" value="<%= filterS %>"/>
                    </td>
                    <td style="width:135px" class="attributetitle">Mostrar&nbsp;inactivos
                    </td>
                    <td style="width:26px"><input type="checkbox" name="showInactive" id="showInactive" class="checkbox" 
                    <% if (inactiveS) { %>checked="checked"<% } %> />
                    </td>
                    <% if (budgetIdS.length() < 1) { %>
                    <td style="width:100px" class="attributetitle">
                    	<div class="rightlink">
                    		<a href="client.jsp?isNew=1">Crear&nbsp;cliente</a>
                        </div>
                    </td>
                    <% } %>
                </tr>
            </table>
        </form>
		
        <script type="text/javascript">
			document.getElementById("filterc").onkeyup = function(){ filterClients(); };
			document.getElementById("filterc").onchange = function(){ filterClients(); };
			document.getElementById("showInactive").onchange = function(){ filterClients(); };
		</script>
                
        <div class="bluebox" id="clientlist">
        
        	<%@ include file="clientlist.jsp" %>
        
        </div> <!-- bluebox -->