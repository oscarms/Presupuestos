package servlet;

import java.io.IOException;
import java.io.PrintWriter;

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
 * Servlet implementation class SetConstructionRef
 * 
 * Updates a budget with the specified Construction Reference
 */
@WebServlet("/SetConstructionRef")
@MultipartConfig
public class SetConstructionRef extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetConstructionRef() {
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
		String construction = request.getParameter("construction");
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		PrintWriter out = response.getWriter();
		
		// filter EditBudget ensures that the user can edit the budget
		// update budget
		if (budgetDAO.setConstructionRef(budgetId, construction)) {
			// budget updated with new construction reference
			out.println("<div style='color:#444'>Referencia de obra actualizada<br /></div>");
			out.flush();
			logDAO.add(LogType.ACTION, user.getName() + " ha modificado la referencia de obra del presupuesto " + budgetId, 
					System.currentTimeMillis());
		} else {
			// error
			out.println("<div style='color:#C03'>Error al actualizar Referencia de obra<br /></div>");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar la referencia de obra del presupuesto " + budgetId
					+ ", pero ha fallado", System.currentTimeMillis());
		}
		out.close();

	}

}
