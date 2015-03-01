package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Permission;
import budget.User;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;

/**
 * Servlet implementation class SetSalesperson
 * 
 * Set a salesperson to a client
 */
@WebServlet("/SetSalesperson")
@MultipartConfig
public class SetSalesperson extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetSalesperson() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters and user
		Integer clientId;
		Integer salespersonId;
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		UserDAO userDAO = new UserDB();
		int message;
		try {
			clientId = Integer.parseInt(request.getParameter("clientId"));
			salespersonId = Integer.parseInt(request.getParameter("salespersonId"));
		} catch (NumberFormatException e) {
			clientId = null;
			salespersonId = null;
		}

		// must have ALLCLIENTS permission
		if (clientId != null && salespersonId != null && user.hasPermission(Permission.ALLCLIENTS)) {
			
			if (userDAO.setSalesperson(clientId, salespersonId)) {
				// client updated with new salesperson
				message = 4;
				logDAO.add(LogType.ACTION, user.getName() + " ha modificado el comercial de un cliente", 
						System.currentTimeMillis());
			} else {
				// error, maybe client or salesperson no exists
				message = 5;
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar el comercial de un cliente, pero ha habido un fallo", 
						System.currentTimeMillis());
			}

		} else {
			// error in parameters or permissions
			message = 5;
			logDAO.add(LogType.WARNING, user.getName() + 
					" ha intentado modificar el comercial de un cliente, pero no tiene permisos o los parámetros eran erróneos", 
					System.currentTimeMillis());
		}
		
		// return client.jsp with message	
		response.sendRedirect("client.jsp?clientId=" + clientId + "&message=" + message);	
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
