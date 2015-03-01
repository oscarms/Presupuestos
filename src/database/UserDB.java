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

import budget.Client;
import budget.Permission;
import budget.Salesperson;
import budget.User;
import dao.UserDAO;

/**
 * This class provides the implementation of the methods
 * available to create, get and update Users, Clients
 * and Salespeople. Methods are specified in UserDAO.
 * It uses DBConnection.
 * 
 * @author oscar
 */
public class UserDB implements UserDAO {

	/*
	 * Attribute UserDAO.MAXRESULTS
	 */
	
	/**
	 * Constructor
	 */
	public UserDB() {
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#getUser(java.lang.String, java.lang.String)
	 */
	@Override
	public User getUser(String email, String encriptedPassword) {
		User user = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT User.* "
					+ "FROM User, Salesperson "
					+ "WHERE "
					+	"User.email = ? AND User.password = ? AND User.isEnabled = true "
					+	"AND User.id = Salesperson.id ");
			pstmt.setString(1, email);
			pstmt.setString(2, encriptedPassword);
			
			result = pstmt.executeQuery();

			if (result.next()) {
				// count permissions
				int count = 0;
				if (result.getBoolean("administrate"))
					count++;
				if (result.getBoolean("viewOffers"))
					count++;
				if (result.getBoolean("createOffers"))
					count++;
				if (result.getBoolean("allClients"))
					count++;
				
				// create permissions structure and add permissions
				Permission[] permissions = new Permission[count];
				count = 0;
				if (result.getBoolean("administrate")) {
					permissions[count] = Permission.ADMINISTRATE;
					count++;
				}
				if (result.getBoolean("viewOffers")) {
					permissions[count] = Permission.VIEWOFFERS;
					count++;
				}
				if (result.getBoolean("createOffers")) {
					permissions[count] = Permission.CREATEOFFERS;
					count++;
				}
				if (result.getBoolean("allClients")) {
					permissions[count] = Permission.ALLCLIENTS;
					count++;
				}
				
				user = new Salesperson(result.getInt("id"), result.getString("email"),
						result.getString("name"), result.getBoolean("isEnabled"), permissions);
				
				if (result.next()) // more than 1 result
					user = null;
			}

			result.close();
			pstmt.close();
			
		} catch (SQLException e) {
			System.err.println("Error retrieving user: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return user;
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#getSalesperson(int)
	 */
	@Override
	public Salesperson getSalesperson(int salespersonId) {
		Salesperson salesperson = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT User.*, Salesperson.emailPassword "
					+ "FROM Salesperson "
					+ "INNER JOIN User "
					+ "ON Salesperson.id = User.id "
					+ "WHERE User.id = ? " );
					
					/*
					+ "SELECT User.*, Salesperson.emailPassword "
					+ "FROM User, Salesperson "
					+ "WHERE User.id = Salesperson.id AND User.id = ? ");
					*/
			pstmt.setInt(1, salespersonId);
			
			result = pstmt.executeQuery();

			if (result.next()) {
				
				// count permissions
				int count = 0;
				if (result.getBoolean("administrate"))
					count++;
				if (result.getBoolean("viewOffers"))
					count++;
				if (result.getBoolean("createOffers"))
					count++;
				if (result.getBoolean("allClients"))
					count++;
				
				// create permissions structure and add permissions
				Permission[] permissions = new Permission[count];
				count = 0;
				if (result.getBoolean("administrate")) {
					permissions[count] = Permission.ADMINISTRATE;
					count++;
				}
				if (result.getBoolean("viewOffers")) {
					permissions[count] = Permission.VIEWOFFERS;
					count++;
				}
				if (result.getBoolean("createOffers")) {
					permissions[count] = Permission.CREATEOFFERS;
					count++;
				}
				if (result.getBoolean("allClients")) {
					permissions[count] = Permission.ALLCLIENTS;
					count++;
				}
				
				salesperson = new Salesperson(result.getInt("id"), result.getString("email"),
						result.getString("name"), result.getBoolean("isEnabled"), 
						permissions, result.getString("emailPassword"));
				
				if (result.next()) // more than 1 result
					salesperson = null;
			}

			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving salesperson: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return salesperson;
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#getClient(int)
	 */
	@Override
	public Client getClient(int clientId) {
		Client client = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT CU.email, CU.name, C.*, S.id salespersonId, S.email s_email, S.name s_name, S.isEnabled s_isEnabled "
					+ "FROM Client C "
					+ "INNER JOIN User CU "
					+ "ON C.id = CU.id "
					+ "LEFT JOIN User S "
					+ "ON C.salesperson = S.id "
					+ "WHERE C.id = ? " );
					/*
					+ "SELECT User.email, User.name, Client.* "
					+ "FROM User, Client "
					+ "WHERE User.id = Client.id AND User.id = ? ");
					*/
			pstmt.setInt(1, clientId);
			
			result = pstmt.executeQuery();

			if (result.next()) {
				
				Salesperson salesperson = null;
				
				if ( result.getObject("salespersonId") != null ) {
					salesperson = new Salesperson(result.getInt("salespersonId"), 
							result.getString("s_email"), result.getString("s_name"), 
							result.getBoolean("s_isEnabled"), new Permission[0]);
				}
						
				client = new Client(clientId, result.getString("number"), 
						result.getString("address"), result.getString("town"), 
						result.getString("province"), result.getString("country"), 
						result.getString("postalCode"), result.getString("name"), 
						result.getString("email"), result.getString("phone"), 
						result.getString("person"), result.getString("notes"), 
						result.getBoolean("isActive"), salesperson);
				
				if (result.next()) // more than 1 result
					client = null;
			}

			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving client: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return client;
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#getClients(int, boolean, java.lang.String)
	 */
	@Override
	public Client[] getClients(int salespersonId, boolean inactives,
			String filter) {
		return this.getClients(salespersonId, false, inactives, filter);
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#getClients(int, boolean, boolean, java.lang.String)
	 */
	@Override
	public Client[] getClients(int salespersonId, boolean others,
			boolean inactives, String filter) {
		List<Client> clients = new ArrayList<Client>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		
		//Normalizamos en la forma NFD (Canonical decomposition)
		filter = Normalizer.normalize(filter, Normalizer.Form.NFD);
		//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
		filter = filter.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		// Create list of words in the filter
		String[] filterwords = filter.split(" ");
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT CU.email, CU.name, C.*, S.id salespersonId, S.email s_email, S.name s_name, S.isEnabled s_isEnabled "
					+ "FROM Client C "
					+ "INNER JOIN User CU "
					+ "ON C.id = CU.id "
					+ "LEFT JOIN User S "
					+ "ON C.salesperson = S.id "
					+ "WHERE ( C.salesperson " + (others ? "is NULL or C.salesperson !=" : "=" ) + " ? )"
					+ 	(inactives ? "" : "AND C.isActive = true ")
					+ "ORDER BY CU.name ");
			
					/*
					+ "SELECT User.id, Client.number, User.name, Client.isActive "
					+ "FROM User, Client "
					+ "WHERE Client.id = User.id "
					+ 	"AND ( Client.salesperson " + (others ? "is NULL or Client.salesperson !=" : "=" ) + " ? )"
					+ 	(inactives ? "" : "AND Client.isActive = true ")
					+ "ORDER BY User.name ");
					*/
					
			pstmt.setInt(1, salespersonId);
						
			result = pstmt.executeQuery();

			Salesperson salesperson;
			Client client;
			boolean found;
			String clientString;
			while (result.next() && clients.size() < MAXRESULTS) {
				
				// create client (with salesperson) to add
				if ( result.getObject("salespersonId") != null ) {
					salesperson = new Salesperson(result.getInt("salespersonId"), 
							result.getString("s_email"), result.getString("s_name"), 
							result.getBoolean("s_isEnabled"), new Permission[0]);
				} else {
						salesperson = null;
				}
						
				client = new Client(result.getInt("id"), result.getString("number"), 
						result.getString("address"), result.getString("town"), 
						result.getString("province"), result.getString("country"), 
						result.getString("postalCode"), result.getString("name"), 
						result.getString("email"), result.getString("phone"), 
						result.getString("person"), result.getString("notes"), 
						result.getBoolean("isActive"), salesperson);
				
				// check if contains all the words in the filter, and add
				found = true;
				for (String word : filterwords) {
					clientString = client.toString();
					//Normalizamos en la forma NFD (Canonical decomposition)
					clientString = Normalizer.normalize(clientString, Normalizer.Form.NFD);
					//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
					clientString = clientString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
					if (!clientString.toLowerCase().contains(word.toLowerCase()))
						found = false;
				}
				
				if (found)
					clients.add(client);
				
			}

			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving clients: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}
		return (Client[])clients.toArray(new Client[clients.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#getSalespeople()
	 */
	@Override
	public Salesperson[] getSalespeople() {
		List<Salesperson> salespeople = new ArrayList<Salesperson>();
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT User.id, User.email, User.name, User.isEnabled, User.administrate, User.viewOffers, User.createOffers, User.allClients  "
					+ "FROM Salesperson "
					+ "INNER JOIN User "
					+ "ON Salesperson.id = User.id ");
			
					/*
					+ "SELECT User.id, User.email, User.name, User.isEnabled, User.administrate, User.viewOffers, User.createOffers, User.allClients  "
					+ "FROM User, Salesperson "
					+ "WHERE Salesperson.id = User.id ");
					*/
			
			result = pstmt.executeQuery();

			int count;
			Permission[] permissions;
			while (result.next()) {
				
				// count permissions
				count = 0;
				if (result.getBoolean("administrate"))
					count++;
				if (result.getBoolean("viewOffers"))
					count++;
				if (result.getBoolean("createOffers"))
					count++;
				if (result.getBoolean("allClients"))
					count++;
				
				// create permissions structure and add permissions
				permissions = new Permission[count];
				count = 0;
				if (result.getBoolean("administrate")) {
					permissions[count] = Permission.ADMINISTRATE;
					count++;
				}
				if (result.getBoolean("viewOffers")) {
					permissions[count] = Permission.VIEWOFFERS;
					count++;
				}
				if (result.getBoolean("createOffers")) {
					permissions[count] = Permission.CREATEOFFERS;
					count++;
				}
				if (result.getBoolean("allClients")) {
					permissions[count] = Permission.ALLCLIENTS;
					count++;
				}
				
				salespeople.add( new Salesperson(result.getInt("id"),
						result.getString("email"), result.getString("name"), 
						result.getBoolean("isEnabled"), permissions) );
				
			}

			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving salespeople: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}
		
		return (Salesperson[])salespeople.toArray(new Salesperson[salespeople.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#create(budget.Client)
	 */
	@Override
	public int create(Client client) {
		
		// sql insert
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int id = -1;
		try {
			// insert into User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO User (email, name) "
					+ "VALUES (?, ?)"
					,PreparedStatement.RETURN_GENERATED_KEYS);
			if (client.getEmail().length() > 0)
				pstmt.setString(1, client.getEmail());
			else
				pstmt.setNull(1, java.sql.Types.VARCHAR);
			pstmt.setString(2, client.getName());
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating client, no rows affected.");
			}
			
			result = pstmt.getGeneratedKeys();
	        if (result.next()) {
	            id = (int) result.getInt(1);
	        
		        pstmt.clearParameters();
		        
		        // insert into Client table
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "INSERT INTO Client (id, number, person, phone, address, town, "
						+ 	"province, country, postalCode, notes, isActive, salesperson)"
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				
				pstmt.setInt(1, id);
				pstmt.setString(2, client.getClientNumber());
				pstmt.setString(3, client.getPerson());
				pstmt.setString(4, client.getPhone());
				pstmt.setString(5, client.getAddress());
				pstmt.setString(6, client.getTown());
				pstmt.setString(7, client.getProvince());
				pstmt.setString(8, client.getCountry());
				pstmt.setString(9, client.getPostalCode());
				pstmt.setString(10, client.getNotes());
				pstmt.setBoolean(11, client.isActive());
				if (client.getSalesperson() != null)
					pstmt.setInt(12, client.getSalesperson().getId());
				else
					pstmt.setNull(12, java.sql.Types.INTEGER);
				
				if (pstmt.executeUpdate() < 1) {
					// client not completely inserted, remove the part inserted into User table
					pstmt.clearParameters();
					pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
							+ "DELETE FROM User "
							+ "WHERE User.id = ?");
					pstmt.setInt(1, id);
					pstmt.executeUpdate();
					throw new SQLException("Error creating client, may be rows affected in User.");
				}

				// client inserted
				
	        }
			result.close();
			pstmt.close();

			return id;
			
		} catch (SQLException e) {
			System.err.println("Error creating client: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#update(budget.Client)
	 */
	@Override
	public boolean update(Client client) {

		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE User "
					+ "SET email=?, name=? "
					+ "WHERE id=? ");
			if (client.getEmail().length() > 0)
				pstmt.setString(1, client.getEmail());
			else
				pstmt.setNull(1, java.sql.Types.VARCHAR);
			pstmt.setString(2, client.getName());
			pstmt.setInt(3, client.getClientId());
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error updating client, no rows affected.");
			}
			
			pstmt.clearParameters();
			
			// update in Client table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Client "
					+ "SET number=?, person=?, phone=?, address=?, town=?, province=?, country=?, postalCode=?, isActive=? , notes=?"
					+ "WHERE id=? ");
			pstmt.setString(1, client.getClientNumber());
			pstmt.setString(2, client.getPerson());
			pstmt.setString(3, client.getPhone());
			pstmt.setString(4, client.getAddress());
			pstmt.setString(5, client.getTown());
			pstmt.setString(6, client.getProvince());
			pstmt.setString(7, client.getCountry());
			pstmt.setString(8, client.getPostalCode());
			pstmt.setBoolean(9, client.isActive());
			pstmt.setInt(11, client.getClientId());
			pstmt.setString(10, client.getNotes());
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error updating client, may be rows affected in table USER.");
			}

			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error updating client: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}

	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#setSalesPerson(int, int)
	 */
	@Override
	public boolean setSalesperson(int clientId, int salespersonId) {
		
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Client table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Client "
					+ "SET salesperson=? "
					+ "WHERE id=? ");
			pstmt.setInt(1, salespersonId);
			pstmt.setInt(2, clientId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting salesperson, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting salesperson: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#create(budget.Salesperson)
	 */
	@Override
	public int create(Salesperson salesperson) {
		
		// sql insert
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int id = -1;
		try {
			// insert into User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO User (email, name) "
					+ "VALUES (?, ?)"
					,PreparedStatement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, salesperson.getEmail());
			pstmt.setString(2, salesperson.getName());
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating salesperson, no rows affected.");
			}
			
			result = pstmt.getGeneratedKeys();
	        if (result.next()) {
	            id = (int) result.getLong(1);
	        
		        pstmt.clearParameters();
		        
		        // insert into Salesperson table
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "INSERT INTO Salesperson (id)"
						+ "VALUES (?)");
				
				pstmt.setInt(1, id);
				
				if (pstmt.executeUpdate() < 1) {
					// salesperson not completely inserted, remove the part inserted into User table
					pstmt.clearParameters();
					pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
							+ "DELETE FROM User "
							+ "WHERE User.id = ?");
					pstmt.setInt(1, id);
					pstmt.executeUpdate();
					throw new SQLException("Error creating salesperson, may be rows affected in User.");
				}

				// salesperson inserted
				
	        }
	        result.close();
			pstmt.close();
			
			return id;
			
		} catch (SQLException e) {
			System.err.println("Error creating salesperson: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#update(budget.Salesperson)
	 */
	@Override
	public boolean update(Salesperson salesperson) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE User "
					+ "SET email=?, name=? "
					+ "WHERE id=? ");
			pstmt.setString(1, salesperson.getEmail());
			pstmt.setString(2, salesperson.getName());
			pstmt.setInt(3, salesperson.getId());
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error updating salesperson, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error updating salesperson: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#setMailPassword(int, java.lang.String)
	 */
	@Override
	public boolean setMailPassword(int salespersonId, String mailPassword) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Salesperson table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Salesperson "
					+ "SET emailPassword=? "
					+ "WHERE id=? ");
			pstmt.setString(1, mailPassword);
			pstmt.setInt(2, salespersonId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting mail password, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting mail password: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#setPassword(int, java.lang.String)
	 */
	@Override
	public boolean setPassword(int userId, String newPassword) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE User "
					+ "SET password=? "
					+ "WHERE id=? ");
			pstmt.setString(1, newPassword);
			pstmt.setInt(2, userId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting password, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting password: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#setUserEnabled(int, boolean)
	 */
	@Override
	public boolean setUserEnabled(int userId, boolean enabled) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE User "
					+ "SET isEnabled=? "
					+ "WHERE id=? ");
			pstmt.setBoolean(1, enabled);
			pstmt.setInt(2, userId);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting isEnabled, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting isEnabled: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#setUserEnabled(java.lang.String, boolean)
	 */
	@Override
	public boolean setUserEnabled(String email, boolean enabled) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE User "
					+ "SET isEnabled=? "
					+ "WHERE email=? ");
			pstmt.setBoolean(1, enabled);
			pstmt.setString(2, email);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting isEnabled, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.UserDAO#setPermissions(int, budget.Permission[])
	 */
	@Override
	public boolean setPermissions(int userId, Permission[] permissions) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in User table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE User "
					+ "SET administrate=?, viewOffers=?, createOffers=?, allClients=? "
					+ "WHERE id=? ");
			pstmt.setBoolean(1, false);
			pstmt.setBoolean(2, false);
			pstmt.setBoolean(3, false);
			pstmt.setBoolean(4, false);
			pstmt.setInt(5, userId);
			
			// set true for the existing permissions
			for (Permission permission : permissions) {
				if (permission.equals(Permission.ADMINISTRATE))
					pstmt.setBoolean(1, true);
				else if (permission.equals(Permission.VIEWOFFERS))
					pstmt.setBoolean(2, true);
				else if (permission.equals(Permission.CREATEOFFERS))
					pstmt.setBoolean(3, true);
				else if (permission.equals(Permission.ALLCLIENTS))
					pstmt.setBoolean(4, true);
			}
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error setting permissions, no rows affected.");
			}
						
			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error setting permissions: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

}
