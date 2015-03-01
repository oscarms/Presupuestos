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

import mail.Mail;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;
import budget.Permission;
import budget.User;

/**
 * Servlet implementation class SendNewPassword
 * 
 * Changes the password of the user with a
 * random-generated and sends it to him by e-mail
 */
@WebServlet("/SendNewPassword")
@MultipartConfig
public class SendNewPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendNewPassword() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters and user
		Integer userId;
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		int message;

		try {
			userId = Integer.parseInt(request.getParameter("salespersonId"));
		} catch (NumberFormatException e) {
			userId = null;
		}
		
		// check permissions: only administrator can send a new password to other users
		if (user.hasPermission(Permission.ADMINISTRATE) && userId != null && user.getId() != userId ) {
			
			// generate new password
			String newPassword = Long.toString( (Double.doubleToLongBits(Math.random())),Character.MAX_RADIX );
			
			// cipher password			
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				logDAO.add(LogType.CRITICAL, "No se ha podido cifrar la contraseña en servlet SendNewPassword", System.currentTimeMillis());
			}
	        md.update(newPassword.getBytes(),0,newPassword.length());
	        String passwordCiphered = new BigInteger(1,md.digest()).toString(16);
			
	        // store in database
			UserDAO userDAO = new UserDB();
			if (userDAO.setPassword(userId, passwordCiphered)) {
				// send by email
				if (new Mail().sendPassword(userDAO.getSalesperson(userId), newPassword)) { // can throw NullPointerException
					// return message 31: password changed and sent by email
					message = 31;
					logDAO.add(LogType.ACTION, user.getName() + " ha enviado una nueva contraseña a un usuario", 
							System.currentTimeMillis());
					
				} else {
					// return message 32: password changed but not sent by email
					message = 32;
					logDAO.add(LogType.WARNING, user.getName() + " ha cambiado la contraseña a un usuario, pero no se ha enviado por email", 
							System.currentTimeMillis());
				}
			} else {
				// return message 33: error (password not changed)
				message = 33;
				logDAO.add(LogType.WARNING, user.getName() + " ha intentado enviar una nueva contraseña a un usuario, pero no se ha podido actualizar", 
						System.currentTimeMillis());
			}			
			
		} else {
			// return message 33: error (not permissions)
			message = 31;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado enviar una nueva contraseña a un usuario, pero no tenía permisos", 
					System.currentTimeMillis());
		}
		
		// return sendpassword.jsp with message	
		response.sendRedirect("sendpassword.jsp?salespersonId=" + userId + "&message=" + message);
				
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
