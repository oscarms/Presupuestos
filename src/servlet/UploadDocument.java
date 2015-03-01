package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import budget.Annotation;
import budget.Document;
import budget.User;
import storage.FileLocalStorage;
import dao.BudgetDAO;
import dao.FileDAO;
import dao.FileType;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class UploadDocument
 * 
 * Loads the document and adds it to the budget
 */
@WebServlet("/UploadDocument")
@MultipartConfig(fileSizeThreshold=1024*1024*2,
				 maxFileSize=50*1024*1024)
public class UploadDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadDocument() {
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
		 
		User user = (User)request.getSession(true).getAttribute("user");
		PrintWriter out = response.getWriter();
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		int documentId;
		String budgetId; // get from form
		String storeAs; // name to be stored in storage
		FileDAO fileDAO = new FileLocalStorage();
		String fileName;
		
		// Filter ViewBudget checks if the user has permissions, does not verify the file. If not file, will throw exception
		
		// retrieve budgetId
		budgetId = request.getParameter("budgetId");
		
		// retrieve file data and check it
		Part part = request.getPart("documentToUpload");
		fileName = extractFileName(part);
		
		// any extension or mime
		
		// size checked by servlet annotation

		// load into database
		documentId = budgetDAO.create(budgetId, new Document(fileName, budgetId));
		if (documentId < 1) {
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" no ha podido cargar el documento " + fileName, System.currentTimeMillis());
		} else {
			// create file name
			storeAs = budgetId + "_" + documentId;
			// add to storage
			if( !fileDAO.create(FileType.DOCUMENT, part, storeAs ) ) {
				budgetDAO.removeDocument(budgetId, documentId);
				fileDAO.delete(FileType.DOCUMENT, storeAs );
				out.println("Se ha producido un error");
				out.flush();
				logDAO.add(LogType.ERROR, user.getName() + 
						" no ha podido guardar el documento " + fileName, System.currentTimeMillis());
			} else {
				budgetDAO.create(budgetId, new Annotation(System.currentTimeMillis(), "Se ha cargado el documento: " + fileName));
				out.println("Completado");
				out.flush();
				logDAO.add(LogType.ACTION, user.getName() + 
						" ha guardado el documento " + fileName + " del presupuesto " + budgetId, 
						System.currentTimeMillis());
			}

		}
		out.close();
	}
	
	
    /**
     * Extracts file name from HTTP header content-disposition
     * @param part Attached file
     * @return empty string
     */
    private String extractFileName(Part part) {
    	// from http://www.codejava.net/java-ee/servlet/java-file-upload-example-with-servlet-30-api
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }
    
}
