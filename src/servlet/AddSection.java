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
 * Servlet implementation class AddSection
 * 
 * Creates a new section in a budget
 */
@WebServlet("/AddSection")
@MultipartConfig
public class AddSection extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddSection() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters and user
		String budgetId = request.getParameter("budgetId");
		String name = request.getParameter("name"); // optional
		if (name == null)
			name = "";
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// filter EditBudget ensures that the user can edit the budget
		// update budget
		if ( budgetDAO.addSection(budgetId, name) >= 0 ) {
			// section added
			logDAO.add(LogType.ACTION, user.getName() + " ha añadido un capítulo al presupuesto " + budgetId, 
					System.currentTimeMillis());
		} else {
			// error
			logDAO.add(LogType.ERROR, user.getName() + " ha intentado añadir un capítulo al presupuesto " + budgetId
					+ ", pero ha fallado", System.currentTimeMillis());
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
