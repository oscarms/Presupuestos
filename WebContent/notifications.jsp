<%@ include file="header.jsp" %>
<%@ page import="budget.User" %>
<%@ page import="budget.UserType" %>

    <div id="main">
        
        <div class="bluebox" id="notificationlist">
        	<% if (((User)session.getAttribute("user")).getUserType() == UserType.SALESPERSON) { %>
        		<%@ include file="notificationlist.jsp" %>
        	<% } else { %>
        		Error al obtener las notificaciones del usuario
        	<% }%>
        </div> <!-- bluebox -->
              
    </div> <!-- main / -->
    
<%@ include file="footer.jsp" %>
