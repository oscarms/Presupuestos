/**
 * Data Access Objects
 */
package dao;

import budget.Price;
import budget.Product;

/**
 * This class provides the specification of the methods
 * available to create, get and update Products and Prices.
 * It is implemented in ProductDB.
 * 
 * @author oscar
 */
public interface ProductDAO {

	/*
	 * Attributes
	 */
	/**
	 * Maximum results that the class can give in
	 * an array. If an array returned by this
	 * class has more length than MAXRESULTS,
	 * there are results not given.
	 */
	public static final int MAXRESULTS = 50;
	/**
	 * First and last product id available
	 * that will be suggested by the system
	 * when creating a new product
	 */
	public static final int FIRSTPRODUCTID = 9000;
	public static final int LASTPRODUCTID = 9999;
	
	
	/* Get objects */
	
	/**
	 * Retrieves the Product with the specified productId
	 * from the database, null if not exists.
	 * 
	 * @param productId The unique ID of the product
	 * @return The product found in the database
	 */
	public Product getProduct(int productId);
	
	/**
	 * Retrieves the products (up to a max, if the array
	 * returned is max there may be results not listed)
	 * with the specified search criteria. The filter search
	 * if every word introduced by the user is contained in
	 * any of the following: The product id, name or description.
	 * 
	 * 
	 * @param minPrice The minimum current price
	 * @param maxPrice The maximum current price
	 * @param discontinued If it is true, will show currently discontinued products
	 * @param filter The words to find
	 * @return array of products
	 */
	public Product[] getProducts(float minPrice, float maxPrice,
			boolean discontinued, String filter);

	
	/* Create products */
	
	/**
	 * Returns a high number (from 9000 to 9999) available to
	 * insert a new product.
	 * 
	 * @return A productId not used
	 */
	public int newProductId();
	
	/**
	 * Inserts the product in the database and returns true
	 * if it completes the operation.
	 * 
	 * @param product The product to insert in the database
	 * @return True if it is inserted
	 */
	public boolean create(Product product);
	
	/**
	 * Inserts the product in the database and returns true
	 * if it completes the operation.
	 * 
	 * @param productId The unique ID of the product
	 * @param name The name of the product
	 * @param description A short description of the product
	 * @param costPrice The cost price of the product
	 * @return True if it is inserted
	 */
	public boolean create(int productId, String name, String description,
			float costPrice);
	
	
	/* Update products */
	
	/**
	 * Updates the product in the database and returns true
	 * if it completes the operation.
	 * 
	 * @param product The product to update in the database
	 * @return True if it is updated
	 */
	public boolean update(Product product);
	
	/**
	 * Updates the product in the database and returns true
	 * if it completes the operation.
	 * 
	 * @param productId The unique ID of the product
	 * @param name The name of the product
	 * @param description A short description of the product
	 * @param costPrice The cost price of the product
	 * @return True if it is updated
	 */
	public boolean update(int productId, String name, String description,
			float costPrice);
	
	
	/* Create prices */
	
	/**
	 * Inserts the price in the database and returns true
	 * if it completes the operation.
	 * 
	 * @param productId The unique ID of the product
	 * @param price A price with a date since it is valid
	 * @return True if it is inserted
	 */
	public boolean create(int productId, Price price);
	
	/**
	 * Inserts the price in the database and returns true
	 * if it completes the operation.
	 * 
	 * @param productId The unique ID of the product
	 * @param price The price value
	 * @param date The date since the price is valid
	 * @return True if it is inserted
	 */
	public boolean create(int productId, float price, long date);

}
