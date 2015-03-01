/**
 * Objects to store files in the local drive
 */
package storage;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;

import dao.FileDAO;
import dao.FileType;

/**
 * This class provides the implementation of the methods
 * available to load and download files to the system.
 * Methods are specified in FileDAO class.
 * The files must be checked as valid before
 * being processed by this class.
 * 
 * @author oscar
 */
public class FileLocalStorage implements FileDAO {

	/*
	 * No Attributes
	 */
	
	/**
	 * Constructor
	 */
	public FileLocalStorage() {
	}

	/* (non-Javadoc)
	 * @see dao.FileDAO#create(dao.FileType, javax.servlet.http.Part, java.lang.String)
	 */
	@Override
	public boolean create(FileType type, Part file, String fileName) {

		if (this.create(type, fileName) != null) {
		
			// Write file
			try {
				file.write(getPath(type, fileName));
				return true;
			} catch (Exception e) {
				return false;
			}
			
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.FileDAO#create(dao.FileType, java.lang.String)
	 */
	@Override
	public File create(FileType type, String fileName) {

		// Create file
		File newFile = new File(getPath(type, fileName));

		return newFile;
	}

	/* (non-Javadoc)
	 * @see dao.FileDAO#create(dao.FileType, java.io.File, java.lang.String)
	 */
	@Override
	public boolean create(FileType type, File file, String fileName) {
		
		File newFile = this.create(type, fileName);
		
		// Write file
		try {
			FileUtils.copyFile(file, newFile);
			return true;
		} catch (Exception e) {
			return false;
		}
		
	}

	/* (non-Javadoc)
	 * @see dao.FileDAO#get(dao.FileType, java.lang.String)
	 */
	@Override
	public File get(FileType type, String fileName) {
		// Select file
		String directory = getDirectory(type);
		File file = new File(directory + fileName);
		
		if (!file.exists()) {
			
			return null;
		}
		return file;
	}
	
	/* (non-Javadoc)
	 * @see dao.FileDAO#delete(dao.FileType, java.lang.String)
	 */
	@Override
	public boolean delete(FileType type, String fileName) {
		// Select file
		String directory = getDirectory(type);
		File file = new File(directory + fileName);
		
		if (!file.exists()) {
			
			return false;
		}
		if (!file.canWrite()) {
			
			return false;
		}
		
		return file.delete();
	}
	
	/* (non-Javadoc)
	 * @see dao.FileDAO#clearTemp()
	 */
	@Override
	public boolean clearTemp() {
		String directory = this.getDirectory(FileType.TEMPORARY);
		
		try {
			FileUtils.cleanDirectory(new File(directory));
		} catch (IOException e) { 
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a string with the directory
	 *
	 * @param type The type of the file
	 * @return The directory where the files of the specified type are stored
	 */
	private String getDirectory(FileType type) {
		String directory = System.getProperty("user.home") + "/GdP_Files/";
		//String directory = System.getenv("OPENSHIFT_DATA_DIR") + "GdP_Files/";
		
		// Assign a different directory for each FileType
		switch (type) {
			case ATTACHMENT:
				directory += "Attachments/";
				break;
			case DOCUMENT:
				directory += "Documents/";
				break;
			case COVER:
				directory += "Covers/";
				break;
			case MINICOVER:
				directory += "MiniCovers/";
				break;
			case PRODUCTIMAGE:
				directory += "ProductImages/";
				break;
			case PRODUCTMINIIMAGE:
				directory += "ProductMiniImages/";
				break;
			case CLIENTIMAGE:
				directory += "ClientImages/";
				break;
			case CLIENTMINIIMAGE:
				directory += "ClientMiniImages/";
				break;
			case GLOBAL:
				directory += "Global/";
				break;
			case TEMPORARY:
				directory += "Temp/";
				break;
		}
		return directory;
	}

	/**
	 * Returns a string with the path of the file
	 *
	 * @param type The type of the file
	 * @param fileName The name of the file to be stored
	 * @return The path where the file can be stored
	 */
	private String getPath(FileType type, String fileName) {
		String directory = getDirectory(type);
		
		// Create directory
		File path = new File(directory);
		if (!path.exists())
			path.mkdirs();
		
		return directory + fileName;
	}
}
