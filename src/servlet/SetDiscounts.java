package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Permission;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class SetDiscounts
 * 
 * Set the percentage of discounts for a product in
 * a budget. Only who can create offers is able to
 * call this servlet.
 */
@WebServlet("/SetDiscounts")
@MultipartConfig
public class SetDiscounts extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetDiscounts() {
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
		Float discount1;
		try {
			discount1 = Float.parseFloat(request.getParameter("discount1"));
		} catch (NumberFormatException e) {
			discount1 = null;
		}
		Float discount2;
		try {
			discount2 = Float.parseFloat(request.getParameter("discount2"));
		} catch (NumberFormatException e) {
			discount2 = null;
		}
		Float discount3;
		try {
			discount3 = Float.parseFloat(request.getParameter("discount3"));
		} catch (NumberFormatException e) {
			discount3 = null;
		}
		
		PrintWriter out = response.getWriter();
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// filter EditBudget ensures that the user can edit the budget
		// check permissions and parameters
		if (user.hasPermission(Permission.CREATEOFFERS) && sectionId != null && 
				productId != null && discount1 != null && discount2 != null && discount3 != null) {
			// update budget
			if (budgetDAO.setDiscounts(budgetId, sectionId, productId, discount1, discount2, discount3)) {
				// done
				out.println("<div style='color:#444'>Descuentos del artículo " + productId + " actualizados<br /></div>");
				out.flush();
				logDAO.add(LogType.ACTION, user.getName() + " ha actualizado descuentos de producto en el presupuesto " + budgetId, 
						System.currentTimeMillis());
			} else {
				// error
				out.println("<div style='color:#C03'>Error actualizando los descuentos del artículo " + productId + "<br /></div>");
				out.flush();
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado actualizar descuentos de producto en el presupuesto "
						+ budgetId + ", pero ha fallado", System.currentTimeMillis());
			}
			
		} else {
			// error in permissions or parameters
			out.println("<div style='color:#C03'>Error al actualizar los descuentos del artículo " + productId + "<br /></div>");
			out.flush();
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado actualizar descuentos de producto en el presupuesto " 
					+ budgetId + ", pero los parámetros eran incorrectos o no tenía permisos", System.currentTimeMillis());
		}
		
		out.close();
		
	}

}
