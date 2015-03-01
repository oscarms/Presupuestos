/**
 * Budget application specific objects
 */
package budget;

/**
 * This class represents a Salesperson as the person
 * that can access to the system and create budgets
 * to the clients. It inherits the methods and attributes
 * of the class User. It is created from the data in the
 * database or introduced in a form. The object contains
 * basic information about the person, application
 * access and permissions.
 * 
 * @author oscar
 */
public class Salesperson extends User {

	/*
	 * Attributes
	 */
	private String emailPassword;
	// will be used for sending emails in the name of the Salesperson
	
	/**
	 * Inherit constructor
	 *
	 * @param id The unique ID of the salesperson
	 * @param email The email address
	 * @param name The name
	 * @param isEnabled If is enabled, can sign in the application
	 * @param permissions A list of permissions assigned
	 */
	public Salesperson(int id, String email, String name, boolean isEnabled,
			Permission[] permissions) {
		this(id, email, name, isEnabled, permissions, "");
	}
	
	/**
	 * Constructor to be used when editing salesperson
	 * or sending an email in his name
	 *
	 * @param id The unique ID of the salesperson
	 * @param email The email address
	 * @param name The name
	 * @param isEnabled If is enabled, can sign in the application
	 * @param permissions A list of permissions assigned
	 * @param emailPassword The password of the email
	 */
	public Salesperson(int id, String email, String name, boolean isEnabled,
			Permission[] permissions, String emailPassword) {
		super(id, email, name, isEnabled, permissions);
		this.emailPassword = emailPassword;
	}
	
	/**
	 * Returns emailPassword attribute
	 * Returns an empty string if null
	 *
	 * @return The password of the email
	 */
	public String getEmailPassword() {
		if (this.emailPassword == null)
			return "";
		else
			return this.emailPassword;
	}
	
	/**
	 * Returns true if there is an email
	 * password and is not empty
	 *
	 * @return True if the salesperson can send emails in his name
	 */
	public boolean hasEmailPassword() {
		return ( this.emailPassword != null &&
				!(emailPassword.equals("")) );
	}
	
	/* (non-Javadoc)
	 * @see budget.User#getUserType()
	 */
	@Override
	public UserType getUserType() {
		return UserType.SALESPERSON;
	}
	

}
