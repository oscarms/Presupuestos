/**
 * Data Access Objects
 */
package dao;

import budget.Annotation;
import budget.Attachment;
import budget.Budget;
import budget.Cover;
import budget.Document;
import budget.Section;
import budget.User;

/**
 * This class provides the specification of the methods
 * available to create, get and update Budgets, Sections,
 * SectionProducts, Annotations, Documents, Attachments
 * and Covers. It is implemented in BudgetDB.
 * 
 * @author oscar
 */
public interface BudgetDAO {

	/*
	 * Attributes
	 */
	/**
	 * Maximum results that the class can give in
	 * an array. If an array returned by this
	 * class is MAXRESULTS length,
	 * there may be results not given.
	 */
	public static final int MAXRESULTS = 50;
	
	public static final float DEFAULT_TAXRATE = 21;
	public static final String DEFAULT_NOTE = 
			"El presente documento está calculado con los datos "
			+ "que se nos han facilitado y según los precios de "
			+ "la tarifa actualmente vigente. Este presupuesto "
			+ "es orientativo y se aconseja revisar las "
			+ "cantidades y los materiales para evitar errores.";
	
	/* Get budgets */
	
	/**
	 * Retrieves the Budget with the specified budgetId
	 * from the database, null if not exists.
	 * 
	 * @param budgetId The unique ID of the budget
	 * @return The budget that matches the ID
	 */
	public Budget getBudget(String budgetId);
	
	/**
	 * Retrieves the budgets (up to a max, if the length
	 * of the array is max there may be results not listed)
	 * assigned to a salesperson with the specified
	 * search criteria. The filter search if every word
	 * introduced by the user is contained in any of the
	 * following: The client number, address, town, province,
	 * country, postalCode, name, email, phone, person or notes,
	 * or the name or email of his assigned salesperson;
	 * the author name or email; the name of any section,
	 * product id, name or description that the budget contains;
	 * any annotation text, document or attachment name;
	 * the budget id, construction reference or note.
	 * 
	 * @param salespersonId The ID of the salesperson assigned to the budgets
	 * @param from Minimum creation date for the search
	 * @param to Maximum creation date for the search
	 * @param filter Words that will include the results
	 * @param expired If true, will include expired budgets
	 * @return A list of budgets that match the parameters
	 */
	public Budget[] getBudgets(int salespersonId, long from, long to,
			String filter, boolean expired);
	
	/**
	 * Retrieves the budgets (up to a max, if the array
	 * returned is max+1 there are results not listed)
	 * assigned (if not others) or not (if others,
	 * this only should be used if the user can view all
	 * clients) with the specified search criteria. The filter
	 * search if every word introduced by the user is contained
	 * in any of the following: The client number, address, town,
	 * province, country, postalCode, name, email, phone, person
	 * or notes, or the name or email of his assigned salesperson;
	 * the author name or email; the name of any section,
	 * product id, name or description that the budget contains;
	 * any annotation text, document or attachment name;
	 * the budget id, construction reference or note.
	 * 
	 * @param salespersonId The ID of the salesperson assigned to the budgets
	 * @param others If true, will return the budgets NOT matching the salesperson
	 * @param from Minimum creation date for the search
	 * @param to Maximum creation date for the search
	 * @param filter Words that will include the results
	 * @param expired If true, will include expired budgets
	 * @return A list of budgets that match the parameters
	 */
	public Budget[] getBudgets(int salespersonId, boolean others, long from,
			long to, String filter, boolean expired);
	
	
	/* Notifications */
	
	/**
	 * Retrieves the list of all offers completed waiting for
	 * the sign of an administrator whose salesperson is the specified.
	 * This method only should be called if the user can view offers.
	 * 
	 * @param salespersonId The salesperson ID matching the salesperson
	 * @return List of completed offers not signed
	 */
	public Budget[] getNotSignedOffers(int salespersonId);
	
	/**
	 * Retrieves the list of all offers completed for clients
	 * of the salesperson waiting for the sign of an administrator.
	 * This method only should be called if the user is an administrator.
	 * 
	 * @return A list of offers not signed with the specified salesperson
	 */
	public Budget[] getNotSignedOffers();
			
	/**
	 * Retrieves the list of all budgets and offers not completed
	 * by the author.
	 * 
	 * @param authorId The ID of the creator
	 * @return A list of budgets and offers no completed by the salesperson
	 */
	public Budget[] getIncompleteBudgets(int authorId);
	
	/**
	 * Retrieves the list of all budgets notified as created
	 * (have creation date) by another author, and the salesperson
	 * is the specified (if not others) or different to the specified
	 * (if others).
	 * 
	 * @param salespersonId The salesperson ID matching the salesperson
	 * @param others If true, will return the budgets not matching the salesperson
	 * @return A list of budgets created by others for the salesperson
	 */
	public Budget[] getNewBudgets(int salespersonId, boolean others);
	
	/**
	 * Retrieves the list of all budgets notified as created
	 * (have creation date) by another author, and the salesperson
	 * is the specified.
	 * 
	 * @param salespersonId The salesperson ID matching the salesperson
	 * @return A list of budgets created by others for the salesperson
	 */
	public Budget[] getNewBudgets(int salespersonId);
	
	/**
	 * Retrieves the list of all offers notified as signed
	 * (have sign) by another administrator, and the
	 * salesperson is the specified.
	 * 
	 * @param salespersonId The salesperson ID matching the salesperson
	 * @return A list of offers signed for the salesperson
	 */
	public Budget[] getNewSignedOffers(int salespersonId);
	
	/**
	 * Retrieves the list of all offers notified as signed
	 * (have sign) by another administrator, and the
	 * salesperson is the specified (if not others) or 
	 * different to the specified (if others).
	 * 
	 * @param salespersonId The salesperson ID matching the salesperson
	 * @param others If true, will return the budgets not matching the salesperson
	 * @return A list of offers signed for the salesperson
	 */
	public Budget[] getNewSignedOffers(int salespersonId, boolean others);
	
	/**
	 * Retrieves the sum of the number of incompleteBudgets whose salesperson
	 * is the author, offers not yet signed (if the user is an administrator)
	 * and notifications (include notifications of offers only if the user can
	 * view offers)
	 *
	 * @param salespersonId The unique ID of the salesperson
	 * @return The sum of budgets with notification or incomplete
	 */
	public int getNotificationCount(int salespersonId);

	/**
	 * Removes the notification that relations the specified
	 * budget and salesperson.
	 * @param salespersonId The unique ID of the salesperson
	 * @param budgetId The unique ID of the budget
	 * @return True if the notification is removed
	 */
	public boolean removeNotification(int salespersonId, String budgetId);
	
	/**
	 * Creates a notification that relations the specified
	 * budget and salesperson.
	 * @param salespersonId The unique ID of the salesperson
	 * @param budgetId The unique ID of the budget
	 * @return True if the notification is added
	 */
	public boolean addNotification(int salespersonId, String budgetId);
	
	/**
	 * Creates a notification for all the administrators
	 * but the salesperson specified that relations them
	 * with the specified budget.
	 * @param salespersonId The unique ID of the salesperson
	 * @param budgetId The unique ID of the budget
	 * @param otherAdministrators If true, the notifications are for all the administrators
	 * @return True if the notifications were created
	 */
	public boolean addNotification(int salespersonId, String budgetId,
			boolean otherAdministrators);
	
	
	/* List, Add, Remove, Update SectionProducts */

	/**
	 * Creates a new product in a section of a budget in
	 * the database with a quantity of 1. Cannot be done if
	 * the budget is completed, or the offer signed.
	 * Returns true if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the sections
	 * @param productId The ID of the product
	 * @return True if the product is inserted to the section
	 */
	public boolean addProduct(String budgetId, int sectionId, int productId);

	/**
	 * Removes a product in a section of a budget
	 * from the database. Cannot be done if
	 * the budget is completed, or the offer signed.
	 * Returns true if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the sections
	 * @param productId The ID of the product
	 * @return True if the product is removed from the section
	 */
	public boolean removeProduct(String budgetId, int sectionId, int productId);

	/**
	 * Updates one or several products in a section of
	 * a budget in the database to order the specified
	 * product before or after the previous or the
	 * succeeding. Cannot be done if the budget is
	 * completed, or the offer signed. Returns true if
	 * it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the sections
	 * @param productId The ID of the product
	 * @param after True to order after the next product, in the other case order before the previous
	 * @return True if the product line is updated
	 */
	public boolean sortProduct(String budgetId, int sectionId, int productId,
			boolean after);

	/**
	 * Updates a product in a section of a budget
	 * in the database to set its quantity to the specified.
	 * Cannot be done if the budget is completed, or the offer
	 * signed. Returns true if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the sections
	 * @param productId The ID of the product
	 * @param quantity The units of product
	 * @return True if it is updated
	 */
	public boolean setQuantity(String budgetId, int sectionId, int productId,
			float quantity);

	/**
	 * Updates a product in a section of an offer
	 * in the database to set its discounts to the
	 * specified. Cannot be done if the budget is
	 * completed, or the offer signed. Returns true
	 * if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the sections
	 * @param productId The ID of the product
	 * @param discount1 A percentage of discount
	 * @param discount2 A percentage of discount
	 * @param discount3 A percentage of discount
	 * @return True if the product line is updated
	 */
	public boolean setDiscounts(String budgetId, int sectionId, int productId,
			float discount1, float discount2, float discount3);
	
	
	/* List, Add, Update, Remove Sections */
	
	/**
	 * Retrieves the sections of a specified budget.
	 * 
	 * @param budgetId The ID of the budget
	 * @return A list of the sections of the budget, sorted
	 */
	public Section[] getSections(String budgetId);
	
	/**
	 * Creates a new section of a budget in the
	 * database. Returns the ID of the new section
	 * 
	 * @param budgetId The ID of the budget
	 * @return The ID of the new section
	 */
	public int addSection(String budgetId);
	
	/**
	 * Creates a new section of a budget in the
	 * database with name. Cannot be done if
	 * the budget is completed, or the offer
	 * signed.
	 * 
	 * @param budgetId The ID of the budget
	 * @param name The title of the section
	 * @return The ID of the new section
	 */
	public int addSection(String budgetId, String name);
	
	/**
	 * Removes a section of a budget from the
	 * database. It cannot remove the section if
	 * there are products. Cannot be done if
	 * the budget is completed, or the offer signed.
	 * Returns true if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the section in the budget
	 * @return True if the section is removed from the budget
	 */
	public boolean removeSection(String budgetId, int sectionId);
	
	/**
	 * Updates one or several sections of a budget
	 * in the database to order the specified section
	 * before or after the previous or the succeeding.
	 * Cannot be done if the budget is completed,
	 * or the offer signed.
	 * Returns true if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the section in the budget
	 * @param after True to order after the next section, in the other case order before the previous
	 * @return True if it is updated
	 */
	public boolean sortSection(String budgetId, int sectionId, boolean after);
	
	/**
	 * Updates a section of a budget with a new name.
	 * Cannot be done if the budget is completed, or
	 * the offer signed. Returns true if it completes
	 * the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param sectionId The ID of the section in the budget
	 * @param name The new title of the section
	 * @return True if the section is updated
	 */
	public boolean renameSection(String budgetId, int sectionId, String name);
	
	
	/* Get Budget Family, Annotations, Documents, Attachments */
	
	/**
	 * Retrieves the budgets that has been modified
	 * from the same budget as the specified.
	 * 
	 * @param budgetId The ID of the budget
	 * @return A list of the budgets modified from the same one, sorted by ID
	 */
	public Budget[] getFamily(String budgetId);
	
	/**
	 * Retrieves the annotations of a budget.
	 * 
	 * @param budgetId The ID of the budget
	 * @return A list of the annotations of the budget, sorted by date
	 */
	public Annotation[] getAnnotations(String budgetId);
	
	/**
	 * Retrieves the documents of a budget.
	 * 
	 * @param budgetId The ID of the budget
	 * @return A list of the documents of the budget
	 */
	public Document[] getDocuments(String budgetId);
	
	/**
	 * Retrieves the attachments of a budget.
	 * 
	 * @param budgetId The ID of the budget
	 * @return A list of the attachments of the budget, sorted
	 */
	public Attachment[] getAttachments(String budgetId);
	
	/**
	 * Retrieves the specified document of a budget.
	 * 
	 * @param budgetId The ID of the budget
	 * @param documentId The ID of the document
	 * @return The requested document
	 */
	public Document getDocument(String budgetId, int documentId);
	
	/**
	 * Retrieves the specified attachment of a budget.
	 * 
	 * @param budgetId The ID of the budget
	 * @param attachmentId The ID of the attachment
	 * @return The requested attachment
	 */
	public Attachment getAttachment(String budgetId, int attachmentId);
	
	/* List, Add, Set Covers */
	
	/**
	 * Retrieves the existing covers.
	 * 
	 * @return A list of all the covers
	 */
	public Cover[] getCovers();
	
	/**
	 * Retrieves the requested cover.
	 * 
	 * @return The requested cover
	 */
	public Cover getCover(int coverId);
	
	/**
	 * Creates a new cover and returns
	 * the new ID of the cover.
	 * 
	 * @param cover
	 * @return The ID of the cover
	 */
	public int create(Cover cover);
	
	/**
	 * Removes a cover and returns
	 * true if it was successful.
	 * 
	 * @param coverId The unique ID of the cover
	 * @return True if the cover was removed
	 */
	public boolean removeCover(int coverId);
	
	/**
	 * Updates a budget with the specified cover.
	 * Returns true if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param coverId The ID of the cover selected for the budget
	 * @return True if the budget is updated
	 */
	public boolean setCover(String budgetId, int coverId);
	
	
	/* Create and Remove Annotations, Documents, Attachments */
	
	/**
	 * Creates an annotation in the specified
	 * budget in the database. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param annotation The new annotation
	 * @return True if the annotation is inserted
	 */
	public boolean create(String budgetId, Annotation annotation);
	
	/**
	 * Creates a document in the specified
	 * budget in the database. Returns the ID
	 * of the new document.
	 * 
	 * @param budgetId The ID of the budget
	 * @param document The new document
	 * @return The ID assigned to the new document
	 */
	public int create(String budgetId, Document document);
	
	/**
	 * Creates a attachment in the specified
	 * budget in the database. Returns the ID
	 * of the new attachment.
	 * 
	 * @param budgetId The ID of the budget
	 * @param attachment The new attachment
	 * @return The ID assigned to the new attachment
	 */
	public int create(String budgetId, Attachment attachment);
	
	/**
	 * Removes the specified document of the budget
	 * in the database. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param documentId The ID of the document
	 * @return True if the document is removed from the budget
	 */
	public boolean removeDocument(String budgetId, int documentId);
	
	/**
	 * Removes the specified attachment of the budget
	 * in the database. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param attachmentId The ID of the attachment
	 * @return True if the attachment is removed from the budget
	 */
	public boolean removeAttachment(String budgetId, int attachmentId);
	
	/**
	 * Removes the specified attachment of the budget
	 * in the database. Returns true if it
	 * completes the operation. 
	 * Updates one or several attachments of a budget
	 * in the database to order the specified
	 * before or after the previous or the
	 * succeeding. Returns true if it completes
	 * the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param attachmentId The ID of the attachment
	 * @param after True to order after the next product, in the other case order before the previous
	 * @return True if the attachment is removed from the budget
	 */
	public boolean sortAttachment(String budgetId, int attachmentId, boolean after);
	
	/* Create budgets */
	
	/**
	 * Creates a budget in the database with the
	 * specified author, or an offer if isOffer.
	 * Returns the budget ID.
	 * 
	 * @param author The user that is creating the budget
	 * @param isOffer If it is true, the budget is an offer
	 * @return The ID of the new budget
	 */
	public String createBudget(User author, boolean isOffer);
	
	/**
	 * Creates a budget in the database with the
	 * specified author. Returns the budget ID.
	 * 
	 * @param author The user that is creating the budget
	 * @return The ID of the new budget
	 */
	public String createBudget(User author);
	
	/**
	 * Creates a budget in the database with the
	 * specified author, or an offer if isOffer,
	 * and the specified budget from as they are
	 * in the same family. Returns the budget ID.
	 * 
	 * @param author The user that is creating the budget
	 * @param budgetIdFrom The ID of the budget in the same family
	 * @param isOffer If it is true, the budget is an offer
	 * @return The ID of the new budget
	 */
	public String createBudget(User author, String budgetIdFrom, boolean isOffer);
	
	/**
	 * Creates a budget in the database with the
	 * specified author and the specified budget
	 * from as they are in the same family.
	 * Returns the budget ID.
	 * 
	 * @param author The user that is creating the budget
	 * @param budgetIdFrom The ID of the budget in the same family
	 * @return The ID of the new budget
	 */
	public String createBudget(User author, String budgetIdFrom);
	
	
	/* Update budgets */
	
	/**
	 * Updates the budget with a new client. The
	 * budget cannot be completed, or the offer
	 * signed. Returns true if it completes the
	 * operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param clientId The ID of the client assigned to the budget
	 * @return True if the budget is updated
	 */
	public boolean setClient(String budgetId, int clientId);
	
	/**
	 * Updates the budget with a new construction
	 * reference. The budget cannot be completed,
	 * or the offer signed. Returns true if it
	 * completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param constructionRef The construction reference
	 * @return True if the budget is updated
	 */
	public boolean setConstructionRef(String budgetId, String constructionRef);
	
	/**
	 * Updates the budget with a new expiration
	 * date. The budget cannot be completed, or
	 * the offer signed. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param date The new expiration date, in ms
	 * @return True if the budget is updated
	 */
	public boolean setExpirationDate(String budgetId, long date);
	
	/**
	 * Updates the budget with a new final
	 * note. The budget cannot be completed, or
	 * the offer signed. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param note The new note
	 * @return True if the budget is updated
	 */
	public boolean setNote(String budgetId, String note);
	
	/**
	 * Updates the budget with a new tax
	 * rate. The budget cannot be completed, or
	 * the offer signed. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param taxRate The new tax rate
	 * @return True if the budget is updated
	 */
	public boolean setTaxRate(String budgetId, float taxRate);
	
	/**
	 * Updates the budget setting if it has a
	 * global total o a spare total for each section.
	 * The budget cannot be completed, or
	 * the offer signed. Returns true if it
	 * completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param hasGlobalTotal If true, all the sections are part of the total of the budget
	 * @return True if the budget is updated
	 */
	public boolean setGlobalTotal(String budgetId, boolean hasGlobalTotal);
	
	/**
	 * Updates the budget with a new creation
	 * date, so it makes the budget complete.
	 * The budget cannot be completed, or
	 * the offer signed before. Returns true
	 * if it completes the operation. 
	 * 
	 * @param budgetId The ID of the budget
	 * @param creationDate The new creation date
	 * @return True if the budget is updated
	 */
	public boolean createBudget(String budgetId, long creationDate);
	
	/**
	 * Updates the offer with a new creation
	 * date and sets the sign to the salesperson
	 * specified, so it makes the offer complete.
	 * The budget cannot be completed, or
	 * the offer signed before. Returns true
	 * if it completes the operation.
	 * 
	 * @param budgetId The ID of the budget
	 * @param salespersonId The ID of the signer
	 * @param date The new creation date
	 * @return True if the budget is updated
	 */
	public boolean signOffer(String budgetId, int salespersonId, long date);
	
}
