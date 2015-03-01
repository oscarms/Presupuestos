/**
 * Database related objects
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is a singleton. It contains
 * the connection to the database, and
 * the methods to retrieve the connection
 * and finalize it.
 * 
 * @author oscar
 */
public class DBConnection {

	/*
	 * Attributes
	 */
	private Connection connection; 
	private static DBConnection _instance;
	private static final String USER = "gdpUser";
	private static final String PASSWORD = "gdp!P4s5w0rD";
	private static final String DBURL = "jdbc:mysql://127.0.0.1:3306/GDPRESUPUESTOS"
							+ "?autoReconnect=true";
							//+ "&verifyServerCertificate=false&useSSL=true&requireSSL=true";
	
	/**
	 * Constructor private (Singleton)
	 */
	private DBConnection() {
		connection = null;
	}
	
	/**
	 * Singleton call
	 *
	 * @return The instance of the singleton
	 */
	public static synchronized DBConnection getInstance() {
		if (_instance==null)
			_instance = new DBConnection();
		return _instance;
	};
	
	/**
	 * Returns the connection to the database.
	 * Creates it if not exists
	 *
	 * @return The connection to the database
	 */
	public Connection getConnection() {
		if (connection==null) {
			try {
				DriverManager.registerDriver(new com.mysql.jdbc.Driver());
				connection = DriverManager.getConnection(DBURL,USER,PASSWORD);
			} catch (SQLException e) {
				return null;
			}	
		}
		return connection;
	}
	
	/**
	 * Closes and deletes the database connection
	 */
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize() {
		try {
			connection.close();
		} catch (SQLException e) {
		}
		connection = null;
	}
	
}
