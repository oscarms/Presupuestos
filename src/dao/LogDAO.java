/**
 * Data Access Objects
 */
package dao;

/**
 * This class provides the specification of the methods
 * available to write a log file.
 * It is implemented in LogDB.
 * 
 * @author oscar
 */
public interface LogDAO {
	
	/*
	 * Attributes
	 */
	/**
	 * Minimum LogType that will be logged
	 * and stored in the database
	 */
	public static final LogType LOGLEVEL = LogType.MESSAGE; // TODO Lower level
	/**
	 * Time in milliseconds from the current
	 * date that the logs are not removed when
	 * purging
	 * 604800000 is a week
	 */
	public static final long TIMETOSTORE = 604800000;
	
	/**
	 * Stores the message in the log
	 *
	 * @param type The type of the message
	 * @param message The text
	 * @param date The time in milliseconds
	 */
	public void add(LogType type, String message, long date);
	
	/**
	 * Purges the log removing old entries
	 * from the database
	 * 
	 * @return Number of rows deleted
	 */
	public int purge();

}
