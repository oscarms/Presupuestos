/**
 * Budget application specific objects
 */
package budget;

import java.io.File;

import storage.FileLocalStorage;
import dao.FileDAO;
import dao.FileType;

/**
 * This class represents a Client as the person or company whom
 * the budget is created. The object is created from the data
 * in the database or introduced in a form. The object contains
 * basic information about the customer, who is the salesperson
 * and provides methods to get an image using classes in the
 * packages dao and storage.
 * 
 * @author oscar
 */
public class Client {
	
	/*
	 * Attributes
	 */
	private int id;
	private String clientNumber; // NIF
	private String address;
	private String town;
	private String province;
	private String country;
	private String postalCode;
	private String name;
	private String email;
	private String phone;
	private String person;
		// name of the person that is the contact with the company
	private String notes;
	private boolean isActive;
		// if isActive it is possible to create a new budget to the client
	private Salesperson salesperson;
	
	/**
	 * Constructor to be used when showing client
	 *
	 * @param id The ID of the client
	 * @param clientNumber The fiscal number
	 * @param address The Street, Number, etc
	 * @param town The city
	 * @param province The province
	 * @param country The name of the country
	 * @param postalCode The postal code (String)
	 * @param name The name of the client
	 * @param email The email
	 * @param phone The phone (String)
	 * @param person The name of the person of contact
	 * @param notes A text where the salesperson can annotate about the client
	 * @param isActive If isActive the client can have new budgets
	 * @param salesperson The salesperson assigned to the client
	 */
	public Client(int id, String clientNumber, String address, String town,
			String province, String country, String postalCode, String name,
			String email, String phone, String person, String notes,
			boolean isActive, Salesperson salesperson) {
		this.id = id;
		this.clientNumber = clientNumber;
		this.address = address;
		this.town = town;
		this.province = province;
		this.country = country;
		this.postalCode = postalCode;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.person = person;
		this.notes = notes;
		this.isActive = isActive;
		this.salesperson = salesperson;
	}
	
	/**
	 * Constructor to be used when creating client
	 *
	 * @param clientNumber The fiscal number
	 * @param address The Street, Number, etc
	 * @param town The city
	 * @param province The province
	 * @param country The name of the country
	 * @param postalCode The postal code (String)
	 * @param name The name of the client
	 * @param email The email
	 * @param phone The phone (String)
	 * @param person The name of the person of contact
	 * @param notes A text where the salesperson can annotate about the client
	 * @param isActive If isActive the client can have new budgets
	 * @param salesperson The salesperson assigned to the client
	 */
	public Client(String clientNumber, String address, String town,
			String province, String country, String postalCode,
			String name, String email, String phone, String person,
			String notes, boolean isActive, Salesperson salesperson) {
		this(-1, clientNumber, address, town, province, country,
				postalCode, name, email, phone, person, notes,
				isActive, salesperson);
	}

	/**
	 * Returns id attribute
	 *
	 * @return The ID of the client
	 */
	public int getClientId() {
		return this.id;
	}
	
	/**
	 * Returns clientNumber attribute
	 * Returns an empty string if null
	 *
	 * @return The fiscal number of the client
	 */
	public String getClientNumber() {
		if (this.clientNumber == null)
			return "";
		else
			return this.clientNumber;
	}
	
	/**
	 * Returns address attribute
	 * Returns an empty string if null
	 *
	 * @return The street, number, etc
	 */
	public String getAddress() {
		if (this.address == null)
			return "";
		else
			return this.address;
	}
	
	/**
	 * Returns town attribute
	 * Returns an empty string if null
	 *
	 * @return The city
	 */
	public String getTown() {
		if (this.town == null)
			return "";
		else
			return this.town;
	}
	
	/**
	 * Returns province attribute
	 * Returns an empty string if null
	 *
	 * @return The province
	 */
	public String getProvince() {
		if (this.province == null)
			return "";
		else
			return this.province;
	}
	
	/**
	 * Returns country attribute
	 * Returns an empty string if null
	 *
	 * @return The name of the country
	 */
	public String getCountry() {
		if (this.country == null)
			return "";
		else
			return this.country;
	}
	
	/**
	 * Returns postalCode attribute
	 * Returns an empty string if null
	 *
	 * @return The postal code (String)
	 */
	public String getPostalCode() {
		if (this.postalCode == null)
			return "";
		else
			return this.postalCode;
	}
	
	/**
	 * Returns name attribute
	 * Returns an empty string if null
	 *
	 * @return The name of the client
	 */
	public String getName() {
		if (this.name == null)
			return "";
		else
			return this.name;
	}
	
	/**
	 * Returns email attribute
	 * Returns an empty string if null
	 *
	 * @return The email address
	 */
	public String getEmail() {
		if (this.email == null)
			return "";
		else
			return this.email;
	}
	
	/**
	 * Returns phone attribute
	 * Returns an empty string if null
	 *
	 * @return The phone number (String)
	 */
	public String getPhone() {
		if (this.phone == null)
			return "";
		else
			return this.phone;
	}
	
	/**
	 * Returns person attribute
	 * Returns an empty string if null
	 *
	 * @return The name of the person of contact
	 */
	public String getPerson() {
		if (this.person == null)
			return "";
		else
			return this.person;
	}
	
	/**
	 * Returns notes attribute
	 * Returns an empty string if null
	 *
	 * @return A text where the salesperson can annotate about the client
	 */
	public String getNotes() {
		if (this.notes == null)
			return "";
		else
			return this.notes;
	}
	
	/**
	 * Returns true if the client is
	 * active (Can have new budgets)
	 *
	 * @return True if the client can have new budgets
	 */
	public boolean isActive() {
		return this.isActive;
	}
	
	/**
	 * Returns true if the client is
	 * NOT active (CANNOT have new budgets)
	 *
	 * @return True if the client cannot have new budgets
	 */
	public boolean isInactive() {
		return !(this.isActive);
	}
	
	/**
	 * Returns salesperson attribute
	 *
	 * @return The salesperson assigned to the client
	 */
	public Salesperson getSalesperson() {
		return this.salesperson;
	}

	/**
	 * Returns a File with the image of the client
	 *
	 * @return A file with the image of the client
	 */
	public File getImage() {
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.get(FileType.CLIENTIMAGE, this.getClientId() + ".png");
		if (file == null)
			file = fileDAO.get(FileType.CLIENTIMAGE, "default.png");
		return file;
	}
	
	/**
	 * Returns a File with the image of the client
	 * in a reduced size
	 *
	 * @return A file with the image of the client in a reduced size
	 */
	public File getMiniImage() {
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.get(FileType.CLIENTMINIIMAGE, this.getClientId() + ".png");
		if (file == null)
			file = fileDAO.get(FileType.CLIENTMINIIMAGE, "default.png");
		return file;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		if (this.getSalesperson() != null)
			return this.getClientNumber() + ": " + this.getName() + ", " + 
					this.getEmail()+ ". " + this.getPerson() + ": " + this.getPhone() 
					+ " - " + this.getAddress() + " " + this.getPostalCode() + " " + 
					this.getTown() + " - " + this.getProvince()  + " (" + this.getCountry()
					+ "). " + this.getNotes() + ". " + this.getSalesperson().toString();
		else
			return this.getClientNumber() + ": " + this.getName() + ", " + 
				this.getEmail()+ ". " + this.getPerson() + ": " + this.getPhone() 
				+ " - " + this.getAddress() + " " + this.getPostalCode() + " " + 
				this.getTown() + " - " + this.getProvince()  + " (" + this.getCountry()
				+ "). " + this.getNotes() + ". ";
	}

}

