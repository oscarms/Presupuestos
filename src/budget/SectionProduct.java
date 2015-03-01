/**
 * Budget application specific objects
 */
package budget;

/**
 * This class represents a product and its information in a line
 * of a budget. It inherits all methods and attributes in Product.
 * It is created from the data in the database. The prices array
 * should be sorted by date starting with the most recent before
 * being added to the Product. It also uses classes in package
 * dao and storage to return the product image.
 * 
 * @author oscar
 */
public class SectionProduct extends Product {
	
	/*
	 * Attributes
	 */
	private float quantity; // number of units of product
	private float discount1; // percentage of discount
	private float discount2; // percentage of discount
	private float discount3; // percentage of discount
	
	/**
	 * Constructor to be used when viewing or editing a budget
	 *
	 * @param productId The unique ID of the product
	 * @param name The name
	 * @param description A short description of the product
	 * @param prices A list of Prices sorted by date
	 * @param quantity The number of units of the product
	 */
	public SectionProduct(int productId, String name, String description,
			Price[] prices, float quantity) {
		this(productId, name, description, prices, quantity, 0, 0, 0, 0);
	}
	
	/**
	 * Constructor to be used when viewing or editing an offer
	 *
	 * @param productId The unique ID of the product
	 * @param name The name
	 * @param description A short description of the product
	 * @param prices A list of Prices sorted by date
	 * @param quantity The number of units of the product
	 * @param discount1 A percentage of discount
	 * @param discount2 A percentage of discount
	 * @param discount3 A percentage of discount
	 * @param costPrice The cost price
	 */
	public SectionProduct(int productId, String name, String description,
			Price[] prices, float quantity, float discount1, float discount2,
			float discount3, float costPrice) {
		super(productId, name, description, costPrice, prices);
		this.quantity = quantity;
		this.discount1 = discount1;
		this.discount2 = discount2;
		this.discount3 = discount3;
	}
	
	/**
	 * Returns the profitMargin as percentage
	 * of selling price, calculated profitMargin = 
	 * (price - costPrice) / price, using the price
	 * in the specified date. The price is updated with
	 * the discounts before being calculated.
	 * Product.getProfitMargin() will get the change.
	 * date are the millis elapsed since 1970
	 */
	/* (non-Javadoc)
	 * @see budget.Product#getProfitMargin(long)
	 */
	@Override
	public float getProfitMargin(long date) {
		float price = getNetPrice(date); // this is the change
		if (price <= 0)
			return 0;
		
		
		float netProfit = price - this.getCostPrice();
		if (netProfit <= 0)
			return 0;
		
		return ( (netProfit / price) * 100 );
	}
	
	/**
	 * Returns quantity attribute
	 *
	 * @return The number of units of the product
	 */
	public float getQuantity() {
		return this.quantity;
	}
	
	/**
	 * Returns the net price using the price
	 * and the discounts
	 *
	 * @return The current price applying the discounts
	 */
	public float getCurrentNetPrice() {
		return getNetPrice(System.currentTimeMillis());
	}
	
	/**
	 * Returns the net price in the specified date
	 * using the price and the discounts.
	 * date are the millis elapsed since 1970
	 *
	 * @param date
	 * @return The price in a date applying the discounts
	 */
	public float getNetPrice(long date) {
		return this.getPrice(date) * (1 - this.getDiscount1() / 100) *
				(1 - this.getDiscount2() / 100) * 
				(1 - this.getDiscount3() / 100);
	}
	
	/**
	 * Returns discount1 attribute as a percentage
	 *
	 * @return A percentage of discount
	 */
	public float getDiscount1() {
		return this.discount1;
	}
	
	/**
	 * Returns discount2 attribute as a percentage
	 *
	 * @return A percentage of discount
	 */
	public float getDiscount2() {
		return this.discount2;
	}
	
	/**
	 * Returns discount3 attribute as a percentage
	 *
	 * @return A percentage of discount
	 */
	public float getDiscount3() {
		return this.discount3;
	}
	
	/**
	 * Returns the total price in the specified date
	 * as netPrice * quantity.
	 * date are the millis elapsed since 1970
	 *
	 * @return The total of the price in a date with discounts in its quantity
	 */
	public float getTotal(long date) {
		return this.getQuantity() * this.getNetPrice(date);
	}
	
	/**
	 * Returns the total price in the specified date
	 * as netPrice * quantity.
	 * date are the millis elapsed since 1970
	 *
	 * @return The total of the current price with discounts in its quantity
	 */
	public float getCurrentTotal() {
		return this.getTotal(System.currentTimeMillis());
	}

}
