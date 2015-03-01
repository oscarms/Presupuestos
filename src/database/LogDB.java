/**
 * Database related objects
 */
package database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dao.LogDAO;
import dao.LogType;

/**
 * This class provides the implementation of the methods
 * available to write a log file.
 * Methods are specified in LogDAO class.
 * It uses ConnectDB.
 * 
 * @author oscar
 */
public class LogDB implements LogDAO {

	/*
	 * Attributes
	 * 	LogDAO.LOGLEVEL
	 *	LogDAO.TIMETOSTORE
	 */

	/**
	 * Constructor
	 */
	public LogDB() {
	}

	/* (non-Javadoc)
	 * @see dao.LogDAO#add(dao.LogType, java.lang.String, long)
	 */
	@Override
	public void add(LogType type, String message, long date) {
		// number the LogType
		int typeId = 0;
		int levelId = 0;

		switch (type) {
			case CRITICAL:
				typeId = 1;
				break;
			case ERROR:
				typeId = 2;
				break;
			case WARNING:
				typeId = 3;
				break;
			case MESSAGE:
				typeId = 4;
				break;
			case ACTION:
				typeId = 5;
				break;	
		}
		
		switch (LOGLEVEL) {
		case CRITICAL:
			levelId = 1;
			break;
		case ERROR:
			levelId = 2;
			break;
		case WARNING:
			levelId = 3;
			break;
		case MESSAGE:
			levelId = 4;
			break;
		case ACTION:
			levelId = 5;
			break;	
		}
		
		if (levelId >= typeId) {
			
			System.err.println(message); // TODO Remove all System.err.println
			
			DateFormat dateFormat = 
					new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S");
			String readableDate = dateFormat.format(new Date(date));
			
			// sql insert
			PreparedStatement pstmt = null;
			try {
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "INSERT INTO Log (instant, date, type, message)"
						+ "VALUES (?, ?, ?, ?)");
				pstmt.setLong(1, date);
				pstmt.setString(2, readableDate);
				pstmt.setInt(3, typeId);
				pstmt.setString(4, message);
				
				if (pstmt.executeUpdate() < 1)
					System.err.println("Error writing log");
				pstmt.close();
	
			} catch (SQLException e) {
				System.err.println("Error writing log: " + e.getMessage());
				if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			} catch (NullPointerException e) {
				System.err.println("Error writing log: " + e.getMessage());
				if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			}
			
		} // end if

	}

	/* (non-Javadoc)
	 * @see dao.LogDAO#purge()
	 */
	@Override
	public int purge() {
		
		long untilDate = System.currentTimeMillis() - TIMETOSTORE;
		int rows = -1;
		
		// sql remove
			PreparedStatement pstmt = null;
			try {
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "DELETE FROM Log "
						+ "WHERE instant < ? ");
				pstmt.setLong(1, untilDate);
				rows = pstmt.executeUpdate();
				pstmt.close();
	
			} catch (SQLException e) {
				System.err.println("Error purging log: " + e.getMessage());
				if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			}
		return rows;
		
	}

}
