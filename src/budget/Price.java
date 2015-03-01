/**
 * Budget application specific objects
 */
package budget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class consists in the representation of each
 * price and the date since it is valid.
 * If a product is discontinued, the price has a
 * negative value, but getPrice() will return 0.
 * It is important to check isDiscontinued too.
 * 
 * @author oscar
 */
public class Price {

	/*
	 * Attributes
	 */
	private long fromDate;
	private float price;
	
	/**
	 * Constructor of the class, will get a negative
	 * float as the price if it is discontinued
	 *
	 * @param fromDate The date since the price is valid
	 * @param price The price or -1 if it is discontinued
	 */
	public Price(long fromDate, float price) {
		this.price = price;
		this.fromDate = fromDate;
	}
	
	/**
	 * Returns the date since the price is valid
	 *
	 * @return The date since the price is valid
	 */
	public long getDate() {
		return this.fromDate;
	}
	
	/**
	 * Returns the date since the price
	 * is valid as string
	 *
	 * @return The date since the price is valid in format dd/MM/yyyy
	 */
	public String getDateString() {
		DateFormat dateFormat = 
				new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date( this.getDate() );
		return dateFormat.format(date);
	}
	
	/**
	 * Returns the price or 0 if it is discontinued
	 *
	 * @return The price or 0 if it is discontinued
	 */
	public float getPrice() {
		if ( this.price < 0 )
			return 0; // is discontinued
		else
			return this.price;
	}
	
	/**
	 * Returns true if it is discontinued
	 *
	 * @return True if it is discontinued
	 */
	public boolean isDiscontinued() {
		return ( this.price < 0 );
	}
}
