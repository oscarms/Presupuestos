/**
 * Budget application specific objects
 */
package budget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents an annotation that is present
 * in a budget to be used as reference by the salesperson.
 * It consists in a message written in a date.
 * 
 * @author oscar
 */
public class Annotation {

	/*
	 * Attributes
	 */
	private long date;
	private String text;
	
	/**
	 * Constructor
	 * 
	 * @param date The annotation date in ms
	 * @param text The annotation text
	 */
	public Annotation(long date, String text) {
		this.date = date;
		this.text = text;
	}
	
	/**
	 * Returns date attribute
	 * 
	 * @return The annotation date in ms
	 */
	public long getDate() {
		return this.date;
	}
	
	/**
	 * Returns date as string
	 * 
	 * @return The annotation date as a string
	 */
	public String getDateString() {
		DateFormat dateFormat = 
				new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date( this.getDate() );
		return dateFormat.format(date);
	}
	
	/**
	 * Returns text attribute or empty string if it is null
	 * 
	 * @return The annotation text
	 */
	public String getText() {
		if ( this.text == null )
			return "";
		else
			return this.text;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return this.getText();
	}

}
