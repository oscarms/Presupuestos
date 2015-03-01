package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mail.Mail;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import dao.UserDAO;
import database.BudgetDB;
import database.LogDB;
import database.UserDB;
import budget.Budget;
import budget.Permission;
import budget.User;

/**
 * Servlet implementation class CreateBudget
 * 
 * Checks the validity of a budget, and sets
 * its creation date.
 * Also creates notifications.
 */
@WebServlet("/CreateBudget")
@MultipartConfig
public class CreateBudget extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateBudget() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters
		User user = (User)request.getSession(true).getAttribute("user");
		String budgetId = request.getParameter("budgetId");
		
		int message;
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		Budget budget = budgetDAO.getBudget(budgetId);
		// filter EditBudget ensures that the user can edit the budget
		// check validity
		if (budget.isValid()) {
			// set creation date
			if (budgetDAO.createBudget(budgetId, System.currentTimeMillis())) {
				// done
				logDAO.add(LogType.ACTION, user.getName() + " ha creado el presupuesto " + budgetId, 
						System.currentTimeMillis());
				
				if (!budget.isOffer()) { // if is offer, do not create notifications until sign the notification
					// Create notifications: For all administrators (but the current one)
					budgetDAO.addNotification(user.getId(), budgetId, true);
					// For the salesperson of the client of the budget, if is not the current user
					if (!budget.getSalesperson().equals(user))
						budgetDAO.addNotification(budget.getSalesperson().getId(), budgetId);
				} else { // is an offer that must be signed
					// Notify all administrators by email
					UserDAO userDAO = new UserDB();
					User[] users = userDAO.getSalespeople();
					
					for (User i : users) {
						if (i.hasPermission(Permission.ADMINISTRATE))
							new Mail().sendNotification(i, budget);
					}
					
				}
				
				// redirect to budget.jsp
				response.sendRedirect("budget.jsp?budgetId=" + budgetId);
				
			} else {
				// error
				logDAO.add(LogType.ERROR, user.getName() + " ha tenido un error al crear el presupuesto " + budgetId, 
						System.currentTimeMillis());
				
				// redirect to createbudget.jsp
				message = 4;
				response.sendRedirect("createbudget.jsp?budgetId=" + budgetId + "&message=" + message);
				
			}

		} else {
			// error in validation
			logDAO.add(LogType.WARNING, user.getName() + " ha intantado crear el presupuesto " + budgetId
					+ ", pero no era válido", System.currentTimeMillis());
			
			// redirect to createbudget.jsp
			message = 2;
			response.sendRedirect("createbudget.jsp?budgetId=" + budgetId + "&message=" + message);
			
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
