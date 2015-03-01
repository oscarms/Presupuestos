/**
 * Budget application specific objects
 */
package budget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import dao.BudgetDAO;
import database.BudgetDB;

/**
 * This class represents a Budget or an Offer. The object is
 * created from the data in the database. The object contains
 * basic information about the budget, provides methods to get
 * its Client, the Salesperson of the Client, the User who
 * created the budget, to get the Total,
 * to get its documents, attachments, annotations...
 * The sections array must be ordered before being added
 * to the budget. It is added to the budget when requested.
 * The arrays returned by the methods must be ordered
 * before being processed by the class.
 * 
 * @author oscar
 */
public class Budget {

	/*
	 * Attributes
	 */
	private String budgetId;
	private String constructionRef;
	private long creationDate;
	private long expirationDate;
	private String note;
	private boolean hasGlobalTotal;
	private float taxRate; // percentage
	private boolean isOffer;
	private User author;
	private Client client;
	private Salesperson signer;
	private Section[] sections = null; // will be set with getSections()
	private Cover cover;
	
	/**
	 * Constructor
	 *
	 * @param isOffer True if offer, false if budget
	 * @param budgetId The ID of the budget
	 * @param creationDate The creation date in ms
	 * @param expirationDate The expiration date in ms
	 * @param client The client of the budget
	 * @param constructionRef The construction reference
	 * @param author The user that made the budget
	 * @param note A final note in the budget
	 * @param hasGlobalTotal Total of all sections or spare each section
	 * @param taxRate The percentage of taxes
	 * @param cover The selected cover
	 * @param signer The administrator that signed the offer
	 */
	public Budget(boolean isOffer, String budgetId, long creationDate,
			long expirationDate, Client client, String constructionRef,
			User author, String note, boolean hasGlobalTotal,
			float taxRate, Cover cover, Salesperson signer) {
		
		this.budgetId = budgetId;
		this.constructionRef = constructionRef;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
		this.note = note;
		this.hasGlobalTotal = hasGlobalTotal;
		this.taxRate = taxRate;
		this.isOffer = isOffer;
		this.author = author;
		this.client = client;
		this.cover = cover;
		this.sections = null; // will be set with getSections()
		this.signer = signer;
	}
	
	/**
	 * Returns budgetId attribute
	 * May be null (but it shouldn't)
	 *
	 * @return The ID of the budget
	 */
	public String getBudgetId() {
		return this.budgetId;
	}
	
	/**
	 * Returns constructionRef attribute
	 * or empty string if it is null
	 *
	 * @return The construction reference
	 */
	public String getConstructionRef() {
		if ( this.constructionRef == null )
			return "";
		else
			return this.constructionRef;
	}
	
	/**
	 * Returns creationDate attribute
	 *
	 * @return The creation date in ms
	 */
	public long getCreationDate() {
		return this.creationDate;
	}
	
	/**
	 * Returns creationDate as string
	 *
	 * @return The creation date in text
	 */
	public String getCreationDateString() {
		if (this.getCreationDate() <= 0)
			return "No existe";
		DateFormat dateFormat = 
				new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date( this.getCreationDate() );
		return dateFormat.format(date);
	}
	
	/**
	 * Returns expirationDate attribute
	 *
	 * @return The expiration date in ms
	 */
	public long getExpirationDate() {
		return this.expirationDate;
	}
	
	/**
	 * Returns expirationDate as string
	 *
	 * @return The creation date in text
	 */
	public String getExpirationDateString() {
		if (this.getExpirationDate() <= 0)
			return "No existe";
		DateFormat dateFormat = 
				new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date( this.getExpirationDate() );
		return dateFormat.format(date);
	}
	
	/**
	 * Returns note attribute
	 * or empty string if it is null
	 *
	 * @return A final note in the budget
	 */
	public String getNote() {
		if ( this.note == null )
			return "";
		else
			return this.note;
	}
	
	/**
	 * Returns isOffer attribute
	 * If it is false it consists in a budget,
	 * if true it consists in an offer
	 *
	 * @return isOffer True if offer, false if budget
	 */
	public boolean isOffer() {
		return this.isOffer;
	}
	
	/**
	 * Returns true if the current date
	 * is newer than the expiration date
	 *
	 * @return True if the expiration date is outdated
	 */
	public boolean isExpired() {
		return ( System.currentTimeMillis() >
					this.getExpirationDate() );
	}

	/**
	 * Returns globalTotal attribute
	 * If it is true all sections are part of a
	 * budget and will be added in a total.
	 * If it is false, each sections acts as a
	 * different budget.
	 *
	 * @return True if the sections are part of the total
	 */
	public boolean hasGlobalTotal() {
		return this.hasGlobalTotal;
	}
	
	/**
	 * Returns taxRate attribute as percentage
	 *
	 * @return Percentage of taxes
	 */
	public float getTaxRate() {
		return this.taxRate;
	}
	
	/**
	 * Returns the sum of all sections total
	 * in the creation date.
	 * If not hasGlobalTotal returns -1
	 *
	 * @return The sum of all sections total
	 */
	public float getTotal() {
		Section[] sections = this.getSections();
		
		if ( !hasGlobalTotal() )
			return -1;
		
		if ( sections == null || sections.length == 0 )
			return 0;
			
		float sum = 0;
		for (Section section : sections) {
			sum += section.getTotal(this.getCreationDate());
		}
		return sum;
	}
	
	/**
	 * Returns the sum of all sections total
	 * if the creation date were now.
	 * If not hasGlobalTotal returns -1
	 *
	 * @return The sum of all sections total
	 */
	public float getCurrentTotal() {
		Section[] sections = this.getSections();
		
		if ( !hasGlobalTotal() )
			return -1;
		
		if ( sections == null || sections.length == 0 )
			return 0;
			
		float sum = 0;
		for (Section section : sections) {
			sum += section.getTotal(System.currentTimeMillis());
		}
		return sum;
	}
	
	/**
	 * Returns the sum of all sections total
	 * plus taxes in the creation date.
	 * If not hasGlobalTotal returns -1
	 *
	 * @return The total plus taxes
	 */
	public float getTotalPlusTaxes() {
		if ( !hasGlobalTotal() )
			return -1;
		
		return ( this.getTotal() * (1 + (this.getTaxRate() / 100) ));
	}
	
	/**
	 * Returns the sum of all sections total
	 * plus taxes if the creationDate were now
	 * If not hasGlobalTotal returns -1
	 *
	 * @return The total plus taxes
	 */
	public float getCurrentTotalPlusTaxes() {
		if ( !hasGlobalTotal() )
			return -1;
		
		return ( this.getCurrentTotal() * (1 + (this.getTaxRate() / 100) ));
	}
	
	/**
	 * Returns the salesperson associated with
	 * the client of the budget
	 *
	 * @return The salesperson of the client of the budget
	 */
	public Salesperson getSalesperson() {
		if (this.getClient() == null)
			return null;
		else
			return this.getClient().getSalesperson();
	}
	
	/**
	 * Returns the salesperson that signed
	 * the offer
	 *
	 * @return The salesperson of the client of the budget
	 */
	public Salesperson getSigner() {
		return this.signer;
	}
	
	/**
	 * Returns the author of the budget
	 * as a User (Currently will be a Salesperson)
	 *
	 * @return The author of the budget
	 */
	public User getAuthor() {
		return this.author;
	}
	
	/**
	 * Returns the client of the budget
	 *
	 * @return The client of the budget
	 */
	public Client getClient() {
		return this.client;
	}
	
	/**
	 * Returns the sections of the budget
	 * If it is null it retrieves the
	 * sections from the DB and sets it
	 *
	 * @return The sections of the budget
	 */
	public Section[] getSections() {
		if (this.sections == null) {
			// get the sections
			BudgetDAO budgetDAO = new BudgetDB();
			this.sections = budgetDAO.getSections(budgetId);
			// if remains null, set as an empty string
			if (this.sections == null)
				this.sections = new Section[0];
		}
		return this.sections;
	}
	
	/**
	 * Returns the annotations of the budget.
	 * If it is null returns an empty string.
	 * Method uses BudgetDAO and budgetDB.
	 *
	 * @return The annotations of the budget
	 */
	public Annotation[] getAnnotations() {
		BudgetDAO budgetDAO = new BudgetDB();
		Annotation[] annotations = budgetDAO.getAnnotations(budgetId);
		if (annotations == null)
			return new Annotation[0];
		else
			return annotations;
	}
	
	/**
	 * Returns the documents of the budget.
	 * If it is null returns an empty string.
	 * Method uses BudgetDAO and budgetDB.
	 *
	 * @return The documents of the budget
	 */
	public Document[] getDocuments() {
		BudgetDAO budgetDAO = new BudgetDB();
		Document[] documents = budgetDAO.getDocuments(budgetId);
		if (documents == null)
			return new Document[0];
		else
			return documents;
	}
	
	/**
	 * Returns the attachments of the budget.
	 * If it is null returns an empty string.
	 * Method uses BudgetDAO and budgetDB.
	 *
	 * @return The attachments of the budget
	 */
	public Attachment[] getAttachments() {
		BudgetDAO budgetDAO = new BudgetDB();
		Attachment[] attachments = budgetDAO.getAttachments(budgetId);
		if (attachments == null)
			return new Attachment[0];
		else
			return attachments;
	}
	
	/**
	 * Returns the family of the budget.
	 * The family are the other budgets and offers
	 * created as a new version of any of the
	 * budgets and offers in the family.
	 * If it is null returns an empty string.
	 * Method uses BudgetDAO and budgetDB.
	 *
	 * @return A list of budgets of the same family
	 */
	public Budget[] getFamily() {
		BudgetDAO budgetDAO = new BudgetDB();
		Budget[] family = budgetDAO.getFamily(budgetId);
		if (family == null)
			return new Budget[0];
		else
			return family;
	}
	
	/**
	 * Returns the cover of the budget
	 *
	 * @return The selected cover of the budget
	 */
	public Cover getCover() {
		if (this.cover == null)
			return new Cover();
		else
			return this.cover;
	}
	
	/**
	 * Check if all the data introduced in the
	 * budget is possible: No empty sections,
	 * has all the attributes filled, the
	 * client has a salesperson, etc
	 *
	 * @return a list of integers: Empty
	 * if the budget is correct. Errors:
	 * -1 if isExpired, -2 if not have author,
	 * -3 if not have client, -4 if the client
	 * does not have salesperson, -5 if there
	 * is an empty section, -6 if there are
	 * no sections, -13 if the client is not
	 * active, -14 if there are discontinued 
	 * products. Advertises: 7 if there is
	 * a product with no quantity, 8 if there
	 * is no final note, 9 if there is no tax
	 * rate, 10 if there is a section with no
	 * name, 11 if there is any discount negative,
	 * 12 if there is no construction reference,
	 * 15 if the salesperson is not enabled.
	 * 
	 */
	public Integer[] check() {
		Set<Integer> messages = new HashSet<Integer>();
		if (this.isExpired())
			messages.add(-1);
		if (this.getAuthor() == null)
			messages.add(-2);
		if (this.getClient() == null)
			messages.add(-3);
		if (this.getSalesperson() == null)
			messages.add(-4);
		if (this.getClient() != null && this.getClient().isInactive())
			messages.add(-13);
		if (this.getSalesperson() != null 
				&& !this.getSalesperson().isEnabled())
			messages.add(15);
		Section[] sections = this.getSections();
		if (sections == null || sections.length < 1)
			messages.add(-6);
		for (Section section : sections) {
			if (section.getProducts() == null 
					|| section.getProducts().length < 1)
				messages.add(-5);
			
			for (SectionProduct product : section.getProducts()) {
				if (product.getQuantity() == 0)
					messages.add(7);
				
				if (product.getDiscount1()< 0 || 
						product.getDiscount2()< 0 || 
						product.getDiscount3()< 0)
					messages.add(11);
				
				if (product.isCurrentlyDiscontinued())
					messages.add(-14);
				
			}
			
			if (section.getName().length() < 1)
				messages.add(10);
		}
		
		if (this.getConstructionRef().length()<1)
			messages.add(12);
		if (this.getNote().length()<1)
			messages.add(8);
		if (this.getTaxRate() <= 0)
			messages.add(9);
		
		Integer[] msgArray = ((Set<Integer>)messages)
				.toArray(new Integer[messages.size()]);
		return msgArray;
	}
	
	/**
	 * Check if all the data introduced in the
	 * budget is possible: No empty sections,
	 * has all the attributes filled, the
	 * client has a salesperson, etc
	 *
	 * @return true if the budget is valid
	 * 
	 */
	public boolean isValid() {
		for (Integer i : this.check()) {
			if ( i != null && i < 0 )
				return false;
		}
		return true;
	}
	
	/**
	 * Returns the coverId of the cover
	 *
	 * @return The cover ID of the cover
	 */
	public int getCoverId() {
		if (this.getCover() == null)
			return -1;
		else
			return this.getCover().getId();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ( isOffer ? "Oferta " : "Presupuesto ")
				+ this.getBudgetId() + ": " 
				+ this.getConstructionRef() + ". (" 
				+ this.getNote() + "). " 
				+ this.getAuthor().toString() + ". " 
				+ this.getClient().toString() + ". " + 
				Arrays.deepToString(this.getSections()) + ". " + 
				Arrays.deepToString(this.getAnnotations()) + ". " + 
				Arrays.deepToString(this.getDocuments())  + ". " + 
				Arrays.deepToString(this.getAttachments());
	}

}
