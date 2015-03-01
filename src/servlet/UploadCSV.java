package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;

import storage.FileLocalStorage;
import budget.Product;
import budget.User;
import dao.FileDAO;
import dao.FileType;
import dao.LogDAO;
import dao.LogType;
import dao.ProductDAO;
import database.LogDB;
import database.ProductDB;

/**
 * Servlet implementation class UploadCSV
 * 
 * Loads the CSV, checks it and updates
 * or creates the listed products
 */
@WebServlet("/UploadCSV")
@MultipartConfig(fileSizeThreshold=1024*1024*2,
				 maxFileSize=50*1024*1024)
public class UploadCSV extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static String FIRSTLINE = "id;name;description;price;costprice";
	private static String SEPARATOR = ";";   
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadCSV() {
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
		FileDAO fileDAO = new FileLocalStorage();
		Part part;
		
		// retrieve parameters
		try {
			// retrieve file data and check it
			part = request.getPart("csvToUpload");
		
			// check mime
			if (!part.getContentType().contains("text")) {
				out.println("El fichero no contiene texto plano");
				out.flush();
				out.close();
				return;
			}
			// size checked by servlet annotation
		} catch (Exception e) {
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" ha entrado a UploadCSV sin fichero", System.currentTimeMillis());
			out.close();
			return;
		}
		
		// Administration permissions checked by filter Administration
		
		// create temporary file
		String fileName = Long.toString(System.currentTimeMillis());
		File file = fileDAO.create(FileType.TEMPORARY, fileName);
		part.write(file.getPath());

		// check file and update or create products
		if (csvProductsUpdate(file)) {
			// completed
			logDAO.add(LogType.ACTION, user.getName() + " ha actualizado los productos con un CSV", System.currentTimeMillis());
			out.println("Completado");
			out.flush();
		} else {
			// error
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" no ha podido completar la actualizacion de productos mediante CSV", 
					System.currentTimeMillis());
		}
		fileDAO.delete(FileType.TEMPORARY, fileName);
	}

	private boolean csvProductsUpdate(File file) {
		ProductDAO productDAO = new ProductDB();
		try {
			List<String> lines = FileUtils.readLines(file);
			// check file
			boolean firstLine = true;
			List<Object[]> csvData = new ArrayList<Object[]>();
			for (String line : lines) {
				if (firstLine) {
					if (!line.equals(FIRSTLINE))
						return false;
					firstLine = false;
				} else {
					// add the values to a new structure after parsing the csv
					// regex from http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
					String[] productLine = line.split(SEPARATOR + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
					Object[] productData = new Object[5];
					
					for (int i = 0; i < productLine.length; i++ ) {
						// remove quotes from strings
						if (((String)productLine[i]) != null && ((String)productLine[i]).length() > 0 && 
								((String)productLine[i]).startsWith("\"") && ((String)productLine[i]).endsWith("\"")) {
							productLine[i] = ((String)productLine[i]).substring(1,((String)productLine[i]).length()-1);
						}	
							
						// check data
						switch (i) {
							case 0: productData[0] = Integer.parseInt((String)productLine[0]); // id
									break;
							case 1: productData[1] = productLine[1]; // name
									break;
							case 2: productData[2] = productLine[2]; // description
									break;
							case 3: 
									if ( ((String)productLine[3]).length() < 1) {
										productData[3] = null;
									} else {
										productData[3] = Float.parseFloat(((String)productLine[3]).replace(",", ".")); // price
									}
									break;
							case 4:
									if ( ((String)productLine[4]).length() < 1) {
										productData[4] = Float.valueOf(0);
									} else {
										productData[4] = Float.parseFloat(((String)productLine[4]).replace(",", ".")); // costPrice
									}
									break;
							default: break;
						}
						
					}

					for (int i = productLine.length; i < 5; i++) {
						
						// last fields not present in the line
						switch (i) {
						case 0: productData[0] = -1; // id
								break;
						case 1: productData[1] = ""; // name
								break;
						case 2: productData[2] = ""; // description
								break;
						case 3: productData[3] = null;
								break;
						case 4: productData[4] = Float.valueOf(0);
								break;
						default: break;
						}
						
					}
					
					// data is correct, add to being processed after parsing all products
					if ((Integer)productData[0] >= 0 && !csvData.add(productData))
						return false;
					
				} // else
			} // for lines
			
			lines.clear();
			lines = null;
			
			// create or update products
			for ( Object[] item : csvData) {
				
				int productId = (Integer)item[0];
				String name = (String)item[1];
				String description = (String)item[2];
				float costPrice = (Float)item[4];
				Float price = (Float)item[3];
				
				if (name == null)
					name = "";
				if (description == null)
					description = "";
				if (price == null)
					price = -1f;
				
				Product oldProduct = productDAO.getProduct(productId);
				
				if (oldProduct == null) {
					if (name == null || name.length() < 1)
						name = "?";
					// insert new product
					productDAO.create(productId, name, description, costPrice);
					
					// trunk hours, minutes and seconds from current day
					long currentDate = System.currentTimeMillis();
					try {
						SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						currentDate = df.parse(df.format(currentDate)).getTime();
					} catch (ParseException e) { }
					
					// insert new price
					productDAO.create(productId, price, currentDate);
					
				} else {
					// update product with the specified changes
					if (name == null || name.length() < 1)
						name = oldProduct.getName();
					if (description == null || description.length() < 1)
						description = oldProduct.getDescription();
					if (costPrice == 0)
						costPrice = oldProduct.getCostPrice();
					productDAO.update(productId, name, description, costPrice);
					
					// check price
					// trunk hours, minutes and seconds from next day
					long date = System.currentTimeMillis() + 86400000;
					try {
						SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						date = df.parse(df.format(date)).getTime();
					} catch (ParseException e) { }
					
					// update price if it is different from the price the next day.
					// Also apply the next day
					if (price != null && price != oldProduct.getPrice(date+1000)) {
						productDAO.create(productId, price, date);
					}
					
				}
			}

		} catch (IOException e) {
			return false;
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
