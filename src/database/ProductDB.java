/**
 * Database related objects
 */
package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import budget.Price;
import budget.Product;
import dao.ProductDAO;

/**
 * This class provides the implementation of the methods
 * available to create, get and update Products and Prices.
 * Methods are specified in ProductDAO. It uses DBConnection.
 * 
 * @author oscar
 */
public class ProductDB implements ProductDAO {

	/*
	 * Attributes
	 * 	ProductDAO.MAXRESULTS
	 * 	ProductDAO.FIRSTPRODUCTID
	 * 	ProductDAO.LASTPRODUCTID
	 */
	
	/**
	 * Constructor
	 */
	public ProductDB() {
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#getProduct(int)
	 */
	@Override
	public Product getProduct(int productId) {
		Product product = null;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Product.*, Price.fromDate, Price.price "
					+ "FROM Product "
					+ "LEFT JOIN Price "
					+ "ON Product.id = Price.product "
					+ "WHERE Product.id = ? "
					+ "ORDER BY Price.fromDate DESC"
					);

			pstmt.setInt(1, productId);
			
			result = pstmt.executeQuery();
			
			String name = null;
			String description = "";
			float costPrice = 0;
			List<Price> prices = new ArrayList<Price>();
			
			while (result.next()) {
				// save product attributes on first iteration
				if (name == null) {
					name = result.getString("name");
					description = result.getString("description");
					costPrice = result.getFloat("costPrice");
				}
				if (result.getObject("fromDate") != null)
					prices.add(new Price(result.getLong("fromDate"), result.getFloat("price")));
			}
			
			// construct product if found in db
			if ( name != null && name.length() > 0 )
				product = new Product(productId, name, description, costPrice, 
						(Price[])prices.toArray(new Price[prices.size()]));
			
			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving product: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}

		return product;
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#getProducts(float, float, boolean, java.lang.String)
	 */
	@Override
	public Product[] getProducts(float minPrice, float maxPrice,
			boolean discontinued, String filter) {
		List<Product> products = new ArrayList<Product>();
		PreparedStatement pstmt = null;
		ResultSet result = null;

		//Normalizamos en la forma NFD (Canonical decomposition)
		filter = Normalizer.normalize(filter, Normalizer.Form.NFD);
		//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
		filter = filter.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		// Create list of words in the filter
		String[] filterwords = filter.split(" ");
		
		// Query db
		try {

			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT Product.*, Price.fromDate, Price.price "
					+ "FROM Product "
					+ "LEFT JOIN Price "
					+ "ON Product.id = Price.product "
					+ "WHERE ( Price.fromDate = ( SELECT MAX(fromDate) FROM Price WHERE product = Product.id AND fromDate <= ? ) OR Price.fromDate is NULL OR 1 = ( SELECT COUNT(fromDate) FROM Price WHERE product = Product.id ) ) "
					+ ( minPrice > Float.MIN_VALUE ? "AND (Price.price >= ?) " : "")
					+ ( maxPrice < Float.MAX_VALUE ? "AND (Price.price <= ?) " : "")
					+ ( !discontinued ? "AND (Price.price >= 0 AND fromDate <= ?) " : "")
					+ "ORDER BY Product.id "
					);
					
			/*
				SELECT Product.*, Price.fromDate, Price.price
				FROM Product
				LEFT JOIN Price
				ON Product.id = Price.productId
				WHERE ( Price.fromDate = (SELECT MAX(fromDate) FROM Price WHERE productId = Product.id) OR Price.fromDate is NULL )
				AND (Price.price >= ?) // if minPrice > Float.min
				AND (Price.price <= 3) // if maxPrice < Float.max
				AND (Price.price >= 0) // if !discontinued
			 */
			
			pstmt.setLong(1, System.currentTimeMillis());
			int valuePos = 2;
			if (minPrice > Float.MIN_VALUE) {
				pstmt.setFloat(valuePos++, (float) (minPrice - 0.0001));
				// comparing floats there is a possible error
			}
			if (maxPrice < Float.MAX_VALUE)
				pstmt.setFloat(valuePos++, (float) (maxPrice + 0.0001));
				// comparing floats there is a possible error
			if (!discontinued)
				pstmt.setLong(valuePos++, System.currentTimeMillis());
			
			result = pstmt.executeQuery();

			Product product;
			Price price;
			Price[] prices;
			boolean found;
			String productString;
			while (result.next() && products.size() < MAXRESULTS) {
				
				// create product (with last price) to add
				if ( result.getObject("fromDate") != null ) {
					price = new Price(result.getLong("fromDate"), result.getFloat("price"));
					prices = new Price[1];
					prices[0] = price;
				} else {
					price = null;
					prices = new Price[0];
				}
					
				product = new Product(result.getInt("id"), result.getString("name"),
						result.getString("description"),
						result.getFloat("costPrice"), prices);
				
				// check if contains all the words in the filter, and add
				found = true;
				for (String word : filterwords) {
					productString = product.toString();
					//Normalizamos en la forma NFD (Canonical decomposition)
					productString = Normalizer.normalize(productString, Normalizer.Form.NFD);
					//Reemplazamos los acentos con una una expresión regular de Bloque Unicode
					productString = productString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
					if (!productString.toLowerCase().contains(word.toLowerCase()))
						found = false;
				}
				
				if (found)
					products.add(product);
				
			}

			result.close();
			pstmt.close();

		} catch (SQLException e) {
			System.err.println("Error retrieving products: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
		}
		return (Product[])products.toArray(new Product[products.size()]);
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#newProductId()
	 */
	@Override
	public int newProductId() {
		int last = LASTPRODUCTID;
		int first = FIRSTPRODUCTID;
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int freeId = -1;
		
		
		try {
			// retrieve the last id +1
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "SELECT MAX(id)+1 id "
					+ "FROM Product "
					+ "WHERE id >= ? AND id <= ? "
					);

			pstmt.setInt(1, first-1);
			pstmt.setInt(2, last);
			
			result = pstmt.executeQuery();

			if (result.next() && result.getObject("id") != null && result.getInt("id") <= last) {
				freeId = result.getInt("id");
			} else {
				result.close();
				pstmt.close();
				// retrieve ids in a hole (not working if it is empty)
				pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "SELECT id+1 id "
						+ "FROM Product "
						+ "WHERE id >= ? AND id < ? AND id+1 NOT IN (SELECT id FROM Product)"
						);
				pstmt.setInt(1, first-1);
				pstmt.setInt(2, last);

				result = pstmt.executeQuery();

				if (result.next() && result.getObject("id") != null) {
					freeId = result.getInt("id");
				} else {
					result.close();
					pstmt.close();
					// check the first one (return first one if empty, -1 if full)
					pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
							+ "SELECT id "
							+ "FROM Product "
							+ "WHERE id = ?"
							);
					pstmt.setInt(1, first);

					result = pstmt.executeQuery();
					if (!result.next())
						freeId = first;
				}
			}

			result.close();
			pstmt.close();
			return freeId;
			
		} catch (SQLException e) {
			System.err.println("Error retrieving a free product ID: " + e.getMessage());
			if (result != null) try { result.close(); } catch (SQLException e2) {}
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return -1;
		}

	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#create(budget.Product)
	 */
	@Override
	public boolean create(Product product) {
		return this.create(product.getProductId(), product.getName(), 
				product.getDescription(), product.getCostPrice());
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#create(int, java.lang.String, java.lang.String, float)
	 */
	@Override
	public boolean create(int productId, String name, String description,
			float costPrice) {

		// sql insert
		PreparedStatement pstmt = null;
		try {
			// insert into Product table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Product (id, name, description, costPrice) "
					+ "VALUES (?, ?, ?, ?)");
			pstmt.setInt(1, productId);
			pstmt.setString(2, name);
			pstmt.setString(3, description);
			pstmt.setFloat(4, costPrice);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating product, no rows affected.");
			}
			// product inserted
				
			pstmt.close();
			
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error creating product: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
				
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#update(budget.Product)
	 */
	@Override
	public boolean update(Product product) {
		return this.update(product.getProductId(), product.getName(), 
				product.getDescription(), product.getCostPrice());
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#update(int, java.lang.String, java.lang.String, float)
	 */
	@Override
	public boolean update(int productId, String name, String description,
			float costPrice) {
		// sql update
		PreparedStatement pstmt = null;
		try {
			// update in Product table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "UPDATE Product "
					+ "SET name=?, description=?, costPrice=? "
					+ "WHERE Product.id=? ");
			pstmt.setInt(4, productId);
			pstmt.setString(1, name);
			pstmt.setString(2, description);
			pstmt.setFloat(3, costPrice);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error updating product, no rows affected.");
			}

			pstmt.close();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Error updating product: " + e.getMessage());
			if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#create(int, budget.Price)
	 */
	@Override
	public boolean create(int productId, Price price) {
		if (price.isDiscontinued()) {
			return this.create(productId, -1, price.getDate());
		} else {
			return this.create(productId, price.getPrice(), price.getDate());
		}
	}

	/* (non-Javadoc)
	 * @see dao.ProductDAO#create(int, float, double)
	 */
	@Override
	public boolean create(int productId, float price, long date) {
		// sql insert
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		ResultSet result = null;
		
		try {
			// insert into Price table
			pstmt = DBConnection.getInstance().getConnection().prepareStatement(""
					+ "INSERT INTO Price (product, price, fromDate) "
					+ "VALUES (?, ?, ?)" );
			pstmt.setInt(1, productId);
			pstmt.setFloat(2, price);
			pstmt.setLong(3, date);
			
			if (pstmt.executeUpdate() < 1) {
				throw new SQLException("Error creating price, no rows affected.");
			}
	        
			pstmt.close();
			return true;
		} catch (SQLException e) {
			try {
				// it is likely the date is repeated. Check and try with other date
				pstmt2 = DBConnection.getInstance().getConnection().prepareStatement(""
						+ "SELECT * "
						+ "FROM Price "
						+ "WHERE product = ? AND fromDate = ? "
						);
	
				pstmt2.setInt(1, productId);
				pstmt2.setLong(2, date);
				
				result = pstmt2.executeQuery();
	
				if (result.next()) {
					if (!create(productId, price, date + 1)) {
						throw new SQLException("Error creating price, no rows affected.");
					}
				} else {
					throw new SQLException("Error creating price, no rows affected.");
				}
				result.close();
				pstmt2.close();
				pstmt.close();
				return true;
				
			} catch (SQLException e1) {
				System.err.println("Error creating price: " + e1.getMessage());
				if (result != null) try { result.close(); } catch (SQLException e2) {}
				if (pstmt2 != null) try { pstmt2.close(); } catch (SQLException e2) {}
				if (pstmt != null) try { pstmt.close(); } catch (SQLException e2) {}
				return false;
			}
		}
		
	}

}
