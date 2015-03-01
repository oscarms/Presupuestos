/**
 * Budget application specific objects
 */
package budget;

import java.util.Arrays;

/**
 * This class represents a section of products in a budget.
 * It provides methods to get the sum of the prices (total)
 * and the list of products, that should get already ordered
 * when creating the object Section.
 * 
 * @author oscar
 */
public class Section {

	/*
	 * Attributes
	 */
	private int sectionId;
	private String name;
	private SectionProduct[] products;
	
	/**
	 * Constructor
	 *
	 * @param sectionId The unique ID of the section
	 * @param name The title of the section
	 * @param products A list of products in the section
	 */
	public Section(int sectionId, String name, SectionProduct[] products) {
		this.sectionId = sectionId;
		this.name = name;
		this.products = products;
	}
	
	/**
	 * Returns sectionId attribute
	 *
	 * @return The unique ID of the section
	 */
	public int getSectionId() {
		return this.sectionId;
	}
	
	/**
	 * Returns name attribute or empty string if it is null
	 *
	 * @return The title of the section
	 */
	public String getName() {
		if ( name == null )
			return "";
		else
			return this.name;
	}
	
	/**
	 * Returns products array or empty array if it is null
	 *
	 * @return A list of products in the section
	 */
	public SectionProduct[] getProducts() {
		if (this.products == null)
			return new SectionProduct[0];
		else
			return this.products;
	}
	
	/**
	 * Returns the total price of the sum
	 * of the products in their quantities
	 * in the specified date.
	 * date are the millis elapsed since 1970
	 *
	 * @return The total price of the sum of the content in the specified date
	 */
	public float getTotal(long date) {
		if ( products == null || products.length == 0 )
			return 0;
		
		float sum = 0;
		for (SectionProduct product : products) {
			sum += product.getTotal(date);
		}
		return sum;
	}
	
	/**
	 * Returns the total price of the sum
	 * of the products in their quantities
	 *
	 * @return The current total price of the sum of the content
	 */
	public float getCurrentTotal() {
		return this.getTotal(System.currentTimeMillis());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return this.getName() + ": " + Arrays.deepToString(this.getProducts());
	}

}
