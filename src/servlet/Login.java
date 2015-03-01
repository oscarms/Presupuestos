package servlet;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import budget.User;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.LogDB;
import database.UserDB;

/**
 * Servlet implementation class Login
 * 
 * Retrieves the user introduced in login.jsp and redirects
 * to index.jsp if success. If the user tries to login
 * with a wrong password many times, disables the account.
 * 
 */
@WebServlet("/Login")
@MultipartConfig
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int MAXTRIES = 5; // if a user tries to login with a wrong password this times, the account is disabled
    private static int INACTIVEMS = 150;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
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
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		LogDAO logDAO = new LogDB();
		HttpSession session = request.getSession(true);
		session.setMaxInactiveInterval(INACTIVEMS);
		UserDAO userDAO = new UserDB();
		
		if (email == null)
			email = "";
		if (password == null)
			password = "";
		
		/* Installation user
		// REMOVE Installation User
		if (email.equals("testAdmin") && password.equals("testPassword")) {
			Permission[] permissions = {Permission.ADMINISTRATE,Permission.ALLCLIENTS, Permission.CREATEOFFERS, Permission.VIEWOFFERS};
			User user2 = new Salesperson(0, "testAdmin", "Administrador de Test", true, permissions) ;
			session.setAttribute("user", user2);
            logDAO.add(LogType.ACTION, email + " ha accedido al sistema", System.currentTimeMillis());
            response.sendRedirect("index.jsp");
            return;
		}
		Installation user end */
		
		// from dzone.com/snippets/get-md5-hash-few-lines-java
        MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			logDAO.add(LogType.CRITICAL, "No se ha podido cifrar la contraseña en servlet LOGIN", System.currentTimeMillis());
		}
        md.update(password.getBytes(),0,password.length());
        password = new BigInteger(1,md.digest()).toString(16);
        
        User user = userDAO.getUser(email, password);
        if (user == null) {
        	// login failed, save the try
			@SuppressWarnings("unchecked")
			Map<String,Integer> tries = (Map<String,Integer>)session.getAttribute("tries");
        	if (tries == null)
        		tries = new HashMap<String,Integer>();
        	if (!tries.containsKey(email))
        		tries.put(email, 1 ); // set 1 try
        	else
        		tries.put(email, tries.get(email) + 1 ); // add 1 try
        	session.setAttribute("tries", tries);
        	
        	// if many tries to disable user and return message 1
        	if ( tries.get(email) >= MAXTRIES) {
        		userDAO.setUserEnabled(email, false);
        		request.setAttribute("email", email);
        		request.setAttribute("message", 1);
        		logDAO.add(LogType.WARNING, email + " ha introducido mal su contraseña varias "
        				+ "veces y su cuenta ha sido deshabilitada", System.currentTimeMillis());
        	} else { // return message 3
        		request.setAttribute("email", email);
        		request.setAttribute("message", 3);
        		logDAO.add(LogType.ACTION, email + " ha introducido mal su contraseña",
        				System.currentTimeMillis());
        	}

        	// redirect to login.jsp with attributes
        	request.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
        	return;
        	
        } else {
        	if (user.isEnabled()) {
        		// if enabled login
        		session.removeAttribute("tries");
                session.setAttribute("user", user);
                logDAO.add(LogType.ACTION, email + " ha iniciado sesión",
        				System.currentTimeMillis());
                response.sendRedirect("index.jsp");
                return;
                
        	} else { // user disabled, return message 2
        		request.setAttribute("email", email);
        		request.setAttribute("message", 2);
        		logDAO.add(LogType.ACTION, email + " ha intentado inciar sesión pero su"
        				+ " cuenta está deshabilitada", System.currentTimeMillis());
        		request.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
        		return;
        	}
        }
        
	}

}
