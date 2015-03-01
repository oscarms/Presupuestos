package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import storage.FileLocalStorage;
import budget.Budget;
import budget.Permission;
import budget.User;
import dao.BudgetDAO;
import dao.FileDAO;
import dao.FileType;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class Remove
 * 
 * Removes the specified document, attachment, 
 * section or product in section. Returns the
 * updated list.
 */
@WebServlet("/Remove")
@MultipartConfig
public class Remove extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Remove() {
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
		Integer documentId;
		Integer sectionId;
		Integer productId;
		
		budgetId = request.getParameter("budgetId");
		try {
			attachmentId = Integer.parseInt(request.getParameter("attachmentId"));
		} catch (NumberFormatException e) {
			attachmentId = null;
		}
		try {
			documentId = Integer.parseInt(request.getParameter("documentId"));
		} catch (NumberFormatException e) {
			documentId = null;
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
		
		if (budgetId == null) {
			// error in parameters
			logDAO.add(LogType.WARNING, user.getName() + " ha llamado al servlet Remove con parámetros incorrectos", 
					System.currentTimeMillis());
			response.sendRedirect("Logout");
			return;	
		}
		
		// check permissions
		BudgetDAO budgetDAO = new BudgetDB();
		Budget budget = budgetDAO.getBudget(budgetId);
		if ( (attachmentId != null || documentId != null) && 
				!(attachmentId != null && documentId != null) && 
				sectionId == null && productId == null ) {
			// case removing attachment or document, while viewing a budget
			// check if the user can view the budget
			if ( !((user.equals(budget.getSalesperson()) || user.hasPermission(Permission.ALLCLIENTS)) 
					&& ( (budget.isOffer() && user.hasPermission(Permission.VIEWOFFERS) )
					|| !budget.isOffer() ) ) ) {
				logDAO.add(LogType.WARNING, user.getName() + 
						" ha intentado eliminar un fichero de un presupuesto sin permisos", 
						System.currentTimeMillis());
				response.sendRedirect("Logout");
				return;
			}
			// at this point, the user has permission to remove the document or attachment
			if (attachmentId != null) {
				// remove attachment
				if ( budgetDAO.removeAttachment(budgetId, attachmentId) ) {
					FileDAO fileDAO = new FileLocalStorage();
					fileDAO.delete(FileType.ATTACHMENT, budgetId + "_" + documentId);
					logDAO.add(LogType.ACTION, user.getName() + " ha eliminado un adjunto en presupuesto " + budgetId, System.currentTimeMillis());
				} else {
					logDAO.add(LogType.WARNING, user.getName() + " ha intentado eliminar un adjunto inexistente en presupuesto " + budgetId, System.currentTimeMillis());
				}
				
				// return attachments.jsp
				response.sendRedirect("attachments.jsp?budgetId=" + budgetId);
			} else { // documentId != null
				// remove document
				if (budgetDAO.removeDocument(budgetId, documentId)) {
					FileDAO fileDAO = new FileLocalStorage();
					fileDAO.delete(FileType.DOCUMENT, budgetId + "_" + documentId);
					logDAO.add(LogType.ACTION, user.getName() + " ha eliminado un documento en presupuesto " + budgetId, System.currentTimeMillis());
				} else
					logDAO.add(LogType.WARNING, user.getName() + " ha intentado eliminar un documento inexistente en presupuesto " + budgetId, System.currentTimeMillis());
				// return documents.jsp
				response.sendRedirect("documents.jsp?budgetId=" + budgetId);
			}
			
		} else if (attachmentId == null && documentId == null && sectionId != null) {
			// case removing section or product in section, while editing a budget
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
			// at this point, the user has permissions to remove the section or product in section
			if (productId == null) {
				// remove section
				budgetDAO.removeSection(budgetId, sectionId);
				logDAO.add(LogType.ACTION, user.getName() + " ha eliminado un capítulo en presupuesto " + budgetId, System.currentTimeMillis());
			} else { // productId != null
				// remove product
				budgetDAO.removeProduct(budgetId, sectionId, productId);
				logDAO.add(LogType.ACTION, user.getName() + " ha eliminado un producto en presupuesto " + budgetId, System.currentTimeMillis());
			}
			// return createsections.jsp
			response.sendRedirect("createsections.jsp?budgetId=" + budgetId);
		} else {
			// error in parameters
			logDAO.add(LogType.WARNING, user.getName() + " ha llamado al servlet Remove con parámetros incorrectos", 
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
