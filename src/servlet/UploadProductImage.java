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

import storage.FileLocalStorage;
import budget.User;
import dao.FileDAO;
import dao.FileType;
import dao.LogDAO;
import dao.LogType;
import dao.ProductDAO;
import database.LogDB;
import database.ProductDB;

/**
 * Servlet implementation class UploadProductImage
 * 
 * Loads the file and creates the image and miniimage
 */
@WebServlet("/UploadProductImage")
@MultipartConfig(fileSizeThreshold=1024*1024*2,
				 maxFileSize=50*1024*1024)
public class UploadProductImage extends HttpServlet {
	
	private static int MINIWIDTH = 600;
	private static int MINIHEIGHT = 600;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadProductImage() {
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
		ProductDAO productDAO = new ProductDB();
		Integer productId; // get from form
		String storeAs; // name to be stored in storage
		FileDAO fileDAO = new FileLocalStorage();
		Part part;
		
		// Filter Administration checks if the user has permissions, does not verify the parameters
		
		// retrieve parameters
		try {
			productId = Integer.parseInt(request.getParameter("productId"));
		
			// retrieve file data and check it
			part = request.getPart("imageToUpload");
		
			// check mime
			if (!part.getContentType().contains("image")) {
				out.println("El fichero no es una imagen");
				out.flush();
				out.close();
				return;
			}
			// size checked by servlet annotation
		} catch (Exception e) {
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" ha entrado a UploadProductImage sin parámetros", System.currentTimeMillis());
			out.close();
			return;
		}
		
		if (productId == null || productDAO.getProduct(productId) == null) {
			// the specified product not exists
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" ha entrado a UploadProductImage sin producto, o no existe", System.currentTimeMillis());
			out.close();
			return;
		}
		
		// create file name
		storeAs = productId + ".png";
		
		// convert image to PNG and store image
		try {
			String oldImageName = Long.toString(System.currentTimeMillis());
			File oldImage = fileDAO.create(FileType.TEMPORARY, oldImageName);
			part.write(oldImage.getPath());
			File newImage = fileDAO.create(FileType.PRODUCTIMAGE, storeAs);
			File newMiniImage = fileDAO.create(FileType.PRODUCTMINIIMAGE, storeAs);
			BufferedImage oldBf = ImageIO.read(oldImage);
			// store image
			ImageIO.write(oldBf, "png", newImage);
			
			// create mini image by cropping and resizing image (Code from http://www.mkyong.com/java/how-to-resize-an-image-in-java/)
			BufferedImage newBf = ImageIO.read(newImage);
			
			// crop image 
			if (newBf.getWidth() > newBf.getHeight())
				newBf = newBf.getSubimage( (newBf.getWidth()-newBf.getHeight())/2 , 0 , newBf.getHeight() , newBf.getHeight() );
			else
				newBf = newBf.getSubimage( 0 , (newBf.getHeight()-newBf.getWidth())/2 , newBf.getWidth() , newBf.getWidth() );
			
			// resize image
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
			// store mini image
			ImageIO.write(resizedImage, "png", newMiniImage);
			logDAO.add(LogType.ACTION, user.getName() + " ha cargado la imagen de producto " + storeAs, System.currentTimeMillis());
			fileDAO.delete(FileType.TEMPORARY, oldImageName);
			out.println("Completado");
			out.flush();
		} catch (Exception e) {
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" no ha podido convertir o guardar la imagen de producto", System.currentTimeMillis());
			// remove new image and miniimage
			fileDAO.delete(FileType.PRODUCTIMAGE, storeAs);
			fileDAO.delete(FileType.PRODUCTMINIIMAGE, storeAs);
			out.close();
			return;
		}
		out.close();
	}

}
