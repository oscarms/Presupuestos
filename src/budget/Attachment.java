/**
 * Budget application specific objects
 */
package budget;

import java.io.File;

import dao.FileDAO;
import dao.FileType;
import storage.FileLocalStorage;

/**
 * This class represents a file that is added
 * at the end of the budget when it is printed.
 * It provides methods to get the file using
 * classes in package dao and storage.
 * 
 * @author oscar
 */
public class Attachment {
	
	/*
	 * Attributes
	 */
	private int id;
	private String name;
	private String budgetId;

	/**
	 * Constructor to be used when retrieving from DB
	 *
	 * @param id The attachment unique ID
	 * @param name The title of the file attached
	 * @param budgetId The ID of the Budget that the attachment is upload to
	 */
	public Attachment(int id, String name, String budgetId) {
		this.id = id;
		this.name = name;
		this.budgetId = budgetId;
	}
	
	/**
	 * Constructor to be used when creating attachment
	 *
	 * @param name The title of the file attached
	 * @param budgetId The ID of the Budget that the attachment is upload to
	 */
	public Attachment(String name, String budgetId) {
		this(-1, name, budgetId);
	}
	
	/**
	 * Returns id attribute
	 *
	 * @return The attachment unique ID
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns name attribute or empty string if it is null
	 *
	 * @return The title of the file attached
	 */
	public String getName() {
		if ( name == null )
			return "";
		else
			return this.name;
	}
	
	/**
	 * Returns budgetId attribute, 
	 * may be null (but shouldn't)
	 *
	 * @return The ID of the Budget that the attachment is upload to
	 */
	public String getBudgetId() {
		return this.budgetId;
	}
	
	/**
	 * Returns a File with the attachment
	 *
	 * @return A File with the attachment
	 */
	public File getAttachment() {
		FileDAO fileDAO = new FileLocalStorage();
		// filename in the storage, not the real name
		String fileName = this.getBudgetId() + "_" 
				+ Integer.toString( this.getId() );
		return fileDAO.get(FileType.ATTACHMENT, fileName);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return this.getName();
	}

}
