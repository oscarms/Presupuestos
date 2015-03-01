/**
 * Budget application specific objects
 */
package budget;

import java.io.File;

import dao.FileDAO;
import dao.FileType;
import storage.FileLocalStorage;

/**
 * This class represents a product. It is created from the data in
 * the database or introduced in a form. The prices array should
 * be sorted by date starting with the most recent before being
 * added to the Product. It also uses classes in package dao and
 * storage to return the product image.
 * 
 * @author oscar
 */
public class Product {
	
	/*
	 * Attributes
	 */
	private int productId;
	private String name;
	private String description;
	private Price[] prices; // already sorted by date
	private float costPrice; // only visible by administrators
	
	/**
	 * Constructor to be used when viewing or
	 * editing the product
	 *
	 * @param productId The unique ID of the product
	 * @param name The name
	 * @param description A short description of the product
	 * @param costPrice The cost price
	 * @param prices A list of Prices sorted by date
	 */
	public Product(int productId, String name, String description,
			float costPrice, Price[] prices) {
		this.productId = productId;
		this.name = name;
		this.description = description;
		this.prices = prices;
		this.costPrice = costPrice;
	}
	
	/**
	 * Returns productId attribute
	 *
	 * @return The unique ID of the product
	 */
	public int getProductId() {
		return this.productId;
	}
	
	/**
	 * Returns name attribute
	 * Returns an empty string if null
	 *
	 * @return The name
	 */
	public String getName() {
		if (this.name == null)
			return "";
		else
			return this.name;
	}
	
	/**
	 * Returns description attribute
	 * Returns an empty string if null
	 *
	 * @return A short description of the product
	 */
	public String getDescription() {
		if (this.description == null)
			return "";
		else
			return this.description;
	}
	
	/**
	 * Returns prices array or empty array if null
	 *
	 * @return A list of Prices sorted by date
	 */
	public Price[] getPrices() {
		if (this.prices == null)
			return new Price[0];
		else
			return this.prices;
	}
	
	/**
	 * Returns current price of the product. If it
	 * is discontinued, returns 0
	 *
	 * @return The current price of the product
	 */
	public float getCurrentPrice() {
		return this.getPrice(System.currentTimeMillis());
	}
	
	/**
	 * Returns the price of the product
	 * in the specified date. If it was
	 * discontinued, returns 0.
	 * Returns 0 if there is no price.
	 * date are the millis elapsed since 1970
	 *
	 * @param date
	 * @return The price of the product in a specified date
	 */
	public float getPrice(long date) {
		if ( prices == null || prices.length == 0 )
			return 0;
		for (Price price : prices) {
			if (price.getDate() < date)
				return price.getPrice();
		}
		return 0;
	}
	
	/**
	 * Returns true if it is currently discontinued
	 *
	 * @return True if it is currenty discontinued
	 */
	public boolean isCurrentlyDiscontinued() {
		return this.isDiscontinued(
				System.currentTimeMillis());
	}
	
	/**
	 * Returns true if the product was discontinued
	 * specified date or there are no prices.
	 * date are the millis elapsed since 1970
	 *
	 * @param date
	 * @return True if it was discontinued in a specified date
	 */
	public boolean isDiscontinued(long date) {
		if ( prices == null || prices.length == 0 )
			return true;
		for (Price price : prices) {
			if (price.getDate() < date)
				return price.isDiscontinued();
		}
		return true;
	}

	/**
	 * Returns costPrice attribute
	 *
	 * @return The cost price
	 */
	public float getCostPrice() {
		return this.costPrice;
	}
	
	/**
	 * Returns current profitMargin as percentage
	 * of selling price, calculated profitMargin = 
	 * (price - costPrice) / price
	 *
	 * @return The profit margin using the current price
	 */
	public float getCurrentProfitMargin() {
		return getProfitMargin(System.currentTimeMillis());
	}
	
	/**
	 * Returns the profitMargin as percentage
	 * of selling price, calculated profitMargin = 
	 * (price - costPrice) / price, using the price
	 * in the specified date.
	 * date are the millis elapsed since 1970
	 *
	 * @param date
	 * @return The profit margin using a price of a specified date
	 */
	public float getProfitMargin(long date) {
		float price = getPrice(date);
		if (price <= 0)
			return 0;
		
		float netProfit = price - this.getCostPrice();
		if (netProfit <= 0)
			return 0;
		
		return ( (netProfit / price) * 100 );
	}
	
	/**
	 * Returns a File with the image of the product
	 *
	 * @return A file with the image of the product
	 */
	public File getImage() {
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.get(FileType.PRODUCTIMAGE, this.getProductId() + ".png");
		if (file == null)
			file = fileDAO.get(FileType.PRODUCTIMAGE, "default.png");
		return file;
	}
	
	/**
	 * Returns a File with the image of the product
	 * in a reduced size
	 *
	 * @return A file with a small image of the product
	 */
	public File getMiniImage() {
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.get(FileType.PRODUCTMINIIMAGE, this.getProductId() + ".png");
		if (file == null)
			file = fileDAO.get(FileType.PRODUCTMINIIMAGE, "default.png");
		return file;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return this.getProductId() + ": " + this.getName() + " (" + 
				this.getDescription()+ ") ";
	}

}
