/**
 * Budget application specific objects
 */
package budget;

import java.io.File;

import storage.FileLocalStorage;
import dao.FileDAO;
import dao.FileType;

/**
 * This class represents a document that is present
 * in a budget to be used as reference by the salesperson.
 * It provides methods to get the file using
 * classes in package dao and storage.
 * 
 * @author oscar
 */
public class Document {

	/*
	 * Attributes
	 */
	private int id;
	private String name;
	private String budgetId;
	
	/**
	 * Constructor to be used when retrieving from DB
	 *
	 * @param documentId The ID of the document in the budget
	 * @param name The name of the original file
	 * @param budgetId The ID of the budget that owns the document
	 */
	public Document(int documentId, String name, String budgetId) {
		this.id = documentId;
		this.name = name;
		this.budgetId = budgetId;
	}
	
	/**
	 * Constructor to be used when creating document
	 *
	 * @param name The name of the original file
	 * @param budgetId The ID of the budget that owns the document
	 */
	public Document(String name, String budgetId) {
		this(-1, name, budgetId);
	}
	
	/**
	 * Returns id attribute
	 *
	 * @return The ID of the document in the budget
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns name attribute or empty string if it is null
	 *
	 * @return The name of the original file
	 */
	public String getName() {
		if ( this.name == null )
			return "";
		else
			return this.name;
	}
	
	/**
	 * Returns budgetId attribute, 
	 * may be null (but shouldn't)
	 *
	 * @return The ID of the budget that owns the document
	 */
	public String getBudgetId() {
		return this.budgetId;
	}
	
	/**
	 * Returns a File with the document
	 *
	 * @return A file that contains the document
	 */
	public File getDocument() {
		FileDAO fileDAO = new FileLocalStorage();
		// filename in the storage, not the real name
		String fileName = this.getBudgetId() + "_" 
				+ Integer.toString( this.getId() );
		return fileDAO.get(FileType.DOCUMENT, fileName);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return this.getName();
	}

}
