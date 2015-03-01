package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;
import budget.User;

/**
 * Servlet implementation class SetMailPassword
 * 
 * Sets the password as the user password
 * for sending emails
 */
@WebServlet("/SetMailPassword")
@MultipartConfig
public class SetMailPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetMailPassword() {
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
		// get user and parameters
		User user = (User)request.getSession(true).getAttribute("user");
		String password = request.getParameter("password");
				
		LogDAO logDAO = new LogDB();
		int message;
		
		// update the password
		UserDAO userDAO = new UserDB();
		if (password != null && userDAO.setMailPassword(user.getId(), password)) {
			// return message 21: password changed
			message = 21;
			logDAO.add(LogType.ACTION, user.getName() + " ha cambiado su contraseña de correo", 
					System.currentTimeMillis());
		} else {
			// return message 23: error
			message = 23;
			logDAO.add(LogType.ACTION, user.getName() + " ha intentado cambiar su contraseña de correo, pero se ha producido un error", 
					System.currentTimeMillis());
		}

		// return setmailpassword.jsp with message	
		response.sendRedirect("setmailpassword.jsp?message=" + message);
		
	}

}
