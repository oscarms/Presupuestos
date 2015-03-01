package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.Budget;
import budget.Permission;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class Sort
 * 
 * Sorts the specified attachment, section
 * or product in section in the required new
 * position. Returns the updated list.
 */
@WebServlet("/Sort")
@MultipartConfig
public class Sort extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Sort() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LogDAO logDAO = new LogDB();
		User user = (User)request.getSession(true).getAttribute("user");
		
		// get parameters
		String budgetId; // always required
		Integer attachmentId;
		Integer sort; // always required
		Integer sectionId;
		Integer productId;
		
		budgetId = request.getParameter("budgetId");
		try {
			attachmentId = Integer.parseInt(request.getParameter("attachmentId"));
		} catch (NumberFormatException e) {
			attachmentId = null;
		}
		try {
			sort = Integer.parseInt(request.getParameter("sort"));
			if ( !(sort == 1) && !(sort == -1) )
				sort = null;
		} catch (NumberFormatException e) {
			sort = null;
		}
		try {
			productId = Integer.parseInt(request.getParameter("productId"));
		} catch (NumberFormatException e) {
			productId = null;
		}
		try {
			sectionId = Integer.parseInt(request.getParameter("sectionId"));
		} catch (NumberFormatException e) {
			sectionId = null;
		}
		
		if (budgetId == null || sort == null) {
			// error in parameters
			logDAO.add(LogType.WARNING, user.getName() + " ha llamado al servlet Sort con parámetros incorrectos", 
					System.currentTimeMillis());
			response.sendRedirect("Logout");
			return;	
		}
		
		// check permissions
		BudgetDAO budgetDAO = new BudgetDB();
		Budget budget = budgetDAO.getBudget(budgetId);
		boolean after = (sort == 1);
		if ( attachmentId != null && sectionId == null && productId == null ) {
			// case sorting attachment, while viewing a budget
			// check if the user can view the budget
			if (!user.equals(budget.getAuthor()) && 
					!(user.hasPermission(Permission.ADMINISTRATE) && budget.isOffer() && 
							budget.getCreationDate() > 0 && budget.getSigner() == null)
							) {
				logDAO.add(LogType.WARNING, user.getName() + 
						" ha intentado ordenar un adjunto de un presupuesto sin permisos", 
						System.currentTimeMillis());
				response.sendRedirect("Logout");
				return;
			}
			// at this point, the user has permission to sort the attachment
				// sort attachment
				budgetDAO.sortAttachment(budgetId, attachmentId, after);
				logDAO.add(LogType.ACTION, user.getName() + " ha ordenado adjuntos en presupuesto " + budgetId, System.currentTimeMillis());
				// return attachments.jsp
				response.sendRedirect("attachments.jsp?budgetId=" + budgetId);
		} else if (attachmentId == null && sectionId != null) {
			// case sorting section or product in section, while editing a budget
			// check if the user can edit the budget, and the budget is editable
			if (!user.equals(budget.getAuthor()) && 
					!(user.hasPermission(Permission.ADMINISTRATE) && budget.isOffer() && 
							budget.getCreationDate() > 0 && budget.getSigner() == null) 
							|| (budget.isOffer() && budget.getSigner() != null) || 
							(!budget.isOffer() && budget.getCreationDate() > 0)
							) {
				logDAO.add(LogType.WARNING, user.getName() + " ha intentado modificar un presupuesto sin permisos", 
						System.currentTimeMillis());
				response.sendRedirect("Logout");
				return;
			}
			// at this point, the user has permissions to sort the section or product in section
			if (productId == null) {
				// sort section
				budgetDAO.sortSection(budgetId, sectionId, after);
				logDAO.add(LogType.ACTION, user.getName() + " ha ordenado secciones en presupuesto " + budgetId, System.currentTimeMillis());

			} else { // productId != null
				// sort product
				budgetDAO.sortProduct(budgetId, sectionId, productId, after);
				logDAO.add(LogType.ACTION, user.getName() + " ha ordenado productos en presupuesto " + budgetId, System.currentTimeMillis());

			}
			// return createsections.jsp
			response.sendRedirect("createsections.jsp?budgetId=" + budgetId);
		} else {
			// error in parameters
			logDAO.add(LogType.WARNING, user.getName() + " ha llamado al servlet Sort con parámetros incorrectos", 
					System.currentTimeMillis());
			response.sendRedirect("Logout");
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
