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
 * Servlet implementation class AddProduct
 * 
 * Adds a product to the specified section of a budget
 */
@WebServlet("/AddProduct")
@MultipartConfig
public class AddProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddProduct() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters and user
		String budgetId = request.getParameter("budgetId");
		Integer sectionId;
		try {
			sectionId = Integer.parseInt(request.getParameter("sectionId"));
		} catch (NumberFormatException e) {
			sectionId = null;
		}
		Integer productId;
		try {
			productId = Integer.parseInt(request.getParameter("productId"));
		} catch (NumberFormatException e) {
			productId = null;
		}
		
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
				
		// filter EditBudget ensures that the user can edit the budget
		// check parameters
		if (sectionId != null && productId != null) {
			if (budgetDAO.addProduct(budgetId, sectionId, productId)) {
				// done
				logDAO.add(LogType.ACTION, user.getName() + " ha añadido un artículo en el presupuesto " + budgetId, 
						System.currentTimeMillis());
			} else {
				// error
				logDAO.add(LogType.MESSAGE, user.getName() + " ha intentado añadir un artículo en el presupuesto " + budgetId
						+ ", pero ha fallado", System.currentTimeMillis());
			}

		} else {
			// error in parameters
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado añadir un artículo en el presupuesto " + budgetId
					+ ", pero los parámetros eran incorrectos", System.currentTimeMillis());
		}
		// return createsections.jsp with budgetId
		response.sendRedirect("createsections.jsp?budgetId=" + budgetId);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
