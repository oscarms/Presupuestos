package servlet;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
 * Servlet implementation class ChangePassword
 * 
 * Checks the old password introduced by the user
 * and sets the new password as password of the user
 */
@WebServlet("/ChangePassword")
@MultipartConfig
public class ChangePassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangePassword() {
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
		String oldPassword = request.getParameter("oldPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");

		LogDAO logDAO = new LogDB();
		int message;
		
		// check if the new password is safe enough
		if (newPassword != null && newPassword.length() > 0 && confirmPassword != null && 
				confirmPassword.length() > 0 && newPassword.equals(confirmPassword) ) {
			
			// encrypt old password
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				logDAO.add(LogType.CRITICAL, "No se ha podido cifrar la vieja contraseña en servlet ChangePassword", System.currentTimeMillis());
			}
	        md.update(oldPassword.getBytes(),0,oldPassword.length());
	        oldPassword = new BigInteger(1,md.digest()).toString(16);

	        // check old password
			UserDAO userDAO = new UserDB();
			User user2 = userDAO.getUser(user.getEmail(), oldPassword);
			
			if (user.equals(user2)) {

				// encrypt new password
		        md.update(newPassword.getBytes(),0,newPassword.length());
		        newPassword = new BigInteger(1,md.digest()).toString(16);
				
		        // update the password
		        if (userDAO.setPassword(user.getId(), newPassword)) {
		        	// return message 11: password changed
		        	message = 11;
		        	logDAO.add(LogType.ACTION, user.getName() + " ha cambiado su contraseña", System.currentTimeMillis());
		        } else {
		        	// return message 14: error
		        	message = 14;
		        	logDAO.add(LogType.ERROR, user.getName() + " ha intentado cambiar su contraseña, pero se ha producido un error", 
		        			System.currentTimeMillis());
		        }
		        
			} else {
				// return message 13: old password not matches
				message = 13;
				logDAO.add(LogType.WARNING, user.getName() + " ha intentado cambiar su contraseña, pero la contraseña anterior no coincidía", 
						System.currentTimeMillis());
			}

		} else {
			// return message 12: new password not valid
			message = 12;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado cambiar su contraseña, pero la nueva contraseña no coincidía", 
					System.currentTimeMillis());
		}
		
		// return changepassword.jsp with message
		response.sendRedirect("changepassword.jsp?message=" + message);

	}

}
