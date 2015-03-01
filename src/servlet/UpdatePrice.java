package servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import budget.User;
import dao.LogDAO;
import dao.LogType;
import dao.ProductDAO;
import database.LogDB;
import database.ProductDB;

/**
 * Servlet implementation class UpdatePrice
 * 
 * Creates a new price for a product
 */
@WebServlet("/UpdatePrice")
@MultipartConfig
public class UpdatePrice extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdatePrice() {
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
		Integer productId;
		Float newPrice;
		boolean discontinued = (request.getParameter("discontinued") != null && request.getParameter("discontinued").equals("on"));
		String date = request.getParameter("date");
		long dateMs;
		User user = (User)request.getSession(true).getAttribute("user");
		LogDAO logDAO = new LogDB();
		ProductDAO productDAO = new ProductDB();
		int message;
		
		try {
			productId = Integer.parseInt(request.getParameter("productId"));
		} catch (NumberFormatException e) {
			productId = null;
		}
		try {
			newPrice = Float.parseFloat(request.getParameter("newPrice"));
		} catch (NumberFormatException e) {
			newPrice = null;
		}
		
		// date is dd/mm/yyyy and it is needed in format long
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			dateMs = df.parse(date).getTime();
		} catch (ParseException e) {
			message = 7;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado crear un precio con la fecha incorrecta", 
					System.currentTimeMillis());
			response.sendRedirect("product.jsp?productId=" + productId + "&message=" + message);	
			return;
		}
				
		// user permissions already checked by filter Administration
	
		// check parameters
		if (productId != null && (newPrice != null || discontinued) ) {
			if (discontinued) {
				if (productDAO.create(productId, -1, dateMs)) {
					// created price discontinued
					message = 5;
					logDAO.add(LogType.ACTION, user.getName() + " ha descatalogado el producto " +  productId, 
							System.currentTimeMillis());
				} else {
					// error
					message = 7;
					logDAO.add(LogType.ERROR, user.getName() + " ha intentado descatalogar un producto, pero se ha producido un error", 
							System.currentTimeMillis());
				}

			} else {
				if (productDAO.create(productId, newPrice, dateMs)) {
					// created price
					message = 4;
					logDAO.add(LogType.ACTION, user.getName() + " ha creado un precio para el producto " +  productId, 
							System.currentTimeMillis());
				} else {
					// error
					message = 7;
					logDAO.add(LogType.ERROR, user.getName() + " ha intentado crear un precio, pero se ha producido un error", 
							System.currentTimeMillis());
				}			
			}

		} else {
			// error in parameters
			message = 7;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado crear un precio con parámetros incorrectos", 
					System.currentTimeMillis());
		}
		
		// return product.jsp with message	
		response.sendRedirect("product.jsp?productId=" + productId + "&message=" + message);	
		
	}

}
