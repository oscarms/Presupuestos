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
 * Servlet implementation class SetGlobalTotal
 * 
 * Updates the GlobalTotal of the budget
 */
@WebServlet("/SetGlobalTotal")
@MultipartConfig
public class SetGlobalTotal extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetGlobalTotal() {
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
		String budgetId = request.getParameter("budgetId");
		boolean globalTotal = request.getParameter("globalTotal") != null && request.getParameter("globalTotal").equals("on"); 

		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// filter EditBudget ensures that the user can edit the budget
		// update budget
		if (budgetDAO.setGlobalTotal(budgetId, globalTotal)) {
			// done
			logDAO.add(LogType.ACTION, user.getName() + " ha actualizado el global total en el presupuesto " + budgetId, 
					System.currentTimeMillis());
		} else {
			// error
			logDAO.add(LogType.ERROR, user.getName() + " ha intentado actualizar el global total en el presupuesto " + budgetId
					+ ", pero ha fallado", System.currentTimeMillis());
		}

		// return createsections.jsp with budgetId
		response.sendRedirect("createsections.jsp?budgetId=" + budgetId);
	}

}
