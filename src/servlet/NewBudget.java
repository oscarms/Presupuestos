package servlet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import storage.FileLocalStorage;
import budget.Annotation;
import budget.Attachment;
import budget.Budget;
import budget.Document;
import budget.Permission;
import budget.Section;
import budget.SectionProduct;
import budget.User;
import dao.BudgetDAO;
import dao.FileDAO;
import dao.FileType;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class NewBudget
 * 
 * Creates a new budget and redirects for
 * editing the new budget
 */
@WebServlet("/NewBudget")
public class NewBudget extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewBudget() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Parse parameters budgetId, action (template/modify), isOffer(1/0) and get user
		String budgetId = request.getParameter("budgetId");
		String action = request.getParameter("action");
		boolean isOffer = request.getParameter("isOffer") != null && request.getParameter("isOffer").equals("1");
		User user = (User)request.getSession(true).getAttribute("user");
		Budget budget = null;
		BudgetDAO budgetDAO = new BudgetDB();
		LogDAO logDAO = new LogDB();
		List<String> errors = new LinkedList<String>();

		/*
		Editing an existing budget / offer:					budgetId != null && action == null
		Creting a new empty budget: 						budgetId == null && action == null && !isOffer
		Creting a new empty offer:  						budgetId == null && action == null && isOffer
		Creating a new budget reusing another budget/offer:	budgetId != null && action.equals("template") && !isOffer
		Creating a new offer reusing another budget/offer:	budgetId != null && action.equals("template") && isOffer
		Creating a new budget modifying another one:		budgetId != null && action.equals("modify") && !isOffer
		Creating a new offer modifying another one:			budgetId != null && action.equals("modify") && isOffer
		Error in parameters:								budgetId == null && action != null
		Error in budgetId:									retrieve budgetId from database failed
		Error in permissions when creating:					isOffer && !user.hasPermission(Permission.CREATEOFFER)
		Error in permissions when editing: 					User is not the author or is not an offer waiting to be signed:
			!user.equals(budget.getSalesperson()) && 
			!(user.hasPermission(Permission.ADMINISTRATE) && budget.isOffer() && budget.getCreationDate() > 0)
		*/

		if (budgetId != null && action == null) {
			// redirect to createbudget.jsp
			response.sendRedirect("createbudget.jsp?budgetId=" + budgetId);
			return;
		} else if (budgetId == null && action != null) {
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado acceder a NewBudget con parámetros incorrectos", 
					System.currentTimeMillis());
			response.sendRedirect("Logout");
			return;
		} else if (isOffer && !user.hasPermission(Permission.CREATEOFFERS)) {
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado crear una oferta sin permisos", System.currentTimeMillis());
			response.sendRedirect("Logout");
			return;
		} else if (budgetId == null && action == null) {
			// creating a new empty budget/offer
			budgetId = budgetDAO.createBudget(user, isOffer);
			logDAO.add(LogType.ACTION, user.getName() + " ha creado el presupuesto " + budgetId, 
					System.currentTimeMillis());
			// redirect to createbudget.jsp
			response.sendRedirect("createbudget.jsp?budgetId=" + budgetId);
			return;
			
		} else if (budgetId != null && action.equals("template")) {
			// creating a new budget/offer reusing another budget/offer
			
			budget = budgetDAO.getBudget(budgetId);
			budgetId = budgetDAO.createBudget(user, isOffer);
			
			// add attributes related to the price
			budgetDAO.setTaxRate(budgetId, budget.getTaxRate());
			budgetDAO.setGlobalTotal(budgetId, budget.hasGlobalTotal());
			
			// add sections and products in the same quantity and, if it is an offer, discounts
			int sectionId;
			for (Section section : budgetDAO.getSections( budget.getBudgetId() )) {
				sectionId = budgetDAO.addSection(budgetId,section.getName());
				for (SectionProduct product : section.getProducts()) {
					if (budgetDAO.addProduct(budgetId, sectionId, product.getProductId() )) {
						budgetDAO.setQuantity(budgetId, sectionId, product.getProductId(),
								product.getQuantity());
						if (isOffer && budget.isOffer()) {
							budgetDAO.setDiscounts(budgetId, sectionId, product.getProductId(),
									product.getDiscount1(), product.getDiscount2(), product.getDiscount3());
						}
					} else {
						errors.add("El producto " + product.getProductId() + ": " + product.getName() 
								+ " no está disponible. Añada un sustituto");
					}
				}
			}
			// errors contains a list of messages of products that cannot be added
			
			logDAO.add(LogType.ACTION, user.getName() + " ha creado el presupuesto " + budgetId, 
					System.currentTimeMillis());
			// redirect to createbudget.jsp
			request.setAttribute("errors", ((List<String>)errors).toArray(new String[errors.size()]));
			request.getServletContext().getRequestDispatcher("/createbudget.jsp?budgetId=" + budgetId).forward(request, response);
			return;
			
		} else if (budgetId != null && action.equals("modify")) {
			// modify a budget by creating another one equal reusing another budget/offer
			
			budget = budgetDAO.getBudget(budgetId);
			Budget[] family = budget.getFamily();
			budgetId = budgetDAO.createBudget(user, family[family.length-1].getBudgetId(), isOffer);
			
			// add attributes (without dates) and cover
			budgetDAO.setCover(budgetId, budget.getCoverId());
			budgetDAO.setClient(budgetId, budget.getClient().getClientId());
			budgetDAO.setConstructionRef(budgetId, budget.getConstructionRef());
			budgetDAO.setNote(budgetId, budget.getNote());
			budgetDAO.setTaxRate(budgetId, budget.getTaxRate());
			budgetDAO.setGlobalTotal(budgetId, budget.hasGlobalTotal());
			
			// add sections and products in the same quantity and, if it is an offer, discounts
			int sectionId;
			for (Section section : budgetDAO.getSections( budget.getBudgetId() )) {
				sectionId = budgetDAO.addSection(budgetId,section.getName());
				for (SectionProduct product : section.getProducts()) {
					if (budgetDAO.addProduct(budgetId, sectionId, product.getProductId() )) {
						budgetDAO.setQuantity(budgetId, sectionId, product.getProductId(),
								product.getQuantity());
						if (isOffer && budget.isOffer()) {
							budgetDAO.setDiscounts(budgetId, sectionId, product.getProductId(),
									product.getDiscount1(), product.getDiscount2(), product.getDiscount3());
						}
					} else {
						errors.add("El producto " + product.getProductId() + ": " + product.getName() 
								+ " no está disponible. Añada un sustituto");
					}
				}
			}
			// errors contains a list of messages of products that cannot be added
			
			// duplicate notes
			for (Annotation annotation : budgetDAO.getAnnotations(budget.getBudgetId()) ) {
				budgetDAO.create(budgetId, annotation);
			}
			
			// add note to the old budget
			String annotationText = "";
			if (!isOffer && !budget.isOffer())
				annotationText = "Se ha modificado el presupuesto en el "+ budgetId;
			else if (isOffer && budget.isOffer())
				annotationText = "Se ha modificado la oferta en la "+ budgetId;
			else if (isOffer && !budget.isOffer())
				annotationText = "Se ha creado la oferta " + budgetId + " a partir de este presupuesto";
			else if (!isOffer && budget.isOffer())
				annotationText = "Se ha creado el presupuesto " + budgetId + " a partir de esta oferta";
			budgetDAO.create(budget.getBudgetId(), new Annotation(System.currentTimeMillis(), annotationText ) );
			// add note to the new budget
			if (!isOffer && !budget.isOffer())
				annotationText = "Este presupuesto es una modificación de " + budget.getBudgetId();
			else if (isOffer && budget.isOffer())
				annotationText = "Esta oferta es una modificación de " + budget.getBudgetId();
			else if (isOffer && !budget.isOffer())
				annotationText = "Se ha creado esta oferta a partir del presupuesto " + budget.getBudgetId();
			else if (!isOffer && budget.isOffer())
				annotationText = "Se ha creado este presupuesto a partir de la oferta " + budget.getBudgetId();
			budgetDAO.create(budgetId, new Annotation(System.currentTimeMillis(), annotationText ) );
			
			FileDAO fileDAO = new FileLocalStorage();
			// duplicate documents
			int documentId;
			for (Document document : budgetDAO.getDocuments(budget.getBudgetId()) ) {
				documentId = budgetDAO.create(budgetId, new Document(document.getName(), budgetId));
				if (documentId < 1)
					errors.add("El documento " + document.getName() + " no se ha añadido");
				else {
					// add to storage
					if( !fileDAO.create(FileType.DOCUMENT, document.getDocument(), budgetId + "_" + documentId ) ) {
						budgetDAO.removeDocument(budgetId, documentId);
						fileDAO.delete(FileType.DOCUMENT, budgetId + "_" + documentId );
						errors.add("El documento " + document.getName() + " no se ha añadido");
					}
				}
			}
			
			// duplicate attachments
			int attachmentId;
			for (Attachment attachment : budgetDAO.getAttachments(budget.getBudgetId()) ) {
				attachmentId = budgetDAO.create(budgetId, new Attachment(attachment.getName(), budgetId));
				if (attachmentId < 1)
					errors.add("El adjunto " + attachment.getName() + " no se ha añadido");
				else {
					// add to storage
					if( !fileDAO.create(FileType.ATTACHMENT, attachment.getAttachment(), budgetId + "_" + attachmentId ) ) {
						budgetDAO.removeAttachment(budgetId, attachmentId);
						fileDAO.delete(FileType.ATTACHMENT, budgetId + "_" + attachmentId );
						errors.add("El adjunto " + attachment.getName() + " no se ha añadido");
					}
				}
			}
			logDAO.add(LogType.ACTION, user.getName() + " ha creado el presupuesto " + budgetId, 
					System.currentTimeMillis());
			// redirect to createbudget.jsp
			request.setAttribute("errors", ((List<String>)errors).toArray(new String[errors.size()]));
			request.getServletContext().getRequestDispatcher("/createbudget.jsp?budgetId=" + budgetId).forward(request, response);
			return;
			
		} else {
			logDAO.add(LogType.WARNING, user.getName() + " ha provocado un error de opciones en createbudget.jsp", 
					System.currentTimeMillis());
			response.sendRedirect("Logout");
			return;
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
