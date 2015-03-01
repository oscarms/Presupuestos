package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class RemoveNotification
 * 
 * Removes the notification to a user related with a
 * specified budget
 */
@WebServlet("/RemoveNotification")
@MultipartConfig
public class RemoveNotification extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveNotification() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters
		User user = (User)request.getSession(true).getAttribute("user");
		String budgetId = request.getParameter("budgetId");
		
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// remove notification
		if (budgetDAO.removeNotification(user.getId(), budgetId)) {
			// done
			logDAO.add(LogType.ACTION, user.getName() + " ha eliminado su notificación del presupuesto " + budgetId, 
					System.currentTimeMillis());
		} else {
			// error
			logDAO.add(LogType.ERROR, user.getName() + " no ha podido eliminar su notificación del presupuesto " + budgetId, 
					System.currentTimeMillis());
		}
		
		// redirect to notificationlist.jsp
		response.sendRedirect("notificationlist.jsp");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
