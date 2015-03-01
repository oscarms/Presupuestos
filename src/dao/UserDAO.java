/**
 * Data Access Objects
 */
package dao;

import budget.Client;
import budget.Permission;
import budget.Salesperson;
import budget.User;

/**
 * This class provides the specification of the methods
 * available to create, get and update Users, Clients
 * and Salespeople. It is implemented in UserDB.
 * 
 * @author oscar
 */
public interface UserDAO {

	/*
	 * Attributes
	 */
	/**
	 * Maximum results that the class can give in
	 * an array. If an array returned by this
	 * class has more length than MAXRESULTS,
	 * there are results not given.
	 */
	public static final int MAXRESULTS = 50;
	
	
	/* Get one object */
	
	/**
	 * Retrieves the User with the specified
	 * email from the database, null if not exists.
	 * 
	 * @param email The email of the user
	 * @param encryptedPassword The password of the user already encrypted
	 * @return The user, or null if not exists
	 */
	public User getUser(String email, String encryptedPassword);
	
	/**
	 * Retrieves the Salesperson with the specified
	 * id from the database, null if not exists.
	 * 
	 * @param salespersonId The unique ID of the salesperson
	 * @return The salesperson
	 */
	public Salesperson getSalesperson(int salespersonId);
	
	/**
	 * Retrieves the Client with the specified
	 * id from the database, null if not exists.
	 * 
	 * @param clientId The unique ID of the client
	 * @return The client
	 */
	public Client getClient(int clientId);
	
	
	/* Get an array for searching or listing */
	
	/**
	 * Retrieves the clients sorted by name (up to a max,
	 * if the array returned is max there may be results
	 * not listed) assigned to a salesperson with the
	 * specified search criteria. The filter search if every
	 * word introduced by the user is contained in any of the
	 * following: The client number, address, town, province,
	 * country, postalCode, name, email, phone, person or notes,
	 * or the name or email of his assigned salesperson.
	 *
	 * @param salespersonId The id of the salesperson assigned to the clients
	 * @param inactives If true, will include the old clients
	 * @param filter The words that the clients should include
	 * @return A list of clients
	 */
	public Client[] getClients(int salespersonId, boolean inactives,
			String filter);
	
	/**
	 * Retrieves the clients sorted by name (up to a max,
	 * if the array returned is max there may be results
	 * not listed) assigned (if not others) or not (if others,
	 * this only should be used if the user can view all clients)
	 * to a salesperson with the specified search criteria. The
	 * filter search if every word introduced by the user is
	 * contained in any of the following: The client number,
	 * address, town, province, country, postalCode, name, email,
	 * phone, person or notes, or the name or email of his
	 * assigned salesperson.
	 * 
	 * @param salespersonId The id of the salesperson
	 * @param others If others, the salesperson is not the assigned to the clients
	 * @param inactives If true, will include the old clients
	 * @param filter The words that the clients should include
	 * @return A list of clients
	 */
	public Client[] getClients(int salespersonId, boolean others,
			boolean inactives, String filter);

	/**
	 * Retrieves all the salespeople. This only should be
	 * used if the user is an administrator.
	 *
	 * @return A list of salespeople
	 */
	public Salesperson[] getSalespeople();
	
	
	/* Create or update clients */
	
	/**
	 * Inserts the client in the database and returns
	 * his Id 
	 * 
	 * @param client The client to be inserted
	 * @return The unique clientId
	 */
	public int create(Client client);
	
	/**
	 * Updates the client with the same Id and returns true if
	 * it completes the operation.
	 * 
	 * @param client The client to be updated
	 * @return True if it is updated
	 */
	public boolean update(Client client);
	
	/**
	 * Updates the client with the salesperson and returns true
	 * if it completes the operation
	 * 
	 * @param clientId The unique ID of the client
	 * @param salespersonId The unique ID of the salesperson
	 * @return True if it is assigned
	 */
	public boolean setSalesperson(int clientId, int salespersonId);
	
	
	/* Create or update salespeople or users */
	
	/**
	 * Inserts the salesperson in the database and returns
	 * his Id 
	 * 
	 * @param salesperson The salesperson to be inserted
	 * @return The unique ID of the salesperson
	 */
	public int create(Salesperson salesperson);
	
	/**
	 * Updates the salesperson with the same Id and returns true
	 * if it completes the operation.
	 * 
	 * @param salesperson The salesperson to be updated
	 * @return True if the salesperson is updated
	 */
	public boolean update(Salesperson salesperson);
	
	/**
	 * Updates the salesperson with the new e-mail password and
	 * returns true if it completes the operation
	 * 
	 * @param salespersonId The unique ID of the salesperson
	 * @param mailPassword The new mail password
	 * @return True if the password is updated
	 */
	public boolean setMailPassword(int salespersonId, String mailPassword);
	
	/**
	 * Updates the user with the new password and returns
	 * true if it completes the operation
	 * 
	 * @param userId The unique ID of the user
	 * @param newPassword The new password already encrypted
	 * @return True if the password is updated
	 */
	public boolean setPassword(int userId, String newPassword);
	
	/**
	 * Updates the user setting it as enabled or not and returns
	 * true if it completes the operation
	 * 
	 * @param userId The unique ID of the user
	 * @param enabled True if it is allowed to sign in
	 * @return True if the user is updated
	 */
	public boolean setUserEnabled(int userId, boolean enabled);
	
	/**
	 * Updates the user setting it as enabled or not and returns
	 * true if it completes the operation
	 * 
	 * @param email The email of the user
	 * @param enabled True if it is allowed to sign in
	 * @return True if the user is updated
	 */
	public boolean setUserEnabled(String email, boolean enabled);
	
	/**
	 * Updates the user permissions with the ones introduced and
	 * returns true if it completes the operation
	 * 
	 * @param userId The unique ID of the user
	 * @param permissions A list with the permissions assigned to the user
	 * @return True if the user is updated
	 */
	public boolean setPermissions(int userId, Permission[] permissions);
	
}
