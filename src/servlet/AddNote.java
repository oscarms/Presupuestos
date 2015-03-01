package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Annotation;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class AddNote
 * 
 * Adds a note to the budget and returns
 * the list of notes updated
 */
@WebServlet("/AddNote")
@MultipartConfig
public class AddNote extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddNote() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters
		User user = (User)request.getSession(true).getAttribute("user");
		String budgetId = request.getParameter("budgetId");
		String text = request.getParameter("text");
		
		LogDAO logDAO = new LogDB();

		// check parameter
		if (text != null && text.length() > 0) {
			// permissions and budget already checked by filter ViewBudget

			// add note
			BudgetDAO budgetDAO = new BudgetDB();
			if (!budgetDAO.create(budgetId, new Annotation(System.currentTimeMillis(), text))) {
				// error creating
				logDAO.add(LogType.ERROR, user.getName() + 
						" ha intentado añadir una nota a un presupuesto, pero ha fallado", 
						System.currentTimeMillis());
			} else {
				logDAO.add(LogType.ACTION, user.getName() + " ha añadido una nota en presupuesto " + budgetId, System.currentTimeMillis());
				// return notes.jsp?budgetId=
				response.sendRedirect("notes.jsp?budgetId=" + budgetId);
			}
		} else {
			// parameter error
			logDAO.add(LogType.WARNING, user.getName() + 
					" ha intentado añadir una nota a un presupuesto con los parámetros incorrectos", 
					System.currentTimeMillis());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
