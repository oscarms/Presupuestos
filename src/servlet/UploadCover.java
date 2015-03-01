package servlet;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import budget.Cover;
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
 * Servlet implementation class UploadCover
 * 
 * Loads the cover, creates the image and miniimage
 * and sets the budget to use it
 */
@WebServlet("/UploadCover")
@MultipartConfig(fileSizeThreshold=1024*1024*2,
				 maxFileSize=50*1024*1024)
public class UploadCover extends HttpServlet {
	
	private static int MINIWIDTH = 140;
	private static int MINIHEIGHT = 198;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadCover() {
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
		String budgetId; // get from form
		String storeAs; // name to be stored in storage
		FileDAO fileDAO = new FileLocalStorage();
		
		// Filter ViewBudget checks if the user has permissions, does not verify the file. If not file, will throw exception
		
		// retrieve budgetId
		budgetId = request.getParameter("budgetId");
		
		// retrieve file data and check it
		Part part = request.getPart("coverToUpload");
		
		// check mime
		if (!part.getContentType().contains("image")) {
			out.println("El fichero no es una imagen");
			out.flush();
			out.close();
			return;
		}
		// size checked by servlet annotation
		else {
			// load cover into database
			int coverId = budgetDAO.create(new Cover());
			if (coverId < 1) {
				out.println("Se ha producido un error");
				out.flush();
				logDAO.add(LogType.ERROR, user.getName() + 
						" no ha podido cargar la portada", System.currentTimeMillis());
			} else {
				// create file name
				storeAs = coverId + ".png";
				// convert image to PNG and store cover
				try {
					File oldImage = fileDAO.create(FileType.TEMPORARY, Long.toString(System.currentTimeMillis()));
					part.write(oldImage.getPath());
					File newImage = fileDAO.create(FileType.COVER, storeAs);
					File newMiniImage = fileDAO.create(FileType.MINICOVER, storeAs);
					BufferedImage oldBf = ImageIO.read(oldImage);
					// store cover
					ImageIO.write(oldBf, "png", newImage);
					// create mini cover by resizing cover (Code from http://www.mkyong.com/java/how-to-resize-an-image-in-java/)
					BufferedImage newBf = ImageIO.read(newImage);
					BufferedImage resizedImage = new BufferedImage(MINIWIDTH, MINIHEIGHT, newBf.getType());
					Graphics2D g = resizedImage.createGraphics();
					g.drawImage(newBf, 0, 0, MINIWIDTH, MINIHEIGHT, null);
					g.dispose();	
					g.setComposite(AlphaComposite.Src);
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
					// store mini cover
					ImageIO.write(resizedImage, "png", newMiniImage);
					logDAO.add(LogType.ACTION, user.getName() + " ha cargado la portada " + storeAs, System.currentTimeMillis());
					oldImage.delete();
				} catch (Exception e) {
					out.println("Se ha producido un error");
					out.flush();
					logDAO.add(LogType.ERROR, user.getName() + 
							" no ha podido convertir o guardar la portada", System.currentTimeMillis());
					// remove new cover and minicover
					budgetDAO.removeCover(coverId);
					fileDAO.delete(FileType.COVER, storeAs);
					fileDAO.delete(FileType.MINICOVER, storeAs);
					out.close();
					return;
				}
				// set cover in budget
				if (budgetDAO.setCover(budgetId, coverId)) {
					out.println("Completado");
					out.flush();
				}
				else {
					out.println("Portada no asignada al presupuesto");
					out.flush();
					logDAO.add(LogType.ERROR, user.getName() + 
							" ha cargado la portada pero no se ha establecido en el presupuesto", 
							System.currentTimeMillis());
				}
				
			}
		}
		out.close();

	}

}
