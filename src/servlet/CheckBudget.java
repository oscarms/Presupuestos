package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Budget;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class CheckBudget
 * 
 * Checks if the budget is valid, and returns
 * to the creation of the budget with a list
 * of errors
 */
@WebServlet("/CheckBudget")
@MultipartConfig
public class CheckBudget extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckBudget() {
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
		Budget budget = budgetDAO.getBudget(budgetId);
		// filter EditBudget ensures that the user can edit the budget
		
		// check budget and redirect with messages
		logDAO.add(LogType.ACTION, user.getName() + " ha comprobado el presupuesto " + budgetId, 
				System.currentTimeMillis());
		Integer[] messages = budget.check();
		request.setAttribute("messages", messages);
		request.getServletContext().getRequestDispatcher("/createbudget.jsp?budgetId=" + budgetId).forward(request, response);	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
