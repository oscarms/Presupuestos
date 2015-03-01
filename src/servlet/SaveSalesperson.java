package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Permission;
import budget.Salesperson;
import budget.User;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;

/**
 * Servlet implementation class SaveSalesperson
 * 
 * Creates or modifies the salesperson with a new
 * e-mail or name
 */
@WebServlet("/SaveSalesperson")
@MultipartConfig
public class SaveSalesperson extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveSalesperson() {
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
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		int message;
		
		try {
			userId = Integer.parseInt(request.getParameter("salespersonId"));
		} catch (NumberFormatException e) {
			userId = null;
		}
		
		/*
		 * check if it is a valid case:
		 * - A userId == user.getId() is modifying himself
		 * - A userId != user.getId() and user is an administrator modifying another user
		 * - userId == null and user is an administrator creating a new user
		 * 
		 * check if all parameters exists and are not empty: name, email
		 * 
		 */

		if ( ( (userId != null && userId == user.getId()) || user.hasPermission(Permission.ADMINISTRATE)) &&
				name != null && email != null && name.length() > 0 && email.length() > 0 ) {
			
			UserDAO userDAO = new UserDB();
			
			if (userId == null && user.hasPermission(Permission.ADMINISTRATE)) {
				// case creating a new user
				userId = userDAO.create(new Salesperson(-1, email, name, false, null) );
				if ( userId != null && userId >= 0 ) {
					// user created successfully
					message = 4;
					logDAO.add(LogType.ACTION, user.getName() + " ha creado el comercial " + name, 
							System.currentTimeMillis());
				} else {
					// user not created
					message = 1;
					logDAO.add(LogType.ERROR, user.getName() + " ha intentado crear un comercial sin éxito", 
							System.currentTimeMillis());
				}
			
			} else if (userId == user.getId() || (userId != user.getId() && user.hasPermission(Permission.ADMINISTRATE))) {
				// case updating a user
				Salesperson salesperson = userDAO.getSalesperson(userId);
				
				if (userDAO.update(
						new Salesperson(userId, email, name, salesperson.isEnabled(), salesperson.getPermissions()) )) {
					// updated successfully
					message = 2;
					logDAO.add(LogType.ACTION, user.getName() + " ha editado el comercial " + name, 
							System.currentTimeMillis());
				} else {
					// 
					message = 3;
					logDAO.add(LogType.ERROR, user.getName() + " ha intentado editar un comercial sin éxito", 
							System.currentTimeMillis());
				}
				
			} else {
				// other case not possible
				// return message 3: error
				message = 3;
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar un comercial, pero ha habido un error", 
						System.currentTimeMillis());
			}
		} else {
			// return message 3: error
			message = 3;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado modificar un comercial, pero no tenía permisos o fallaban parámetros", 
					System.currentTimeMillis());
		}
		
		// return salesperson.jsp with message	
		response.sendRedirect("salesperson.jsp?salespersonId=" + userId + "&message=" + message); 
		
	}

}
