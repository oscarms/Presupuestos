package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.User;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;

/**
 * Servlet implementation class RemoveMailPassword
 * 
 * Removes the user password for sending emails
 */
@WebServlet("/RemoveMailPassword")
@MultipartConfig
public class RemoveMailPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveMailPassword() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// get user
		User user = (User)request.getSession(true).getAttribute("user");
				
		LogDAO logDAO = new LogDB();
		int message;
		
		// remove the password
		UserDAO userDAO = new UserDB();
		if (userDAO.setMailPassword(user.getId(), null)) {
			// return message 22: password removed
			message = 22;
			logDAO.add(LogType.ACTION, user.getName() + " ha eliminado su contraseña de correo", 
					System.currentTimeMillis());
		} else {
			// return message 23: error
			message = 23;
			logDAO.add(LogType.ACTION, user.getName() + " ha intentado eliminar su contraseña de correo, pero se ha producido un error", 
					System.currentTimeMillis());
		}

		// return setmailpassword.jsp with message	
		response.sendRedirect("setmailpassword.jsp?message=" + message);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
