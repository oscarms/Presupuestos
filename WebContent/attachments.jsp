<%@ page import="budget.User" %>
<%@ page import="budget.Attachment" %>
<%@ page import="database.LogDB" %>
<%@ page import="dao.LogDAO" %>
<%@ page import="dao.LogType" %>
<%@ page import="database.BudgetDB" %>
<%@ page import="dao.BudgetDAO" %>
<%
String budgetIdA = request.getParameter("budgetId");
User userA = (User)session.getAttribute("user");
LogDAO logDAOA = new LogDB();
BudgetDAO budgetDAOA = new BudgetDB();
logDAOA.add(LogType.ACTION, userA.getName() + " ha listado los anexos del presupuesto " + budgetIdA, System.currentTimeMillis());

Attachment[] attachments = budgetDAOA.getAttachments(budgetIdA);
if (attachments == null || attachments.length < 1) { %>
					<div class="linetext">
			            <div class="lefttext">
			                No hay anexos
			            </div>
			        </div>		
<%
} else {
	// imgbuttonsmalldisabled en first up, imgbuttonsmalldisabled en last down
	Attachment attachment;
	boolean first = true;
	boolean last = false;
	for (int i = 0; i < attachments.length; i++) {
		attachment = attachments[i];
		if (i == attachments.length-1)
			last = true;
		%>
					<div class="linetext" id="attachment<%= attachment.getId() %>">
                        <div class="lefttext">
                            <a href="Download?budgetId=<%= budgetIdA %>&amp;attachmentId=<%= attachment.getId() %>" target="_blank">
                            <%= attachment.getName() %></a>
                        </div>
                        <div class="rightlink">
                        	<img src="images/up.png" alt="Mover al anterior" 
                        		class="<% if (first) { %>imgbuttonsmalldisabled<% } else { %>imgbuttonsmall<% } %>" 
                        		id="sortupattachment<%= attachment.getId() %>" />
                            <img src="images/down.png" alt="Mover al posterior" 
                            	class="<% if (last) { %>imgbuttonsmalldisabled<% } else { %>imgbuttonsmall<% } %>" 
                            	id="sortdownattachment<%= attachment.getId() %>" />
                            <img src="images/bin.png" alt="Eliminar" 
                            	class="imgbuttonsmall" id="removeattachment<%= attachment.getId() %>" />
                        </div>
                    </div>
					<script type="text/javascript">
						document.getElementById("sortupattachment<%= attachment.getId() %>").onclick = 
							function(){ updateDiv('attachmentlist',
									'Sort?attachmentId=<%= attachment.getId() %>&budgetId=<%= budgetIdA %>&sort=-1'); };
									document.getElementById("sortdownattachment<%= attachment.getId() %>").onclick = 
							function(){ updateDiv('attachmentlist',
									'Sort?attachmentId=<%= attachment.getId() %>&budgetId=<%= budgetIdA %>&sort=1'); };
									document.getElementById("removeattachment<%= attachment.getId() %>").onclick = 
							function(){ if (confirm("Se va a eliminar el anexo")) { updateDiv('attachmentlist',
									'Remove?attachmentId=<%= attachment.getId() %>&budgetId=<%= budgetIdA %>'); } };
					</script>
		<%
		first = false;
	} // for
} // else
%>
