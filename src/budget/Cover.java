/**
 * Budget application specific objects
 */
package budget;

import java.io.File;

import storage.FileLocalStorage;
import dao.FileDAO;
import dao.FileType;

/**
 * This class represents a cover available
 * to be selected as the cover of the budget
 * when it is printed.
 * It provides methods to get the cover image
 * using classes in package dao and storage.
 * 
 * @author oscar
 */
public class Cover {

	/*
	 * Attributes
	 */
	private int id;
	
	/**
	 * Constructor to be used when retrieving from DB
	 *
	 * @param coverId The ID of the cover
	 */
	public Cover(int coverId) {
		this.id = coverId;
	}
	
	/**
	 * Constructor to be used when creating cover
	 */
	public Cover() {
		this(-1);
	}
	
	/**
	 * Returns id attribute
	 *
	 * @return The ID of the cover
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns a File with the image of the cover
	 *
	 * @return A file with the image of the cover
	 */
	public File getImage() {
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.get(FileType.COVER, this.getId() + ".png");
		if (file == null)
			file = fileDAO.get(FileType.COVER, "default.png");
		return file;
	}
	
	/**
	 * Returns a File with a reduced version of
	 * the image of the cover
	 *
	 * @return A file with a reduced version of the image of the cover
	 */
	public File getMiniImage() {
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.get(FileType.MINICOVER, this.getId() + ".png");
		if (file == null)
			file = fileDAO.get(FileType.MINICOVER, "default.png");
		return file;
	}

}
