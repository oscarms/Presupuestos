/**
 * Budget application specific objects
 */
package budget;

/**
 * This class represents a User as a person that can access to
 * the system. Must be specialized as a Salesperson (in the future
 * also Client). It is created from the data in the database
 * or introduced in a form. The object contains basic information
 * about the person, application access and permissions.
 * 
 * @author oscar
 */
public abstract class User {
	
	/*
	 * Attributes
	 */
	private int id; // must be unique and be equal to a ID of a salesperson or client
	private String email; // person email
	private String name; // person name
	private boolean isEnabled; // true if the user can access the system
	private Permission[] permissions; // list of assigned permissions
	
	/**
	 * Constructor
	 *
	 * @param id The unique ID of the user
	 * @param email The email address
	 * @param name The name
	 * @param isEnabled True if the user is able to sign in the application
	 * @param permissions The list of permissions assigned to the user
	 */
	public User(int id, String email, String name,
			boolean isEnabled, Permission[] permissions) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.isEnabled = isEnabled;
		this.permissions = permissions;
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
	 * Returns name attribute
	 * Returns an empty string if null
	 *
	 * @return The name
	 */
	public String getName() {
		if (this.name == null)
			return "";
		else
			return this.name;
	}
	
	/**
	 * Returns id attribute
	 *
	 * @return The unique ID of the user
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns isEnabled attribute
	 * Will be true if the user is allowed
	 * to log in to the system
	 *
	 * @return True if the user is able to sign in the application
	 */
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	/**
	 * Returns permissions array
	 * Returns a empty array if null
	 *
	 * @return The list of permissions assigned to the user
	 */
	public Permission[] getPermissions() {
		if (this.permissions == null)
			return new Permission[0];
		else
			return this.permissions;
	}
	
	/**
	 * Returns true if the user has
	 * the specified permission
	 *
	 * @param permission
	 * @return True if the user has assigned the permission
	 */
	public boolean hasPermission(Permission permission) {
		if (this.permissions == null || permissions.length == 0)
			return false;
		
		for (Permission perm : permissions) {
			if ( perm.equals(permission) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Must be overwrited by the specialited class
	 * Will return the UserType
	 * (SALESPERSON, CLIENT in the future...)
	 *
	 * @return The type of user
	 */
	public UserType getUserType() {
		return null;
	}
	
	
	/**
	 * Two users are equal if they
	 * have the same id
	 */
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ( this.getId() == ((User)obj).getId() );
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return this.getName() + ", " + this.getEmail();
	}

}
