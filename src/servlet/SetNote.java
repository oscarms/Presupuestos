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
 * Servlet implementation class SetNote
 * 
 * Updates a budget with the specified final note
 */
@WebServlet("/SetNote")
@MultipartConfig
public class SetNote extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetNote() {
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
		String note = request.getParameter("note");
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		PrintWriter out = response.getWriter();
		
		// filter EditBudget ensures that the user can edit the budget
		// update budget
		if (budgetDAO.setNote(budgetId, note)) {
			// budget updated with new note
			out.println("<div style='color:#444'>Nota final actualizada<br /></div>");
			out.flush();
			logDAO.add(LogType.ACTION, user.getName() + " ha modificado la nota final del presupuesto " + budgetId, 
					System.currentTimeMillis());
		} else {
			// error
			out.println("<div style='color:#C03'>Error al actualizar Nota final<br /></div>");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar la nota final del presupuesto " + budgetId
					+ ", pero ha fallado", System.currentTimeMillis());
		}
		out.close();

	}

}
