/**
 * Data Access Objects
 */
package dao;

import java.io.File;

import javax.servlet.http.Part;

/**
 * This class provides the specification of the methods
 * available to load and download files to the system.
 * It is implemented in FileLocalStorage.
 * The files must be checked as valid before
 * being processed by this class.
 * 
 * @author oscar
 */
public interface FileDAO {
	
	/*
	 * No Attributes
	 */
	
	/**
	 * Stores the file in the system
	 *
	 * @param type Type of file
	 * @param file The file uploaded
	 * @param fileName The name of the file where to be stored
	 * @return True if it is stored successfully
	 */
	public boolean create(FileType type, Part file, String fileName);
	
	/**
	 * Stores the file in the system
	 *
	 * @param type Type of file
	 * @param file The file to be stored
	 * @param fileName The name of the file where to be stored
	 * @return True if the file is stored successfully
	 */
	public boolean create(FileType type, File file, String fileName);
	
	/**
	 * Creates a new file and returns it
	 * to be written
	 * 
	 * @param type Type of file
	 * @param fileName The name of the file where to be stored
	 * @return The new file
	 */
	public File create(FileType type, String fileName);
	
	/**
	 * Retrieves the file from the system
	 *
	 * @param type Type of file
	 * @param fileName The name of the file where is stored
	 * @return The file that matches the name and the type
	 */
	public File get(FileType type, String fileName);
	
	/**
	 * Removes the file from the system
	 *
	 * @param type Type of file
	 * @param fileName The name of the file where to be stored
	 * @return True if deletes the file that matches the name and the type
	 */
	public boolean delete(FileType type, String fileName);
	
	/**
	 * Removes all the temporary files
	 *
	 * @return True if it empties the directory
	 */
	public boolean clearTemp();

}
