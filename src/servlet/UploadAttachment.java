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
import budget.Attachment;
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
 * Servlet implementation class UploadAttachment
 * 
 * Loads the attachment and adds it to the budget
 */
@WebServlet("/UploadAttachment")
@MultipartConfig(fileSizeThreshold=1024*1024*2,
				 maxFileSize=50*1024*1024)
public class UploadAttachment extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadAttachment() {
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
		int attachmentId;
		String budgetId; // get from form
		String storeAs; // name to be stored in storage
		FileDAO fileDAO = new FileLocalStorage();
		String fileName;
		
		// Filter ViewBudget checks if the user has permissions, does not verify the file. If not file, will throw exception
		
		// retrieve budgetId
		budgetId = request.getParameter("budgetId");
		
		// retrieve file data and check it
		Part part = request.getPart("attachmentToUpload");
		fileName = extractFileName(part);
		
		// check extension
		if (fileName.toLowerCase().lastIndexOf(".pdf") != fileName.length()-4) {
			out.println("El fichero no es un PDF");
			out.flush();
		}
		// check mime
		else if (!part.getContentType().equals("application/pdf")) {
			out.println("El fichero no es un PDF válido");
			out.flush();
		}
		// size checked by servlet annotation
		else {
			// load into database
			attachmentId = budgetDAO.create(budgetId, new Attachment(fileName, budgetId));
			if (attachmentId < 1) {
				out.println("Se ha producido un error");
				out.flush();
				logDAO.add(LogType.ERROR, user.getName() + 
						" no ha podido cargar el adjunto " + fileName, System.currentTimeMillis());
			} else {
				// create file name
				storeAs = budgetId + "_" + attachmentId;
				// add to storage
				if( !fileDAO.create(FileType.ATTACHMENT, part, storeAs ) ) {
					budgetDAO.removeAttachment(budgetId, attachmentId);
					fileDAO.delete(FileType.ATTACHMENT, storeAs );
					out.println("Se ha producido un error");
					out.flush();
					logDAO.add(LogType.ERROR, user.getName() + 
							" no ha podido guardar el adjunto " + fileName, System.currentTimeMillis());
				} else {
					budgetDAO.create(budgetId, new Annotation(System.currentTimeMillis(), "Se ha cargado el adjunto: " + fileName));
					out.println("Completado");
					out.flush();
					logDAO.add(LogType.ACTION, user.getName() + 
							" ha guardado el adjunto " + fileName + " del presupuesto " + budgetId, 
							System.currentTimeMillis());
				}
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
