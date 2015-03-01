package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import storage.FileLocalStorage;
import dao.BudgetDAO;
import dao.FileDAO;
import dao.FileType;
import dao.LogDAO;
import dao.LogType;
import database.BudgetDB;
import database.LogDB;

/**
 * Servlet implementation class RemoveCover
 * 
 * Removes the cover from the database and its files
 */
@WebServlet("/RemoveCover")
@MultipartConfig
public class RemoveCover extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveCover() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// retrieve parameters
		int coverId = Integer.parseInt(request.getParameter("coverId"));
		
		LogDAO logDAO = new LogDB();
		BudgetDAO budgetDAO = new BudgetDB();
		FileDAO fileDAO = new FileLocalStorage();
		
		// remove from database
		if (budgetDAO.removeCover(coverId)) {
			
			// remove files
			if ( fileDAO.delete(FileType.MINICOVER, coverId + ".png")
					&& fileDAO.delete(FileType.COVER, coverId + ".png") ) {
				// done
				logDAO.add(LogType.ACTION, "Se ha eliminado la portada " + coverId, 
						System.currentTimeMillis());
			} else {
				// error
				logDAO.add(LogType.ERROR, "No se han podido eliminar los ficheros de la portada " + coverId, 
						System.currentTimeMillis());
			}

		} else {
			// error
			logDAO.add(LogType.ERROR, "No se ha podido eliminar de la base de datos la portada " + coverId, 
					System.currentTimeMillis());
		}
		
		// redirect to maintenance.jsp
		response.sendRedirect("maintenance.jsp");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

}
