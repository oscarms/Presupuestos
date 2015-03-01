package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Client;
import budget.Permission;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.BudgetDB;
import database.LogDB;
import database.UserDB;

/**
 * Servlet implementation class SetClient
 * 
 * Updates the client of a budget
 */
@WebServlet("/SetClient")
@MultipartConfig
public class SetClient extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetClient() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters and user
		Integer clientId;
		String budgetId = request.getParameter("budgetId");
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		UserDAO userDAO = new UserDB();
		int message = 0;
		try {
			clientId = Integer.parseInt(request.getParameter("clientId"));
		} catch (NumberFormatException e) {
			clientId = null;
		}
		
		// filter EditBudget ensures that the user can edit the budget
		// check if the client exists, and the user is the salesperson or has ALLCLIENTS permission
		Client client = userDAO.getClient(clientId);
		if (clientId != null && client != null &&
				( user.hasPermission(Permission.ALLCLIENTS) || client.getSalesperson().equals(user) ) ) {
			// update the budget
			if (budgetDAO.setClient(budgetId, clientId)) {
				// budget updated
				logDAO.add(LogType.ACTION, user.getName() + " ha modificado el cliente del presupuesto " + budgetId, 
						System.currentTimeMillis());
				
			} else {
				message = 3;
				request.setAttribute("message", message);
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar el cliente del presupuesto " + budgetId + 
						", pero ha fallado", System.currentTimeMillis());
			}

		} else {
			// error in parameters
			message = 4;
			request.setAttribute("message", message);
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado modificar el cliente del presupuesto " + budgetId + 
					", pero los parámetros eran erróneos", System.currentTimeMillis());
			
		}
				
		// return createbudget.jsp with message	
		response.sendRedirect("createbudget.jsp?budgetId=" + budgetId + "&message=" + message);
				
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
