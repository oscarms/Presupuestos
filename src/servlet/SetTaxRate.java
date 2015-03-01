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
 * Servlet implementation class SetTaxRate
 * 
 * Updates the tax rate of the budget
 */
@WebServlet("/SetTaxRate")
@MultipartConfig
public class SetTaxRate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetTaxRate() {
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
		Float tax;
		try {
			tax = Float.parseFloat(request.getParameter("tax"));
		} catch (NumberFormatException e) {
			tax = null;
		}
		
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// filter EditBudget ensures that the user can edit the budget
		// check parameters
		if (tax != null) {
			// update budget
			if (budgetDAO.setTaxRate(budgetId, tax)) {
				// done
				logDAO.add(LogType.ACTION, user.getName() + " ha actualizado los impuestos del presupuesto " + budgetId, 
						System.currentTimeMillis());
			} else {
				// error
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado actualizar los impuestos del presupuesto " + budgetId
						+ ", pero ha fallado", System.currentTimeMillis());
			}
			
		} else {
			// error in parameters
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado actualizar los impuestos del presupuesto " + budgetId
					+ ", pero los parámetros eran incorrectos", System.currentTimeMillis());
		}

		// return createsections.jsp with budgetId
		response.sendRedirect("createsections.jsp?budgetId=" + budgetId);
	}

}
