package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
 * Servlet implementation class SetExpirationDate
 * 
 * Updates a budget with the specified Expiration Date
 */
@WebServlet("/SetExpirationDate")
@MultipartConfig
public class SetExpirationDate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetExpirationDate() {
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
		String expiration = request.getParameter("expiration");
		long expirationMs;
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		PrintWriter out = response.getWriter();
		
		// filter EditBudget ensures that the user can edit the budget
		
		// expiration date date is dd/mm/yyyy and it is needed in format long
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			expirationMs = df.parse(expiration).getTime();
		} catch (ParseException e) {
			out.println("<div style='color:#C03'>Error en el formato de la fecha de validez<br /></div>");
			out.flush();
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado modificar la fecha de validez del presupuesto "
			+ budgetId + ", pero el formato no era válido", System.currentTimeMillis());
			out.close();
			return;
		}
		
		// update budget
		if (budgetDAO.setExpirationDate(budgetId, expirationMs)) {
			// budget updated with new expiration date
			out.println("<div style='color:#444'>Fecha de validez actualizada<br /></div>");
			out.flush();
			logDAO.add(LogType.ACTION, user.getName() + " ha modificado la fecha de validez del presupuesto " + budgetId, 
					System.currentTimeMillis());
			
		} else {
			// error
			out.println("<div style='color:#C03'>Error al actualizar Fecha de validez<br /></div>");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + " ha intentado modificar la fecha de validez del presupuesto " + budgetId
					+ ", pero ha fallado", System.currentTimeMillis());
		}
		out.close();
	}

}
