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
 * Servlet implementation class SetProductQuantity
 * 
 * Set the quantity of the specified product in the budget
 */
@WebServlet("/SetProductQuantity")
@MultipartConfig
public class SetProductQuantity extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetProductQuantity() {
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
		Float quantity;
		try {
			quantity = Float.parseFloat(request.getParameter("quantity"));
		} catch (NumberFormatException e) {
			quantity = null;
		}
		
		PrintWriter out = response.getWriter();
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		
		// filter EditBudget ensures that the user can edit the budget
		// check permissions and parameters
		if (sectionId != null && productId != null && quantity != null) {
			// update budget
			if (budgetDAO.setQuantity(budgetId, sectionId, productId, quantity)) {
				// done
				out.println("<div style='color:#444'>Cantidad del artículo " + productId + " actualizada<br /></div>");
				out.flush();
				logDAO.add(LogType.ACTION, user.getName() + " ha actualizado cantidades de producto en el presupuesto " + budgetId, 
						System.currentTimeMillis());
			} else {
				// error
				out.println("<div style='color:#C03'>Error actualizando la cantidad del artículo " + productId + "<br /></div>");
				out.flush();
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado actualizar cantidades de producto en el presupuesto "
						+ budgetId + ", pero ha fallado", System.currentTimeMillis());
			}
			
		} else {
			// error in parameters
			out.println("<div style='color:#C03'>Error al actualizar la cantidad del artículo " + productId + "<br /></div>");
			out.flush();
			logDAO.add(LogType.MESSAGE, user.getName() + " ha intentado actualizar cantidades de producto en el presupuesto " 
					+ budgetId + ", pero los parámetros eran incorrectos", System.currentTimeMillis());
		}
		
		out.close();
		
	}

}
