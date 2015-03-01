<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%@ page import="storage.FileLocalStorage" %>
<%@ page import="dao.FileDAO" %>
<%@ page import="budget.Cover" %>

<%@ include file="header.jsp" %>

<%
LogDAO logDAO = new LogDB();
FileDAO fileDAO = new FileLocalStorage();
BudgetDAO budgetDAO = new BudgetDB();
%>
	<div id="main">
                
            <div>
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Mantenimiento</h2>
                	</div>
            	</div>

                <div class="lefttext">
                    Se han eliminado <%= logDAO.purge() %> líneas del log<br />
                    <br  />
                    <% if (fileDAO.clearTemp()) { %>Se<% } else { %>No se<% } %> ha vaciado la carpeta temporal<br />
                    <br />
                </div>
                
            </div>
            
            <div>
            	<div class="linetitleA">
            		<div class="lefttext">
            			<h2></h2>
                	</div>
            	</div>
            </div>
            
            <div>
            	<div class="linetitleA">
            		<div class="lefttext">
            			<h2>Eliminar portada</h2>
                	</div>
            	</div>
            	
            	<div class="lefttext">
				<%
				Cover[] covers = budgetDAO.getCovers();
				if (covers != null && covers.length > 0) {
				
					for(Cover cover : covers) {
						// if cover.id == budgetC.getCoverId() selectedcover %>
									<img src="Download?coverImage=<%= cover.getId() %>" alt="Eliminar portada" 
										class="imgcover" 
										id="cover<%= cover.getId() %>" />
									<script type="text/javascript">
										document.getElementById("cover<%= cover.getId() %>").onclick = 
											function(){ 
												if (confirm("Se va a eliminar la portada")) {
													window.location = "./RemoveCover?coverId=" + <%= cover.getId() %>;
												}
											};
									</script>
						<%
					} // end for
				} // end if
				%>
				</div>
            </div>        
    </div> <!-- main / -->
    
<%@include file="footer.jsp" %>
