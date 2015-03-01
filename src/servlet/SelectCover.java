package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;
import budget.User;

/**
 * Servlet implementation class SelectCover
 * 
 * Assigns the specified cover to the budget
 * and returns the list of covers updated
 */
@WebServlet("/SelectCover")
@MultipartConfig
public class SelectCover extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SelectCover() {
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
		// retrieve parameters
		User user = (User)request.getSession(true).getAttribute("user");
		String budgetId = request.getParameter("budgetId");
		Integer coverId;
		try {
			coverId = Integer.parseInt(request.getParameter("coverId"));
		} catch (NumberFormatException e) {
			coverId = null;
		}
		// permissions already checked by filter ViewBudget
		LogDAO logDAO = new LogDB();

		if (coverId != null) {
			// select cover
			BudgetDAO budgetDAO = new BudgetDB();
			if (!budgetDAO.setCover(budgetId, coverId)) {
				// error creating
				logDAO.add(LogType.ERROR, user.getName() + 
						" ha intentado seleccionar la portada de un presupuesto, pero ha fallado", 
						System.currentTimeMillis());
			} else
				logDAO.add(LogType.ACTION, user.getName() + " cambiado la portada del presupuesto " + budgetId, System.currentTimeMillis());
				// return covers.jsp?budgetId=
				response.sendRedirect("covers.jsp?budgetId=" + budgetId);
			
		} else {
			// parameter error
			logDAO.add(LogType.WARNING, user.getName() + 
					" ha intentado seleccionar una portada con los parámetros incorrectos", 
					System.currentTimeMillis());
		}

	}

}
