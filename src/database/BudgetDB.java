/**
 * Database related objects
 */
package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import budget.Annotation;
import budget.Attachment;
import budget.Budget;
import budget.Client;
import budget.Cover;
import budget.Document;
import budget.Permission;
import budget.Product;
import budget.Salesperson;
import budget.Section;
import budget.SectionProduct;
import budget.User;
import dao.BudgetDAO;
import dao.ProductDAO;

/**
 * This class provides the implementation of the methods
 * available to create, get and update Budgets, Sections,
 * SectionProducts, Annotations, Documents, Attachments
 * and Covers. Methods are specified in BudgetDAO.
 * It uses DBConnection.
 * 
 * @author oscar
 */
public class BudgetDB implements BudgetDAO {

	/*
	 * Attribute 
	 * 	BudgetDAO.MAXRESULTS
	 * 	BudgetDAO.DEFAULT_TAXRATE
	 * 	BudgetDAO.DEFAULT_NOTE
	 */
	
	/**
	 * Constructor
	 */
	public BudgetDB() {
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getBudget(java.lang.String)
	 */
	@Override
	public Budget getBudget(String budgetId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		Budget budget = null;
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "WHERE Budget.id = ? "
					);
			
			/*
			SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled,
					c.number c_number, c.address c_address, c.town c_town, c.province c_province,
					c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email,
					c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive,
					c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name,
					cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled	
			FROM Budget
			LEFT JOIN User a
			ON Budget.author = a.id
			LEFT JOIN Client c
			ON Budget.client = c.id
			LEFT JOIN User cs
			ON c.salesperson = cs.id
			LEFT JOIN User cu
			ON Budget.client = cu.id
			LEFT JOIN User s
			ON Budget.signer = s.id
			WHERE creationDate > 0 AND ( (isOffer=true AND signer IS NOT NULL) OR isOffer=false) AND Budget.id="BUD120ADS"
			*/
			
			pstmt.setString(1, budgetId);
			
			result = pstmt.executeQuery();

			if (result.next()) {
				budget = budgetFromResult(result);
				
				if (result.next()) // more than 1 result
					budget = null;
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return budget;
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getBudgets(int, long, long, java.lang.String, boolean)
	 */
	@Override
	public Budget[] getBudgets(int salespersonId, long from, long to,
			String filter, boolean expired) {
		return this.getBudgets(salespersonId, false, from, to, filter, expired);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getBudgets(int, boolean, long, long, java.lang.String, boolean)
	 */
	@Override
	public Budget[] getBudgets(int salespersonId, boolean others, long from,
			long to, String filter, boolean expired) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "WHERE creationDate > 0 AND cs.id "+ (others ? "!=" : "=") +" ? "
					//+ "		AND (CASE WHEN cs.viewOffers = true THEN ( isOffer=true AND signer IS NOT NULL) ELSE (isOffer=false) END ) "
					+ "		AND (CASE WHEN 1=(SELECT count(sp.id) FROM User sp WHERE sp.id = ? AND sp.viewOffers = true) THEN ( (isOffer=true AND signer IS NOT NULL) OR isOffer=false) ELSE (isOffer=false) END ) "
					+ 		(!expired ? "AND expirationDate >= " + Long.toString(System.currentTimeMillis()) + " " : "")
					+		(from > 0 ? "AND creationDate >= " + Long.toString(from) + " " : "")
					+		(to > 0 ? "AND creationDate <= " + Long.toString(to) + " " : "")
					+ "ORDER BY Budget.creationDate DESC "
					);
						
			/*
			SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled,
					c.number c_number, c.address c_address, c.town c_town, c.province c_province,
					c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email,
					c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive,
					c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name,
					cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled	
			FROM Budget
			LEFT JOIN User a
			ON Budget.author = a.id
			LEFT JOIN Client c
			ON Budget.client = c.id
			LEFT JOIN User cs
			ON c.salesperson = cs.id
			LEFT JOIN User cu
			ON Budget.client = cu.id
			LEFT JOIN User s
			ON Budget.signer = s.id
			WHERE creationDate > 0 AND ( (isOffer=true AND signer IS NOT NULL) OR isOffer=false) "
			*/
			
			pstmt.setInt(1, salespersonId);
			pstmt.setInt(2, salespersonId);
			
			result = pstmt.executeQuery();
			
			Budget budget = null;
			boolean found;
			
			//Normalizamos en la forma NFD (Canonical decomposition)
			filter = Normalizer.normalize(filter, Normalizer.Form.NFD);
			//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
			filter = filter.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			// Create list of words in the filter
			String[] filterwords = filter.split(" ");
			
			String budgetString;
			while (result.next() && budgets.size() < MAXRESULTS) {
	
				budget = budgetFromResult(result);
				
				// check if contains all the words in the filter, and add
				found = true;
				for (String word : filterwords) {
					budgetString = budget.toString();
					//Normalizamos en la forma NFD (Canonical decomposition)
					budgetString = Normalizer.normalize(budgetString, Normalizer.Form.NFD);
					//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
					budgetString = budgetString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
					if (!budgetString.toLowerCase().contains(word.toLowerCase()))
						found = false;
				}
				
				if (found)
					budgets.add(budget);
				
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNotSignedOffers(int)
	 */
	@Override
	public Budget[] getNotSignedOffers(int salespersonId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "WHERE creationDate > 0 AND isOffer=true AND signer IS NULL AND cs.id = ? "
					+ "		AND (CASE WHEN cs.viewOffers = true THEN ( isOffer=true AND signer IS NULL) ELSE (isOffer=false) END ) "
					+ "ORDER BY Budget.creationDate DESC "
					);
	
			pstmt.setInt(1, salespersonId);
			result = pstmt.executeQuery();
						
			while (result.next()) {
				budgets.add(budgetFromResult(result));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget in getNotSignedOffers: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNotSignedOffers()
	 */
	@Override
	public Budget[] getNotSignedOffers() {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "WHERE creationDate > 0 AND isOffer=true AND signer IS NULL "
					+ "ORDER BY Budget.creationDate DESC "
					);
	
			result = pstmt.executeQuery();
						
			while (result.next()) {
				budgets.add(budgetFromResult(result));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget in getNotSignedOffers: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getIncompleteBudgets(int)
	 */
	@Override
	public Budget[] getIncompleteBudgets(int authorId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "WHERE (creationDate IS NULL OR creationDate <= 0) AND author = ? "
					+ 			"AND (Budget.isOffer = false OR a.viewOffers = true)"
					+ "ORDER BY Budget.creationDate DESC "
					);
	
			pstmt.setInt(1, authorId);
			result = pstmt.executeQuery();
						
			while (result.next()) {
				budgets.add(budgetFromResult(result));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget in getIncompleteBudgets(int): " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNewBudgets(int, boolean)
	 */
	@Override
	public Budget[] getNewBudgets(int salespersonId, boolean others) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "INNER JOIN Notification n "
					+ "ON n.budget = Budget.id "
					+ "WHERE n.user = ? AND isOffer = false AND c.salesperson " + (others ? "!=" : "=") + " ? "
					+ "ORDER BY Budget.creationDate DESC "
					);
	
			pstmt.setInt(1, salespersonId);
			pstmt.setInt(2, salespersonId);
			result = pstmt.executeQuery();
						
			while (result.next()) {
				budgets.add(budgetFromResult(result));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget in getNewBudgets: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}
		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNewBudgets(int)
	 */
	@Override
	public Budget[] getNewBudgets(int salespersonId) {
		return this.getNewBudgets(salespersonId, false);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNewSignedOffers(int)
	 */
	@Override
	public Budget[] getNewSignedOffers(int salespersonId) {
		return this.getNewSignedOffers(salespersonId, false);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNewSignedOffers(int, boolean)
	 */
	@Override
	public Budget[] getNewSignedOffers(int salespersonId, boolean others) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "INNER JOIN Notification n "
					+ "ON n.budget = Budget.id "
					+ "WHERE n.user = ? AND isOffer = true AND c.salesperson " + (others ? "!=" : "=") + " ? "
					+ 		(!others ? "AND cs.viewOffers = true " : "")
					+ "ORDER BY Budget.creationDate DESC "
					);
	
			pstmt.setInt(1, salespersonId);
			pstmt.setInt(2, salespersonId);
			result = pstmt.executeQuery();
						
			while (result.next()) {
				budgets.add(budgetFromResult(result));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving budget in getNewSignedOffers: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}
		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getNotificationCount(int)
	 */
	@Override
	public int getNotificationCount(int salespersonId) {

		// Count notifications of the user (include offers if the user can viewoffers)
		// Count incomplete budgets of the user as author
		// Count offers not yet signed if the user is an administrator
		
		/*
		 * 29 is salespersonId
		SELECT (
		SELECT count(Notification.budget) num
		FROM Notification
		INNER JOIN User
		ON Notification.user = User.id
		INNER JOIN Budget
		ON Notification.budget = Budget.id
		WHERE (Budget.isOffer = false OR User.viewOffers = true)
		AND (User.allClients = true OR salesperson.id = User.id)
		) + (
		SELECT count(Budget.id) num
		FROM Budget
		INNER JOIN User
		ON Budget.author = User.id 
		INNER JOIN Client client
		ON Budget.client = client.id
		INNER JOIN User salesperson
		ON client.salesperson = salesperson.id
		WHERE Budget.author = 29
			AND CASE WHEN (User.viewOffers) THEN (Budget.creationDate is NULL)
			ELSE (Budget.isOffer = false && Budget.creationDate is NULL) END
		) + (
		SELECT CASE WHEN (SELECT administrate FROM User WHERE id = 29) THEN count(id) ELSE 0 END
		FROM Budget
		WHERE creationDate > 0 AND isOffer=true AND signer IS NULL
		) AS num
		 */
		
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int count = 0;
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT ( "
					+ "		SELECT count(Notification.budget) num "
					+ "		FROM Notification "
					+ "		INNER JOIN User "
					+ "		ON Notification.user = User.id "
					+ "		INNER JOIN Budget "
					+ "		ON Notification.budget = Budget.id "
					+ "		INNER JOIN Client client "
					+ "		ON Budget.client = client.id "
					+ "		INNER JOIN User salesperson "
					+ "		ON client.salesperson = salesperson.id "
					+ "		WHERE (Budget.isOffer = false OR User.viewOffers = true) AND Notification.user = ? "
					+ "		AND (User.allClients = true OR salesperson.id = User.id) "
					+ ""
					+ "		) + ( "
					+ ""
					+ "		SELECT count(Budget.id) num "
					+ "		FROM Budget "
					+ "		INNER JOIN User "
					+ "		ON Budget.author = User.id "
					+ "		WHERE Budget.author = ? "
					+ "			AND CASE WHEN (User.viewOffers) THEN (Budget.creationDate is NULL) "
					+ "			ELSE (Budget.isOffer = false && Budget.creationDate is NULL) END "
					+ ""
					+ "		) + ( "
					+ ""
					+ "		SELECT CASE WHEN (SELECT administrate FROM User WHERE id = ?) THEN count(id) ELSE 0 END "
					+ "		FROM Budget "
					+ "		WHERE creationDate > 0 AND isOffer=true AND signer IS NULL "
					+ ""
					+ ") AS num "
				);
	
			pstmt.setInt(1, salespersonId);
			pstmt.setInt(2, salespersonId);
			pstmt.setInt(3, salespersonId);
			result = pstmt.executeQuery();
						
			if (result.next()) {
				count = result.getInt("num");
			}
			
			result.close();
			pstmt.close();
			return count;

		} catch (SQLException e) {
			System.err.println("Error retrieving count of notifications: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#removeNotification(int, java.lang.String)
	 */
	@Override
	public boolean removeNotification(int salespersonId, String budgetId) {
		PreparedStatement pstmt = null;
		
		// sql delete
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "DELETE FROM Notification "
					+ "WHERE user = ? AND budget = ? ");
	
			pstmt.setInt(1, salespersonId);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error removing notification, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error removing notification: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#addNotification(int, java.lang.String)
	 */
	@Override
	public boolean addNotification(int salespersonId, String budgetId) {
		
		// sql insert
		PreparedStatement pstmt = null;
		try {
			// insert into Notification table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Notification (user, budget) "
					+ "VALUES (?, ?)"
					);
			pstmt.setInt(1, salespersonId);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating notification, no rows affected.");
			}

			pstmt.close();
			
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error creating notification: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#addNotification(int, java.lang.String, boolean)
	 */
	@Override
	public boolean addNotification(int salespersonId, String budgetId,
			boolean otherAdministrators) {
		
		if (!otherAdministrators) {
			return this.addNotification(salespersonId, budgetId);
		} else {
			/*
			INSERT INTO Notification(user,budget)
			SELECT id user, '123' FROM User WHERE User.administrate = true AND User.id != 12
			 */
			// For each administrator but the specified one, add notification
			
			// sql insert
			PreparedStatement pstmt = null;
			try {
				// insert into Notification table
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "INSERT INTO Notification (user, budget) "
						+ "SELECT id, ? FROM User WHERE User.administrate = true AND User.id != ? "
						);
				pstmt.setInt(2, salespersonId);
				pstmt.setString(1, budgetId);
				
				pstmt.executeUpdate();

				pstmt.close();
				
				return true;
				
			} catch (SQLException e) {
				System.err.println("Error creating notifications: " + e.getMessage());
				if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
				return false;
			}
			
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#addProduct(java.lang.String, int, int)
	 */
	@Override
	public boolean addProduct(String budgetId, int sectionId, int productId) {
		
		// sql insert
		PreparedStatement pstmt = null;
		try {
			/*
			INSERT INTO SectionProduct (budget, section, product, quantity, position)
			SELECT 123, 456, 9000, 1, 
			CASE WHEN max(position) IS NULL THEN 1 ELSE max(position)+1 END 
			FROM SectionProduct WHERE budget = 123 AND section = 456;
			 */
			// insert into SectionProduct table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO SectionProduct (budget, section, product, quantity, position) "
					+ "SELECT ?, ?, ?, 1, "
					+ "CASE WHEN max(position) IS NULL THEN 1 ELSE max(position)+1 END "
					+ "FROM SectionProduct WHERE budget = ? AND section = ?; "
					);
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, sectionId);
			pstmt.setInt(3, productId);
			pstmt.setString(4, budgetId);
			pstmt.setInt(5, sectionId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating product in section, no rows affected.");
			}

			pstmt.close();
			
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error creating product in section: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#removeProduct(java.lang.String, int, int)
	 */
	@Override
	public boolean removeProduct(String budgetId, int sectionId, int productId) {
		PreparedStatement pstmt = null;
		
		// sql delete
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "DELETE FROM SectionProduct "
					+ "WHERE budget = ? AND section = ? AND product = ? ");
	
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, sectionId);
			pstmt.setInt(3, productId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error removing product in section, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error removing product in section: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#sortProducts(java.lang.String, int, int, boolean)
	 */
	@Override
	public boolean sortProduct(String budgetId, int sectionId, int productId,
			boolean after) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		
		try {
			int oldPosition;
			int newPosition;
			int otherProduct;
			// retrieve the current position
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT position FROM SectionProduct WHERE budget = ? AND section = ? AND product = ? "
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, sectionId);
			pstmt.setInt(3, productId);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				oldPosition = result.getInt("position");
				result.close();
				pstmt.clearParameters();
				pstmt.close();
			} else {
				throw new SQLException("Error sorting products in section: product in section not exists.");
			}
			
			
			// retrieve the id and position of the next / previus element.
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT product,position "
					+ "FROM SectionProduct "
					+ "WHERE budget = ? AND section = ? AND position " + (after ? ">" : "<") + " ? "
					+ "ORDER BY position "+ (after ? "ASC" : "DESC") +" "
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, sectionId);
			pstmt.setInt(3, oldPosition);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				newPosition = result.getInt("position");
				otherProduct = result.getInt("product");
				result.close();
				pstmt.clearParameters();
				pstmt.close();
			} else {
				throw new SQLException("Error sorting products in section: other product in section not exists.");
			}
			
			
			// update the section to the new position
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE SectionProduct "
					+ "SET position=? "
					+ "WHERE budget = ? AND section=? AND product=? "
					);
			
			pstmt.setInt(1, newPosition);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, sectionId);
			pstmt.setInt(4, productId);
			pstmt.addBatch();
			
			// update the position of the other element to the new position
			pstmt.setInt(1, oldPosition);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, sectionId);
			pstmt.setInt(4, otherProduct);
			pstmt.addBatch();
			
			int[] results = pstmt.executeBatch();
			if (results[0]<1 || results[1]<0) {
				throw new SQLException("Error sorting products in section while updating");
			}
			
			result.close();
			pstmt.close();
			return true;

		} catch (SQLException e) {
			System.err.println("Error sorting products in section: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setQuantity(java.lang.String, int, int, float)
	 */
	@Override
	public boolean setQuantity(String budgetId, int sectionId, int productId,
			float quantity) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in SectionProduct table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE SectionProduct "
					+ "SET quantity=? "
					+ "WHERE budget=? AND section=? AND product=? ");
			pstmt.setFloat(1, quantity);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, sectionId);
			pstmt.setInt(4, productId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting quantity of product, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting quantity of product: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setDiscounts(java.lang.String, int, int, float, float, float)
	 */
	@Override
	public boolean setDiscounts(String budgetId, int sectionId, int productId,
			float discount1, float discount2, float discount3) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in SectionProduct table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE SectionProduct "
					+ "SET discount1=?, discount2=?, discount3=? "
					+ "WHERE budget=? AND section=? AND product=? ");
			pstmt.setFloat(1, discount1);
			pstmt.setFloat(2, discount2);
			pstmt.setFloat(3, discount3);
			pstmt.setString(4, budgetId);
			pstmt.setInt(5, sectionId);
			pstmt.setInt(6, productId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting discounts, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting discounts: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getSections(java.lang.String)
	 */
	@Override
	public Section[] getSections(String budgetId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Section> sections = new ArrayList<Section>();
		
		// Query db
		try {
			/*
			SELECT Section.id sectionId, Section.name sectionName, SectionProduct.product,
				Product.name, Product.description, Product.costPrice, SectionProduct.quantity,
				SectionProduct.discount1, SectionProduct.discount2, SectionProduct.discount3
			FROM Section
			LEFT JOIN SectionProduct
			ON Section.id = SectionProduct.section AND Section.budget = SectionProduct.budget
			LEFT JOIN Product
			ON SectionProduct.product = Product.id
			WHERE Section.budget = 0
			ORDER BY Section.position, SectionProduct.position
			 */
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Section.id sectionId, Section.name sectionName, SectionProduct.product, "
					+ "		SectionProduct.quantity, SectionProduct.discount1, SectionProduct.discount2, "
					+ "		SectionProduct.discount3 "
					+ "FROM Section "
					+ "LEFT JOIN SectionProduct "
					+ "ON Section.id = SectionProduct.section AND Section.budget = SectionProduct.budget "
					+ "WHERE Section.budget = ? "
					+ "ORDER BY Section.position, SectionProduct.position "
					);

			pstmt.setString(1, budgetId);
			result = pstmt.executeQuery();
			
			int sectionId = -1;
			String name = null;
			List<SectionProduct> products = null; // to recognize first loop
			Product product; // uses ProductDAO
			ProductDAO productDAO = new ProductDB();
			while (result.next()) {
				if (products == null) {
					products = new ArrayList<SectionProduct>();
					sectionId = result.getInt("sectionId");
					name = result.getString("sectionName");
				} else if (sectionId != result.getInt("sectionId")) {
					sections.add(new Section(sectionId, name, 
							(SectionProduct[])products.toArray(new SectionProduct[products.size()])));
					products = new ArrayList<SectionProduct>();
					name = result.getString("sectionName");
					sectionId = result.getInt("sectionId");
				}
				if (result.getObject("product") != null) {
					product = productDAO.getProduct(result.getInt("product"));
					products.add( new SectionProduct(product.getProductId(), product.getName(),
							product.getDescription(), product.getPrices(), result.getFloat("quantity"),
							result.getFloat("discount1"), result.getFloat("discount2"),
							result.getFloat("discount3"), product.getCostPrice()) );
				}
			}
			if (products != null) // add the last iteration if has iterated
				sections.add(new Section(sectionId, name, 
						(SectionProduct[])products.toArray(new SectionProduct[products.size()])));
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving sections: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Section[])sections.toArray(new Section[sections.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#addSection(java.lang.String)
	 */
	@Override
	public int addSection(String budgetId) {
		return this.addSection(budgetId, "");
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#addSection(java.lang.String, java.lang.String)
	 */
	@Override
	public int addSection(String budgetId, String name) {
        
		// sql insert
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int id = -1;
		try {
			/*
			INSERT INTO Section (budget, id, position, name)
			SELECT "123", 
			CASE WHEN max(id) IS NULL THEN 1 ELSE max(id)+1 END, 
			CASE WHEN max(position) IS NULL THEN 1 ELSE max(position)+1 END, 
			"hola" 
			FROM Section WHERE budget = "123"
			 */
			// insert into Section table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Section (budget, position, name) "
					+ "SELECT ?, "
					+ "CASE WHEN max(position) IS NULL THEN 1 ELSE max(position)+1 END, "
					+ "? " 
					+ "FROM Section WHERE budget = ? "
					,PreparedStatement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, budgetId);
			pstmt.setString(2, name);
			pstmt.setString(3, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating section, no rows affected.");
			}

			result = pstmt.getGeneratedKeys();
	        if (result.next())
	            id = (int) result.getInt(1);
	        
			result.close();
			pstmt.close();
			
			return id;
			
		} catch (SQLException e) {
			System.err.println("Error creating section: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#removeSection(java.lang.String, int)
	 */
	@Override
	public boolean removeSection(String budgetId, int sectionId) {
		PreparedStatement pstmt = null;
		
		// sql delete
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "DELETE FROM Section "
					+ "WHERE id = ? AND budget = ? ");
	
			pstmt.setInt(1, sectionId);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error removing section, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error removing section: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#sortSection(java.lang.String, int, boolean)
	 */
	@Override
	public boolean sortSection(String budgetId, int sectionId, boolean after) {
		
		// retrieve the current position:
		/*
			SELECT position FROM Section WHERE budget = 123 AND id = 1 
		
			budgetId,sectionId -> oldPosition
		*/

		// retrieve the id and position of the next / previus element.
		// First result of (If null, return false and do not change anything):
		/*
				SELECT id,position
				FROM Section
				WHERE budget = 123 AND position > 2
				ORDER BY position ASC
				
				or
				
				SELECT *
				FROM Section
				WHERE budget = 123 AND position < 2
				ORDER BY position DESC
				
				budgetId,oldPosition -> otherSection,newPosition
		*/
		
		// update the current position to the new position
		/*
				UPDATE Section
				SET position=?
				WHERE budget = ? AND id=?
						
				sectionId,newPosition
		*/
		
		// update the position of the other element to the old position
		/*
				UPDATE Section
				SET position=?
				WHERE budget = ? AND id=?
						
				otherSection,oldPosition
		 */
		
		PreparedStatement pstmt = null;
		ResultSet result = null;
		
		try {
			int oldPosition;
			int newPosition;
			int otherSection;
			// retrieve the current position
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT position FROM Section WHERE budget = ? AND id = ? "
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, sectionId);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				oldPosition = result.getInt("position");
				result.close();
				pstmt.clearParameters();
				pstmt.close();
			} else {
				throw new SQLException("Error sorting sections: section not exists.");
			}
			
			
			// retrieve the id and position of the next / previus element.
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT id,position "
					+ "FROM Section "
					+ "WHERE budget = ? AND position " + (after ? ">" : "<") + " ? "
					+ "ORDER BY position "+ (after ? "ASC" : "DESC") +" "
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, oldPosition);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				newPosition = result.getInt("position");
				otherSection = result.getInt("id");
				result.close();
				pstmt.clearParameters();
				pstmt.close();
			} else {
				throw new SQLException("Error sorting sections: other section not exists.");
			}
			
			
			// update the section to the new position
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Section "
					+ "SET position=? "
					+ "WHERE budget = ? AND id=? "
					);
			
			pstmt.setInt(1, newPosition);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, sectionId);
			pstmt.addBatch();
			
			// update the position of the other element to the new position
			pstmt.setInt(1, oldPosition);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, otherSection);
			pstmt.addBatch();
			
			int[] results = pstmt.executeBatch();
			if (results[0]<1 || results[1]<0) {
				throw new SQLException("Error sorting sections while updating");
			}
			
			result.close();
			pstmt.close();
			return true;

		} catch (SQLException e) {
			System.err.println("Error sorting sections: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#renameSection(java.lang.String, int, java.lang.String)
	 */
	@Override
	public boolean renameSection(String budgetId, int sectionId, String name) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Section table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Section "
					+ "SET name=? "
					+ "WHERE budget=? AND id=? ");
			pstmt.setString(1, name);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, sectionId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting name of section, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting name of section: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getFamily(java.lang.String)
	 */
	@Override
	public Budget[] getFamily(String budgetId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Budget> budgets = new ArrayList<Budget>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Budget.*, a.email a_email, a.name a_name, a.isEnabled a_isEnabled, "
					+ "			c.number c_number, c.address c_address, c.town c_town, c.province c_province, "
					+ "			c.country c_country, c.postalcode c_postalCode, cu.name c_name, cu.email c_email, "
					+ "			c.phone c_phone, c.person c_person, c.notes c_notes, c.isActive c_isActive, "
					+ "			c.salesperson c_salespersonId, cs.email cs_email, cs.name cs_name, "
					+ "			cs.isEnabled cs_isEnabled, s.email s_email, s.name s_name, s.isEnabled s_isEnabled "
					+ "FROM Budget "
					+ "LEFT JOIN User a "
					+ "ON Budget.author = a.id "
					+ "LEFT JOIN Client c "
					+ "ON Budget.client = c.id "
					+ "LEFT JOIN User cs "
					+ "ON c.salesperson = cs.id "
					+ "LEFT JOIN User cu "
					+ "ON Budget.client = cu.id "
					+ "LEFT JOIN User s "
					+ "ON Budget.signer = s.id "
					+ "WHERE creationDate > 0 AND Budget.original = (SELECT original FROM Budget WHERE id = ?) "
					+ "ORDER BY Budget.creationDate DESC "
					);

			pstmt.setString(1, budgetId);
			
			result = pstmt.executeQuery();
			
			while (result.next() && budgets.size() < MAXRESULTS) {
					budgets.add(budgetFromResult(result));			
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving family of budget: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Budget[])budgets.toArray(new Budget[budgets.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getAnnotations(java.lang.String)
	 */
	@Override
	public Annotation[] getAnnotations(String budgetId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Annotation> annotations = new ArrayList<Annotation>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Annotation "
					+ "WHERE budget = ? "
					+ "ORDER BY date DESC "
					);

			pstmt.setString(1, budgetId);
			result = pstmt.executeQuery();
			
			while (result.next()) {
				annotations.add(new Annotation(result.getLong("date"), result.getString("text")));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving annotations: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Annotation[])annotations.toArray(new Annotation[annotations.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getDocuments(java.lang.String)
	 */
	@Override
	public Document[] getDocuments(String budgetId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Document> documents = new ArrayList<Document>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Document "
					+ "WHERE budget = ? "
					+ "ORDER BY position "
					);

			pstmt.setString(1, budgetId);
			result = pstmt.executeQuery();
			
			while (result.next()) {
				documents.add(new Document(result.getInt("id"), 
						result.getString("name"), result.getString("budget")));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving documents: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Document[])documents.toArray(new Document[documents.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getAttachments(java.lang.String)
	 */
	@Override
	public Attachment[] getAttachments(String budgetId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Attachment "
					+ "WHERE budget = ? "
					+ "ORDER BY position "
					);

			pstmt.setString(1, budgetId);
			result = pstmt.executeQuery();
			
			while (result.next()) {
				attachments.add(new Attachment(result.getInt("id"), 
						result.getString("name"), result.getString("budget")));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving attachments: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Attachment[])attachments.toArray(new Attachment[attachments.size()]);
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getDocument(java.lang.String, int)
	 */
	@Override
	public Document getDocument(String budgetId, int documentId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		Document document = null;
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Document "
					+ "WHERE budget = ? AND id = ? "
					);

			pstmt.setString(1, budgetId);
			pstmt.setInt(2, documentId);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				document = new Document(result.getInt("id"), 
						result.getString("name"), result.getString("budget"));
				
				if (result.next()) // more than 1 result
					document = null;
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving document: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return document;
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getAttachment(java.lang.String, int)
	 */
	@Override
	public Attachment getAttachment(String budgetId, int attachmentId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		Attachment attachment = null;
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Attachment "
					+ "WHERE budget = ? AND id = ? "
					);

			pstmt.setString(1, budgetId);
			pstmt.setInt(2, attachmentId);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				attachment = new Attachment(result.getInt("id"), 
						result.getString("name"), result.getString("budget"));
				
				if (result.next()) // more than 1 result
					attachment = null;
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving attachment: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return attachment;
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getCovers()
	 */
	@Override
	public Cover[] getCovers() {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		List<Cover> covers = new ArrayList<Cover>();
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Cover "
					);

			result = pstmt.executeQuery();
			
			while (result.next()) {
				covers.add(new Cover(result.getInt("id")));
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving covers: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return (Cover[])covers.toArray(new Cover[covers.size()]);
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#getCover(int)
	 */
	@Override
	public Cover getCover(int coverId) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		Cover cover = null;
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT * "
					+ "FROM Cover "
					+ "WHERE id = ? "
					);

			pstmt.setInt(1, coverId);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				cover = new Cover(result.getInt("id"));
				
				if (result.next()) // more than 1 result
					cover = null;
			}
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving cover: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return cover;
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#create(budget.Cover)
	 */
	@Override
	public int create(Cover cover) {
		// the cover in parameter has no ID (is not created)
		
		// sql insert
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int id = -1;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Cover (id) "
					+ "VALUES (DEFAULT) "
					,PreparedStatement.RETURN_GENERATED_KEYS);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating cover, no rows affected.");
			}

			result = pstmt.getGeneratedKeys();
	        if (result.next())
	            id = (int) result.getInt(1);
	            
	        result.close();
			pstmt.close();
			
			return id;
			
		} catch (SQLException e) {
			System.err.println("Error creating cover: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#removeCover(int)
	 */
	@Override
	public boolean removeCover(int coverId) {
		PreparedStatement pstmt = null;
		
		// sql delete
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "DELETE FROM Cover "
					+ "WHERE id = ? ");
	
			pstmt.setInt(1, coverId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error removing cover, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error removing cover: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setCover(java.lang.String, int)
	 */
	@Override
	public boolean setCover(String budgetId, int coverId) {
		// sql update
		PreparedStatement pstmt = null;
		if (coverId >= 0)
			try {
				// update in Budget table
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "UPDATE Budget "
						+ "SET cover=? "
						+ "WHERE id=? ");
				pstmt.setInt(1, coverId);
				pstmt.setString(2, budgetId);
				
				if (pstmt.executeUpdate() < 1) {
					throw new SQLException("Error setting cover of budget, no rows affected.");
				}
							
				pstmt.close();
				return true;
				
			} catch (SQLException e) {
				System.err.println("Error setting cover of budget: " + e.getMessage());
				if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
				return false;
			}
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#create(java.lang.String, budget.Annotation)
	 */
	@Override
	public boolean create(String budgetId, Annotation annotation) {
        
		PreparedStatement pstmt = null;
		try {
			// insert into Annotation table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO ANNOTATION (date, budget, text) "
					+ "VALUES (?, ?, ?) "
					);
			pstmt.setString(2, budgetId);
			pstmt.setLong(1, annotation.getDate());
			pstmt.setString(3, annotation.getText());
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating annotation, no rows affected.");
			}

			pstmt.close();
			
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error creating annotation: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#create(java.lang.String, budget.Document)
	 */
	@Override
	public int create(String budgetId, Document document) {
		
		// sql insert
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int id = -1;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Document (budget, position, name) "
					+ "SELECT ?,"
					+ "CASE WHEN max(position) IS NULL THEN 1 ELSE max(position)+1 END "
					+ " , ? "
					+ "FROM Document WHERE budget = ? "
					,PreparedStatement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, budgetId);
			pstmt.setString(2, document.getName());
			pstmt.setString(3, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating document, no rows affected.");
			}

			result = pstmt.getGeneratedKeys();
	        if (result.next())
	            id = (int) result.getInt(1);
	            
	        result.close();
			pstmt.close();
			
			return id;
			
		} catch (SQLException e) {
			System.err.println("Error creating document: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#create(java.lang.String, budget.Attachment)
	 */
	@Override
	public int create(String budgetId, Attachment attachment) {
		
		// sql insert
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int id = -1;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Attachment (budget, position, name) "
					+ "SELECT ?, "
					+ "CASE WHEN max(position) IS NULL THEN 1 ELSE max(position)+1 END "
					+ ", ? "
					+ "FROM Attachment WHERE budget = ? "
					,PreparedStatement.RETURN_GENERATED_KEYS);
			
			pstmt.setString(1, budgetId);
			pstmt.setString(2, attachment.getName());
			pstmt.setString(3, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating attachment, no rows affected.");
			}

			result = pstmt.getGeneratedKeys();
	        if (result.next())
	            id = (int) result.getInt(1);
	            
	        result.close();
			pstmt.close();
			
			return id;
			
		} catch (SQLException e) {
			System.err.println("Error creating attachment: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#removeDocument(java.lang.String, int)
	 */
	@Override
	public boolean removeDocument(String budgetId, int documentId) {
		PreparedStatement pstmt = null;
		
		// sql delete
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "DELETE FROM Document "
					+ "WHERE id = ? AND budget = ? ");
	
			pstmt.setInt(1, documentId);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error removing document, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error removing document: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#removeAttachment(java.lang.String, int)
	 */
	@Override
	public boolean removeAttachment(String budgetId, int attachmentId) {
		PreparedStatement pstmt = null;
		
		// sql delete
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "DELETE FROM Attachment "
					+ "WHERE id = ? AND budget = ? ");
	
			pstmt.setInt(1, attachmentId);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error removing attachment, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error removing attachment: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see dao.BudgetDAO#sortAttachment(java.lang.String, int, boolean)
	 */
	@Override
	public boolean sortAttachment(String budgetId, int attachmentId, boolean after) {

		PreparedStatement pstmt = null;
		ResultSet result = null;
		
		try {
			int oldPosition;
			int newPosition;
			int otherAttachment;
			// retrieve the current position
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT position FROM Attachment WHERE budget = ? AND id = ? "
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, attachmentId);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				oldPosition = result.getInt("position");
				result.close();
				pstmt.clearParameters();
				pstmt.close();
			} else {
				throw new SQLException("Error sorting attachments: attachment not exists.");
			}
			
			
			// retrieve the id and position of the next / previus element.
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT id,position "
					+ "FROM Attachment "
					+ "WHERE budget = ? AND position " + (after ? ">" : "<") + " ? "
					+ "ORDER BY position "+ (after ? "ASC" : "DESC") +" "
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setInt(2, oldPosition);
			result = pstmt.executeQuery();
			
			if (result.next()) {
				newPosition = result.getInt("position");
				otherAttachment = result.getInt("id");
				result.close();
				pstmt.clearParameters();
				pstmt.close();
			} else {
				throw new SQLException("Error sorting attachments: other attachment not exists.");
			}
			
			
			// update the section to the new position
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Attachment "
					+ "SET position=? "
					+ "WHERE budget = ? AND id=? "
					);
			
			pstmt.setInt(1, newPosition);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, attachmentId);
			pstmt.addBatch();
			
			// update the position of the other element to the new position
			pstmt.setInt(1, oldPosition);
			pstmt.setString(2, budgetId);
			pstmt.setInt(3, otherAttachment);
			pstmt.addBatch();
			
			int[] results = pstmt.executeBatch();
			if (results[0]<1 || results[1]<0) {
				throw new SQLException("Error sorting attachments while updating");
			}
			
			result.close();
			pstmt.close();
			return true;

		} catch (SQLException e) {
			System.err.println("Error sorting attachments: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#createBudget(budget.User, boolean)
	 */
	@Override
	public String createBudget(User author, boolean isOffer) {
		return this.createBudget(author, null, isOffer);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#createBudget(budget.User)
	 */
	@Override
	public String createBudget(User author) {
		return this.createBudget(author, false);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#createBudget(budget.User, java.lang.String, boolean)
	 */
	@Override
	public String createBudget(User author, String budgetIdFrom, boolean isOffer) {
		
		PreparedStatement pstmt = null;
		ResultSet result = null;
		
		try {
			// generate budget id from parameters and existing budgets in db
			
			// retrieve count of budgets
			int budgetCount;
			
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT count(id)+1 FROM Budget ");
			
			result = pstmt.executeQuery();
			
			if (result.next())
				budgetCount = result.getInt(1);
			else
				throw new SQLException("Error creating budget while generating name");
			
			pstmt.clearParameters();
			result.close();
			pstmt.close();
			// generate name as F00000001JC for an offer or P00000002OM for a budget
			String budgetId = (isOffer ? "F" : "P") + String.format("%08d", budgetCount) 
					+ author.getName().substring(0,1) + ( author.getName().contains(" ") ? 
							author.getName().substring(author.getName().indexOf(" ")+1,
							author.getName().indexOf(" ")+2) : 
							author.getName().substring(0,2) ).toUpperCase();
			//Normalizamos en la forma NFD (Canonical decomposition)
			budgetId = Normalizer.normalize(budgetId, Normalizer.Form.NFD);
			//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
			budgetId = budgetId.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			
			// insert budget
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Budget (id, hasGlobalTotal, taxRate, isOffer, author, original, note) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?) " 
					);
			
			pstmt.setString(1, budgetId);
			pstmt.setBoolean(2, true);
			pstmt.setFloat(3, DEFAULT_TAXRATE);
			pstmt.setBoolean(4, isOffer);
			pstmt.setInt(5, author.getId());
			if (budgetIdFrom != null && budgetIdFrom.length() > 0)
				pstmt.setString(6, budgetIdFrom);
			else
				pstmt.setString(6, budgetId);
			pstmt.setString(7, DEFAULT_NOTE);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating budget, no rows affected.");
			}
	        
			pstmt.close();
			
			return budgetId;
			
		} catch (SQLException e) {
			System.err.println("Error creating budget: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#createBudget(budget.User, java.lang.String)
	 */
	@Override
	public String createBudget(User author, String budgetIdFrom) {
		return this.createBudget(author, budgetIdFrom, false);
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setClient(java.lang.String, int)
	 */
	@Override
	public boolean setClient(String budgetId, int clientId) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET client=? "
					+ "WHERE id=? ");
			pstmt.setInt(1, clientId);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting client of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting client of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setConstructionRef(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean setConstructionRef(String budgetId, String constructionRef) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET constructionRef=? "
					+ "WHERE id=? ");
			pstmt.setString(1, constructionRef);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting constructionRef of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting constructionRef of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setExpirationDate(java.lang.String, long)
	 */
	@Override
	public boolean setExpirationDate(String budgetId, long date) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET expirationDate=? "
					+ "WHERE id=? ");
			pstmt.setLong(1, date);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting expiration date of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting expiration date of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setNote(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean setNote(String budgetId, String note) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET note=? "
					+ "WHERE id=? ");
			pstmt.setString(1, note);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting final note of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting final note of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setTaxRate(java.lang.String, float)
	 */
	@Override
	public boolean setTaxRate(String budgetId, float taxRate) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET taxRate=? "
					+ "WHERE id=? ");
			pstmt.setFloat(1, taxRate);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting tax rate of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting tax rate of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#setGlobalTotal(java.lang.String, boolean)
	 */
	@Override
	public boolean setGlobalTotal(String budgetId, boolean hasGlobalTotal) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET hasGlobalTotal=? "
					+ "WHERE id=? ");
			pstmt.setBoolean(1, hasGlobalTotal);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting global total of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting global total of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#createBudget(java.lang.String, long)
	 */
	@Override
	public boolean createBudget(String budgetId, long creationDate) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET creationDate=? "
					+ "WHERE id=? ");
			pstmt.setLong(1, creationDate);
			pstmt.setString(2, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting creation date of budget, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting creation date of budget: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.BudgetDAO#signOffer(java.lang.String, int, long)
	 */
	@Override
	public boolean signOffer(String budgetId, int salespersonId, long date) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Budget table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Budget "
					+ "SET creationDate=?, signer=? "
					+ "WHERE id=? ");
			pstmt.setLong(1, date);
			pstmt.setInt(2, salespersonId);
			pstmt.setString(3, budgetId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting sign of offer, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting sign of offer: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}
	

	/**
	 * Extracts the budget from the database
	 * using the selected result from DB
	 * 
	 * @param result The result pointing the budget line
	 * @return A budget
	 * @throws SQLException 
	 */
	private Budget budgetFromResult(ResultSet result) throws SQLException {
		Salesperson salesperson = null;
		Client client = null;
		User author = null;
		Cover cover = null;
		Salesperson signer = null;
		// client and salesperson
		if ( result.getObject("client") != null ) {
			
			if ( result.getObject("c_salespersonId") != null ) {
				salesperson = new Salesperson(result.getInt("c_salespersonId"), 
						result.getString("cs_email"), result.getString("cs_name"), 
						result.getBoolean("cs_isEnabled"), new Permission[0]);
			}
		
			client = new Client(result.getInt("client"), result.getString("c_number"), 
					result.getString("c_address"), result.getString("c_town"), 
					result.getString("c_province"), result.getString("c_country"), 
					result.getString("c_postalCode"), result.getString("c_name"), 
					result.getString("c_email"), result.getString("c_phone"), 
					result.getString("c_person"), result.getString("c_notes"), 
					result.getBoolean("c_isActive"), salesperson);
		}
		
		// author
		if ( result.getObject("author") != null ) {
			author = new Salesperson(result.getInt("author"), result.getString("a_email"), 
					result.getString("a_name"), result.getBoolean("a_isEnabled"), new Permission[0]);
		}
		
		// cover
		if ( result.getObject("cover") != null ) {
			cover = new Cover(result.getInt("cover"));
		}
		
		// signer
		if ( result.getObject("signer") != null ) {
			signer = new Salesperson(result.getInt("signer"), result.getString("s_email"), 
					result.getString("s_name"), result.getBoolean("s_isEnabled"), new Permission[0]);
		}
		
		// create budget to return
		return new Budget(result.getBoolean("isOffer"), result.getString("id"),
				result.getLong("creationDate"), result.getLong("expirationDate"), 
				client, result.getString("constructionRef"), author, 
				result.getString("note"), result.getBoolean("hasGlobalTotal"), 
				result.getFloat("taxRate"), cover, signer);
	}
	
}
