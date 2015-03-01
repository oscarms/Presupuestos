/**
 * Data Access Objects
 */
package dao;

/**
 * This class represents the different types
 * of logs that can generate the application
 * 
 * @author oscar
 */
public enum LogType {

	/*
	 * Types of log messages
	 */
	CRITICAL, // There is an error that may damage the data
	ERROR, // The code reached a point that should not
	WARNING, // Something bad happened
	MESSAGE, // Information given by a user or interesting to know
	ACTION; // Something that has happened or has be done
	
}
