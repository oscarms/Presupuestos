package servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pdf.PdfBox;
import budget.Attachment;
import budget.Budget;
import budget.Client;
import budget.Document;
import budget.Permission;
import budget.User;
import dao.BudgetDAO;
import dao.LogDAO;
import dao.LogType;
import dao.ProductDAO;
import dao.UserDAO;
import database.BudgetDB;
import database.LogDB;
import database.ProductDB;
import database.UserDB;

/**
 * Servlet implementation class Download
 * 
 * Gives a response with the requested file:
 * A document, attachment, PDF of a budget,
 * product / client / cover miniimage.
 * 
 * Checks if the user has permissions to view
 * a budget (document / attachment / PDF of budget),
 * or view a client (client miniimage)
 */
@WebServlet("/Download")
@MultipartConfig
public class Download extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Download() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LogDAO logDAO = new LogDB();
		User user = (User)request.getSession(true).getAttribute("user");
		File file;
		String fileName;
		
		// get parameters budgetId, attachmentId, documentId, productId, clientId, coverId
		String budgetId;
		Integer attachmentId;
		Integer documentId;
		Integer productId;
		Integer clientId;
		Integer coverId;
		
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
			productId = Integer.parseInt(request.getParameter("productImage"));
		} catch (NumberFormatException e) {
			productId = null;
		}
		try {
			clientId = Integer.parseInt(request.getParameter("clientImage"));
		} catch (NumberFormatException e) {
			clientId = null;
		}
		try {
			coverId = Integer.parseInt(request.getParameter("coverImage"));
		} catch (NumberFormatException e) {
			coverId = null;
		}
		
		/*
		 * check permissions
		 * set file for each case
		 * set filename for each case
		 */
		
		if (budgetId != null && 
				productId == null && clientId == null && coverId == null ) {
			
			BudgetDAO budgetDAO = new BudgetDB();
			Budget budget = budgetDAO.getBudget(budgetId);
			
			// check if the user can view the budget
			if (!((user.equals(budget.getSalesperson()) || user.hasPermission(Permission.ALLCLIENTS)) 
					&& ( (budget.isOffer() && user.hasPermission(Permission.VIEWOFFERS) )
					|| !budget.isOffer() ) ) ) {
				logDAO.add(LogType.WARNING, user.getName() + 
						" ha intentado descargar de un presupuesto sin permisos", 
						System.currentTimeMillis());
				response.sendRedirect("Logout");
				return;
			}
			// at this point, the user has permissions
			
			if (attachmentId != null && documentId == null) {
				// case attachment of a budget
				Attachment attachment = budgetDAO.getAttachment(budgetId, attachmentId);
				file = attachment.getAttachment();
				fileName = attachment.getName();
			} else if (attachmentId == null && documentId != null) {
				// case document of a budget
				Document document = budgetDAO.getDocument(budgetId, documentId);
				file = document.getDocument();
				fileName = document.getName();
				
			} else if (attachmentId == null && documentId == null) {
				// case PDF of a budget
				fileName = budgetId + ".pdf";
				file = PdfBox.createPdf(budget);
								
			} else {
				// incorrect parameters
				logDAO.add(LogType.WARNING, user.getName() + 
						" ha intentado descargar de un presupuesto con los parámetros incorrectos", 
						System.currentTimeMillis());
				response.sendRedirect("Logout");
				return;
			}

		} else if (budgetId == null && attachmentId == null && documentId == null && 
				productId != null && clientId == null && coverId == null ) {
			ProductDAO productDAO = new ProductDB();
			// case mini image of product
			file = productDAO.getProduct(productId).getMiniImage();
			fileName = "Product" + productId + ".png";
			
		} else if (budgetId == null && attachmentId == null && documentId == null && 
				productId == null && clientId != null && coverId == null ) {
			
			UserDAO userDAO = new UserDB();
			Client client = userDAO.getClient(clientId);
			// check if the user can edit the client
			if (!user.hasPermission(Permission.ALLCLIENTS) && !user.equals(client.getSalesperson())) {
				// user cannot edit the client
				logDAO.add(LogType.WARNING, user.getName() + 
						" ha intentado descargar de un cliente sin permisos", 
						System.currentTimeMillis());
				response.sendRedirect("Logout");
				return;
			}
			// at this point, the user has permissions
			
			// case mini image of client
			file = client.getMiniImage();
			fileName = "Client" + clientId + ".png";
			
		} else if (budgetId == null && attachmentId == null && documentId == null && 
				productId == null && clientId == null && coverId != null ) {
			// case mini image of cover
			BudgetDAO budgetDAO = new BudgetDB();
			file = budgetDAO.getCover(coverId).getMiniImage();
			fileName = "cover" + coverId + ".png";
			
		} else {
			// incorrect parameters
			logDAO.add(LogType.WARNING, user.getName() + 
					" ha intentado descargar con los parámetros incorrectos", 
					System.currentTimeMillis());
			response.sendRedirect("Logout");
			return;
		}

		/*
		 * file contains the requested file
		 * fileName contains the file filename
		 * 
		 * now return the file to the user
		 */
		
		// from www.codemiles.com/servlets-jsp/jsp-to-download-file-t17.html
		BufferedInputStream buf=null;
		ServletOutputStream myOut=null;

		try{
			if (file == null)
				throw new IOException("el fichero a descargar es null");
			
			myOut = response.getOutputStream( );
	
			//set response headers
			response.setContentType("application/force-download");
				// stackoverflow.com/questions/6520231/how-to-force-browser-to-download-file
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.addHeader("Content-Disposition","attachment; filename="+fileName );
			response.setContentLength( (int) file.length( ) );
			FileInputStream input = new FileInputStream(file);
			buf = new BufferedInputStream(input);
			int readBytes = 0;
	
			//read from the file; write to the ServletOutputStream
			while((readBytes = buf.read( )) != -1)
				myOut.write(readBytes);
			
			logDAO.add(LogType.ACTION, user.getName() + " ha descargado " + fileName, System.currentTimeMillis());
		} catch (IOException e){
			logDAO.add(LogType.ERROR, user.getName() + 
					" ha intentado descargar pero ha habido un error", 
					System.currentTimeMillis());
			throw new ServletException(e);
		} finally {
			//close the input/output streams
			if (myOut != null)
				myOut.close( );
			if (buf != null)
				buf.close( );
	     }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
