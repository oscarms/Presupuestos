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
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;

/**
 * Servlet implementation class SaveClient
 * 
 * Creates or updates a client
 */
@WebServlet("/SaveClient")
@MultipartConfig
public class SaveClient extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveClient() {
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
		Integer clientId;
		String number = request.getParameter("number");
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		if (email.trim().length() < 1)
			email = null;
		String phone = request.getParameter("phone");
		String person = request.getParameter("person");
		String address = request.getParameter("address");
		String town = request.getParameter("town");
		String province = request.getParameter("province");
		String country = request.getParameter("country");
		String postalcode = request.getParameter("postalcode");
		String notes = request.getParameter("notes");
		boolean active = (request.getParameter("active") != null && request.getParameter("active").equals("on"));
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		UserDAO userDAO = new UserDB();
		int message;
		try {
			clientId = Integer.parseInt(request.getParameter("clientId"));
		} catch (NumberFormatException e) {
			clientId = null;
		}
		
		if (clientId != null) {
			// editing existing client
			Client client = userDAO.getClient(clientId);
			// check permissions (user is the salesperson of the client, or has ALLCLIENTS permission
			if (client != null && 
					(client.getSalesperson().equals(user) || user.hasPermission(Permission.ALLCLIENTS)) ) {
				Client updatedClient = 
						new Client(clientId, number, address, town, 
								province, country, postalcode, name, 
								email, phone, person, notes, active, 
								client.getSalesperson());
				if (number != null && number.length() > 0 && userDAO.update(updatedClient)) {
					// client updated
					message = 1;
					logDAO.add(LogType.ACTION, user.getName() + " ha editado el cliente " + number, 
							System.currentTimeMillis());
					
				} else {
					// error updating
					message = 5;
					logDAO.add(LogType.ERROR, user.getName() + " ha intentado editar un cliente, pero ha habido un fallo", 
							System.currentTimeMillis());
				}

			} else {
				message = 5;
				logDAO.add(LogType.WARNING, user.getName() + " ha intentado editar un cliente con parámetros incorrectos", 
						System.currentTimeMillis());
			}

		} else {
			// creating new client (the salesperson assigned is the current user)
			Client newClient = 
					new Client(number, address, town, 
							province, country, postalcode, name, 
							email, phone, person, notes, active, 
							userDAO.getSalesperson( user.getId() ));
			clientId = userDAO.create(newClient);
			if (number != null && number.length() > 0 && clientId >= 0) {
				// client created
				message = 2;
				logDAO.add(LogType.ACTION, user.getName() + " ha creado el cliente " + number, 
						System.currentTimeMillis());
				
			} else {
				// error creating
				message = 3;
				logDAO.add(LogType.WARNING, user.getName() + " ha intentado crear un cliente, pero ha habido un fallo", 
						System.currentTimeMillis());
				response.sendRedirect("client.jsp?isNew=1&message=" + message);
				return;
			}
			
		}

		// return client.jsp with message
		response.sendRedirect("client.jsp?clientId=" + clientId + "&message=" + message);
		
	}

}
