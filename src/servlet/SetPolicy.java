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
 * Servlet implementation class SetPolicy
 * 
 * Modifies the user permissions
 */
@WebServlet("/SetPolicy")
@MultipartConfig
public class SetPolicy extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetPolicy() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters and user
		Integer userId;
		boolean enable;
		boolean administrator;
		boolean viewOffers;
		boolean createOffers;
		boolean allClients;
		User user = (User)request.getSession(true).getAttribute("user");
				
		try {
			userId = Integer.parseInt(request.getParameter("salespersonId"));
			enable = request.getParameter("enable") != null && request.getParameter("enable").equals("on");
			administrator = request.getParameter("administrator") != null && request.getParameter("administrator").equals("on");
			viewOffers = request.getParameter("viewOffers") != null && request.getParameter("viewOffers").equals("on");
			createOffers = request.getParameter("createOffers") != null && request.getParameter("createOffers").equals("on");
			allClients = request.getParameter("allClients") != null && request.getParameter("allClients").equals("on");
						
		} catch (NumberFormatException e) {
			userId = null;
			enable = false;
			administrator = false;
			viewOffers = false;
			createOffers = false;
			allClients = false;
		} catch (NullPointerException e) {
			userId = null;
			enable = false;
			administrator = false;
			viewOffers = false;
			createOffers = false;
			allClients = false;
		}
		
		LogDAO logDAO = new LogDB();
		int message;
		
		// Administration permission already checked by filter Administration. Administrator cannot edit himself
		// Check policy logic: If is administrator, has all permissions
		if (userId != null && userId != user.getId() && 
				(!administrator || (administrator && viewOffers && createOffers && allClients) ) ) {			

			// count permissions
			int count = 0;
			if (administrator)
				count++;
			if (viewOffers)
				count++;
			if (createOffers)
				count++;
			if (allClients)
				count++;
			
			// create permissions structure and add permissions
			Permission[] permissions = new Permission[count];
			count = 0;
			if (administrator) {
				permissions[count] = Permission.ADMINISTRATE;
				count++;
			}
			if (viewOffers) {
				permissions[count] = Permission.VIEWOFFERS;
				count++;
			}
			if (createOffers) {
				permissions[count] = Permission.CREATEOFFERS;
				count++;
			}
			if (allClients) {
				permissions[count] = Permission.ALLCLIENTS;
				count++;
			}
			
			// set if the user is enabled and the permissions
			UserDAO userDAO = new UserDB();
			if (userDAO.setUserEnabled(userId, enable) && userDAO.setPermissions(userId, permissions)) {
				// operation completed
				message = 41;
				logDAO.add(LogType.ACTION, user.getName() + " ha modificado los permisos de un usuario", 
						System.currentTimeMillis());

			} else {
				// error
				message = 42;
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar los permisos de un usuario, pero se ha producido un error", 
						System.currentTimeMillis());
			}
		} else {
			// error in parameters
			message = 42;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado modificar los permisos de un usuario con los parámetros incorrectos", 
					System.currentTimeMillis());
		}
		
		// return setpolicy.jsp with message	
		response.sendRedirect("setpolicy.jsp" + "?salespersonId=" + userId + "&message=" + message);	
		
	}

}
