<%@ page isErrorPage="true" import="java.io.*" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>

<%
	LogDAO logDAO = new LogDB();
	StringWriter errors = new StringWriter();
	PrintWriter errorsPw = new PrintWriter(errors);
	try {
		logDAO.add(LogType.ERROR, 
				exception.getMessage() + ": " + errors.toString(), 
				System.currentTimeMillis());
		exception.printStackTrace(errorsPw); // TODO remove
	} catch (Exception e) {
		exception.printStackTrace(); // TODO remove
	} finally {
		errorsPw.close();
		errors.close();
	}
%>

<%@ include file="header.jsp" %>

	<div id="main">
                
            <div>
                <div class="linetitleA">
            		<div class="lefttext">
            			<h2>Se ha producido un error</h2>
                	</div>
            	</div>

                <div class="lefttext">
                    La página que estaba cargando ha producido un error<br />
                    <br  />
                    Vuelta a intentarlo o, si el error persiste, contacte con el administrador<br />
                    <br />
                </div>
                
            </div>
                        
    </div> <!-- main / -->
    
<%@include file="footer.jsp" %>
