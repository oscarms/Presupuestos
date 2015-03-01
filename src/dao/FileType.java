/**
 * Data Access Objects
 */
package dao;

/**
 * This class represents the different types
 * of files that can be loaded and downloaded
 * 
 * @author oscar
 */
public enum FileType {

	/*
	 * Types of Files
	 */
	DOCUMENT, // A Word, a PDF, an image...
	ATTACHMENT, // A PDF to be attached at the end of the budget
	COVER, // A PNG image that will be the background of the cover
	MINICOVER, // The cover in size 70x99px
	PRODUCTIMAGE, // A PNG image of the product
	PRODUCTMINIIMAGE, // The product image in size 300x300px
	CLIENTIMAGE, // A PNG image of the client
	CLIENTMINIIMAGE, // The client image in size 300x300px
	TEMPORARY, // A temporary file
	GLOBAL; // Global files
	
}
