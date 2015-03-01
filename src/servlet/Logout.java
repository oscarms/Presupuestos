package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import budget.User;
import dao.LogType;
import database.LogDB;
import dao.LogDAO;

/**
 * Servlet implementation class Logout
 * 
 * Removes the session and redirects to login.jsp
 */
@WebServlet("/Logout")
@MultipartConfig
public class Logout extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Logout() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		if (session.getAttribute("user") != null){
			String email = ((User)session.getAttribute("user")).getEmail();
			session.invalidate();
			LogDAO logDAO = new LogDB();
			logDAO.add(LogType.ACTION, email + " ha cerrado su sesión", System.currentTimeMillis());
		}
		response.sendRedirect("login.jsp");
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request,response);
	}

}
