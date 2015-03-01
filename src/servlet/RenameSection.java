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
 * Servlet implementation class RenameSection
 * 
 * Renames a section in a budget
 */
@WebServlet("/RenameSection")
@MultipartConfig
public class RenameSection extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RenameSection() {
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
		Integer sectionId;
		String name = request.getParameter("name");
		try {
			sectionId = Integer.parseInt(request.getParameter("sectionId"));
		} catch (NumberFormatException e) {
			sectionId = null;
		}
		
		PrintWriter out = response.getWriter();
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// filter EditBudget ensures that the user can edit the budget
		// check parameters
		if (sectionId != null && name != null) {
			// update section name
			if (budgetDAO.renameSection(budgetId, sectionId, name)) {
				// done
				out.println("<div style='color:#444'>Capítulo " + name + " renombrado<br /></div>");
				out.flush();
				logDAO.add(LogType.ACTION, user.getName() + " ha renombrado un capítulo en el presupuesto " + budgetId, 
						System.currentTimeMillis());
			} else {
				// error
				out.println("<div style='color:#C03'>Error renombrando el capítulo<br /></div>");
				out.flush();
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado renombrar un capítulo en el presupuesto " + budgetId
						+ ", pero ha fallado", System.currentTimeMillis());
			}
			
		} else {
			// error in parameters
			out.println("<div style='color:#C03'>Error al renombrar el capítulo<br /></div>");
			out.flush();
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado renombrar un capítulo en el presupuesto " + budgetId
					+ ", pero los parámetros eran incorrectos", System.currentTimeMillis());
			
		}
		out.close();
		
	}

}
