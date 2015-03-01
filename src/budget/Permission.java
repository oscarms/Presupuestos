/**
 * Budget application specific objects
 */
package budget;

/**
 * This class represents the different types
 * of permissions that a user can have assigned.
 * A user can have 0, 1 or n permissions assigned.
 * 
 * @author oscar
 */
public enum Permission {

	/*
	 * Permissions for Salespeople
	 */
	ADMINISTRATE,
	// Edit products, Edit salespeople, Sign offers
	VIEWOFFERS,
	CREATEOFFERS,
	ALLCLIENTS;
	// View clients not assigned to him, and create budgets to them
	
}
