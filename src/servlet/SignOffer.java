package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mail.Mail;
import budget.Budget;
import budget.Permission;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class SignOffer
 * 
 * Checks the permissions of the user and
 * the validity of the offer, and signs
 * it with the current user
 */
@WebServlet("/SignOffer")
@MultipartConfig
public class SignOffer extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignOffer() {
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
		// check permissions and validity
		if (budget.isOffer() && user.hasPermission(Permission.ADMINISTRATE) && budget.isValid()) {
			// sign offer
			if (budgetDAO.signOffer(budgetId, user.getId(), System.currentTimeMillis())) {
				// done
				logDAO.add(LogType.ACTION, user.getName() + " ha firmado la oferta " + budgetId, 
						System.currentTimeMillis());
				
				// Create notifications: For all administrators (but the current one)
				budgetDAO.addNotification(user.getId(), budgetId, true);
				// For the salesperson of the client of the budget, if is not the current user
				if (!budget.getSalesperson().equals(user) && budget.getSalesperson().hasPermission(Permission.VIEWOFFERS)) {
					budgetDAO.addNotification(budget.getSalesperson().getId(), budgetId);
					// Send email to the salesperson of the client of the budget if can view offers
					new Mail().sendNotification(budget.getSalesperson(), budget);
				}

				// redirect to budget.jsp
				response.sendRedirect("budget.jsp?budgetId=" + budgetId);
				
			} else {
				// error
				logDAO.add(LogType.ERROR, user.getName() + " ha tenido un error al firmar la oferta " + budgetId, 
						System.currentTimeMillis());
				
				// redirect to createbudget.jsp
				message = 4;
				response.sendRedirect("createbudget.jsp?budgetId=" + budgetId + "&message=" + message);	
				
			}

		} else {
			// error in validation
			logDAO.add(LogType.WARNING, user.getName() + " ha intantado firmar la oferta " + budgetId
					+ ", pero no era válida o no tenía permisos", System.currentTimeMillis());
			
			// redirect to createbudget.jsp
			message = 1;
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
