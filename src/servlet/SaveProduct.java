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
 * Servlet implementation class SaveProduct
 * 
 * Creates or updates a product
 */
@WebServlet("/SaveProduct")
@MultipartConfig
public class SaveProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveProduct() {
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
		boolean isNew = (request.getParameter("isNew") != null && request.getParameter("isNew").equals("1"));
		Integer productId;
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		Float costPrice;
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
			costPrice = Float.parseFloat(request.getParameter("costPrice"));
		} catch (NumberFormatException e) {
			costPrice = (float) 0;
		}
				
		// user permissions already checked by filter Administration
		
		// check parameters: needs productId and name
		if (productId != null && name != null && name.length() > 0) {
			if (description == null)
				description = "";
			
			// create or update product
			if ( isNew ? productDAO.create(productId, name, description, costPrice) : productDAO.update(productId, name, description, costPrice) ) {
				message = (isNew ? 2 : 1);
				logDAO.add(LogType.ACTION, user.getName() + " ha creado o modificado el producto " + productId, 
						System.currentTimeMillis());
				if (isNew) {
					// add the product as discontinued
					
					// trunk hours, minutes and seconds from current time
					long date = System.currentTimeMillis();
					try {
						SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						date = df.parse(df.format(date)).getTime();
					} catch (ParseException e) { }
					productDAO.create(productId, -1, date);
				}

			} else {
				// error in operation
				message = (isNew ? 3 : 6);
				logDAO.add(LogType.ERROR, user.getName() + " ha intentado crear o editar un producto sin éxito", 
						System.currentTimeMillis());
			}
			
		} else {
			// error in parameters
			message = 6;
			logDAO.add(LogType.WARNING, user.getName() + " ha intentado crear o editar un producto con los parámetros incorrectos", 
					System.currentTimeMillis());
		}
		
		// return product.jsp with message
		response.sendRedirect("product.jsp?productId=" + productId + "&message=" + message);
		
	}

}
