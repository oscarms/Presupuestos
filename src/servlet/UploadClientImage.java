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
import budget.Client;
import budget.Permission;
import budget.User;
import dao.FileType;
import dao.LogType;
import dao.UserDAO;
import dao.FileDAO;
import dao.LogDAO;
import database.UserDB;
import database.LogDB;

/**
 * Servlet implementation class UploadClientImage
 * 
 * Loads the file and creates the image and miniimage
 */
@WebServlet("/UploadClientImage")
@MultipartConfig(fileSizeThreshold=1024*1024*2,
				 maxFileSize=50*1024*1024)
public class UploadClientImage extends HttpServlet {
	
	private static int MINIWIDTH = 600;
	private static int MINIHEIGHT = 600;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadClientImage() {
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
		LogDAO logDAO = new LogDB();
		UserDAO userDAO = new UserDB();
		Integer clientId; // get from form
		String storeAs; // name to be stored in storage
		FileDAO fileDAO = new FileLocalStorage();
		Part part;
		PrintWriter out = response.getWriter();
		
		// retrieve parameters
		try {
			clientId = Integer.parseInt(request.getParameter("clientId"));
		
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
					" ha entrado a UploadClientImage sin parámetros", System.currentTimeMillis());
			out.close();
			return;
		}
		
		Client client = userDAO.getClient(clientId);
		if (clientId == null || client == null) {
			// the specified client not exists
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" ha entrado a UploadClientImage sin cliente, o no existe", 
					System.currentTimeMillis());
			out.close();
			return;
		}
				
		// check user permission to edit client
		if (!user.hasPermission(Permission.ALLCLIENTS) && !client.getSalesperson().equals(user)) {
			// the user cannot view or edit the client
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" ha entrado a UploadClientImage con un cliente que no le pertenece", 
					System.currentTimeMillis());
			out.close();
			return;
		}
		
		// create file name
		storeAs = clientId + ".png";
		
		// convert image to PNG and store image
		try {
			String oldImageName = Long.toString(System.currentTimeMillis());
			File oldImage = fileDAO.create(FileType.TEMPORARY, oldImageName);
			part.write(oldImage.getPath());
			File newImage = fileDAO.create(FileType.CLIENTIMAGE, storeAs);
			File newMiniImage = fileDAO.create(FileType.CLIENTMINIIMAGE, storeAs);
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
			logDAO.add(LogType.ACTION, user.getName() + " ha cargado la imagen de cliente " + storeAs, System.currentTimeMillis());
			fileDAO.delete(FileType.TEMPORARY, oldImageName);
			out.println("Completado");
			out.flush();
			out.close();
		} catch (Exception e) {
			out.println("Se ha producido un error");
			out.flush();
			logDAO.add(LogType.ERROR, user.getName() + 
					" no ha podido convertir o guardar la imagen de cliente", System.currentTimeMillis());
			// remove new image and miniimage
			fileDAO.delete(FileType.CLIENTIMAGE, storeAs);
			fileDAO.delete(FileType.CLIENTMINIIMAGE, storeAs);
			out.close();
			return;
		}
				
	}

}
