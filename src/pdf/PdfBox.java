/**
 * Package with classes to create PDFs
 */
package pdf;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.EncodingManager;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.util.PDFMergerUtility;

import storage.FileLocalStorage;
import dao.FileDAO;
import dao.FileType;
import budget.Attachment;
import budget.Budget;
import budget.Section;
import budget.SectionProduct;

/**
 * This class provides a method to create a temporary
 * PDF file from a budget that can be downloaded or 
 * sent by email.
 * This class uses the FileDAO data storage classes and
 * the related with the Budget class
 * 
 * @author oscar
 */
public class PdfBox {
	
	/**
	 * This class represents a cell in a table with a
	 * set of attributes that are set in the constructor
	 * and the methods to retrieve these attributes
	 * 
	 * @author oscar
	 */
	private class Cell {

		/*
		 * Atributes
		 */
		private String text;
		private String alignment; // First character C/c centered, R/r right, other left
		private Color backColor;
		private Color textColor;
		private PDFont font;
		private int fontSize;
		
		/**
		 * Constructor. All parameters must be used without null
		 * 
		 * @param text The text in the cell
		 * @param font Font to draw the text
		 * @param fontSize Size of the font
		 * @param textColor Color of the font
		 * @param alignment Where to align the text: Left, Right or Center
		 * @param backColor Color to fill the rectangle of the cell
		 */
		public Cell(String text, PDFont font, int fontSize, Color textColor, 
				String alignment, Color backColor) {
			this.text = text;
			this.font = font;
			this.fontSize = fontSize;
			this.textColor = textColor;
			this.alignment = alignment;
			this.backColor = backColor;
		}
		
		/**
		 * @return The text in the cell
		 */
		public String getText() {
			return this.text;
		}
		
		/**
		 * @return Font to draw the cell
		 */
		public PDFont getFont()  {
			return this.font;
		}
		
		/**
		 * @return Size of the font
		 */
		public int getFontSize() {
			return this.fontSize;
		}
		
		/**
		 * @return Color of the font
		 */
		public Color getTextColor() {
			return this.textColor;
		}
		
		/**
		 * @return Where to align the text: Left, Right or Center
		 */
		public String getAlignment() {
			return this.alignment;
		}
		
		/**
		 * @return Color to fill the rectangle of the cell
		 */
		public Color getBackColor() {
			return this.backColor;
		}

	}
	
	
	/*
	 * Attributes
	 */
	
	// Attributes about page size and drawing cursor position
	private PDDocument document;
	private PDPage page;
	private boolean compressEnabled = false;
	
	private static final PDRectangle PAGESIZE = PDPage.PAGE_SIZE_A4;
	/*
	 * Contents are drawable inside the rectangle delimited
	 * by pageMarginTop, pageMarginBottom, pageMarginLeft,
	 * and pageMarginRight. These attributes are set by
	 * initializeCursor() using Margin attribute.
	 */
	private float pageMarginTop;
	private float pageMarginBottom;
	private float pageMarginLeft;
	private float pageMarginRight;
	private float cursorPosition;
	/*
	 * cursorPosition is a vertical coordinate in the page.
	 * Above this point (coordinates bigger than cursorPosition),
	 * the page is already drawn. Below this point, the page
	 * is empty.
	 */
	private static final float MARGIN = mmToPdfBox(20);
	
	// Fonts
	private PDFont font; 
	private PDFont fontBold;
	private PDFont fontMedium;
	private PDFont fontLight;
	
	// Colors
	private Color textColor; // Color created by the cover color
	private Color bwColor; // Color to the font in the cover
	/*
	 * textColor and bwColor are set by setColors()
	 * using the color of the cover where the text is
	 * written on. textColor is dark enough to be legible
	 * on a white background.
	 */
	
	// Budget
	private Budget budget;
	
	// Company Info
	private static final String COMPANYINFO1 = "Mortadelo y Filemón, T.I.A. NIF A76542346";
	private static final String COMPANYINFO2 = "13 Rue del Percebe. 55555 Ibañez - Tebeo, España";
	private static final String COMPANYINFO3 = "555-1234-56789, info@myf.org";
	
	/**
	 * Private constructor. Manages the whole construction
	 * of the PDF file, that stores in the file passed by
	 * the parameter. It will rewrite the file if it already
	 * exists
	 * 
	 * @param file File where the PDF is stored.
	 * @param budget The budget to show in the PDF.
	 */
	private PdfBox(File file, Budget budget) {

		try {
			// store budget
			this.budget = budget;
						
			// initialize document and load fonts
			createDocument();

			// set font color attributes
			setColors();

			// initialize cursor and set in a new page
			initializeCursor();

			// draw cover
			drawCover();
			cursorPosition = pageMarginBottom;
				// forces to create a new page in the next step

			// iterate sections
			for (Section section : budget.getSections()) {
				drawSection(section);
			}

			// draw summary
			createPage();
			drawSummary();
			
			// draw total tables
			
			if (!budget.hasGlobalTotal()) {
				// draw total in case it is an offer
				String title;
				String netPrice;
				String taxes;
				String total;
				float height;
				float heightL = 0;
				float width = mmToPdfBox(70);
				boolean right = false;
				
				float xLeft = pageMarginLeft;
				float xRight = pageMarginRight - width;
				
				for (Section section : budget.getSections()) {
					
					// retrieve data
					title = section.getName();
					float sectionPrice = section.
							getTotal(budget.getCreationDate());
					netPrice = formatMoney(sectionPrice);
					taxes = formatMoney(sectionPrice * 
							(budget.getTaxRate()/100) );
					total = formatMoney(sectionPrice * 
							((budget.getTaxRate()/100) + 1) );
					
					// check if needs a new page
					height = getSectionTotalHeight(title, width);
					if ( height > cursorPosition - pageMarginBottom ) {
						createPage();
						cursorPosition -= mmToPdfBox(5);
						right = false;
					}
					
					// draw
					drawSectionTotal((right ? xRight : xLeft),
							cursorPosition, width, title, netPrice, 
							taxes, total);
					
					// update cursorPosition
					if (right) {
						cursorPosition -= Math.max(heightL, height)
								+ mmToPdfBox(10);
						right = false;
					} else { // left
						heightL = height;
						right = true;
					}
	
				}
									
				// update cursorPosition if it finished on the left side
				if (right) {
					cursorPosition -= heightL + mmToPdfBox(5);
				}
			} else { // is not an offer
				
				// draw total in case it is a budget
				drawTotal();
			}

			// draw final note
			drawNote();
			
			// Add the attachments to the PDF
			mergeAttachments();
						
			// Save the PDF and close the document
			document.save(file);
			document.close();

		} catch (IOException e) {
			// File changes to null
			if (document != null)
				try {
					file = null;
					document.close();
					document = null; 
				} catch (IOException e1) {
					document = null;
				}
		}
		
	}
	
	/**
	 * The only one public method.
	 * Generates a PDF file in the temporary folder
	 * with the budgetID as the name. Rewrites the
	 * file if already exists. The PDF contains the
	 * information of the budget and its attachments.
	 * Returns null if there is a problem
	 * 
	 * @param budget Budget to print in the PDF
	 * @return File that contains the PDF
	 */
	public static File createPdf(Budget budget) {
		
		FileDAO fileDAO = new FileLocalStorage();
		File file = fileDAO.create(FileType.TEMPORARY, 
				budget.getBudgetId() + ".pdf");
		
		new PdfBox(file, budget);
		
		return file;
	}

	/**
	 * Calculates the position of the margins,
	 * sets the attributes about the page size
	 * and sets the cursorPosition at the top
	 * of a page
	 */
	private void initializeCursor() {

		pageMarginTop = PAGESIZE.getUpperRightY() - MARGIN;
		pageMarginBottom = PAGESIZE.getLowerLeftY() + MARGIN;
		pageMarginLeft = PAGESIZE.getLowerLeftX() + MARGIN;
		pageMarginRight = PAGESIZE.getUpperRightX() - MARGIN;
		cursorPosition = pageMarginTop;
	}
	
	/**
	 * Creates a new page in the PDF, draws the header
	 * of the page and the page number at the bottom
	 * 
	 * @throws IOException
	 */
	private void createPage() throws IOException {
		
		// create page
		page = new PDPage(PAGESIZE);
		document.addPage(page);

		PDPageContentStream stream = new PDPageContentStream
				(document, page, true, compressEnabled);
		
		// Add page number
		
		// alignment right, start drawing 2mm below margin bottom
		cursorPosition = pageMarginBottom - mmToPdfBox(2);
		// color grey, font standard, font size 12
		stream.setNonStrokingColor(textColor);
		writeLineFromRight(fontLight,12,
				String.valueOf(document.getNumberOfPages() ),stream);

		// Add header
		
		// alignment left, start drawing above pageMarginTop.
		// color textColor, font light, font size 16
		String headerText = (budget.isOffer() ? "Oferta Nº " : 
			"Presupuesto Nº ") + budget.getBudgetId();
		PDFont currentFont = fontLight;
		int currentFontSize = 16;
		float height = currentFont.getFontDescriptor().
				getFontBoundingBox().getHeight() / 
				1000 * currentFontSize;
		cursorPosition = pageMarginTop + height + mmToPdfBox(2);
		stream.setNonStrokingColor(textColor);
		writeLine(currentFont,currentFontSize,headerText,stream);

		stream.close();

		cursorPosition = pageMarginTop; // must do nothing
	}

	/**
	 * Creates the document and imports the fonts that will
	 * be needed. The fonts should be stored in the Global
	 * folder
	 * 
	 * @throws IOException
	 */
	private void createDocument() throws IOException {
		document = new PDDocument();
		
		// load fonts
		FileDAO fileDAO = new FileLocalStorage();
		
		font = PDType1Font.HELVETICA;
		fontBold = PDTrueTypeFont.loadTTF(document, 
				fileDAO.get(FileType.GLOBAL, "HnBd.ttf") );
		fontMedium = PDTrueTypeFont.loadTTF(document, 
				fileDAO.get(FileType.GLOBAL, "HnRo.ttf") );
		fontLight = PDTrueTypeFont.loadTTF(document, 
				fileDAO.get(FileType.GLOBAL, "HnLt.ttf") );
		
	}
	
	/**
	 * Creates a new page and draws the cover image as the
	 * background. Also writes the text in the cover
	 * 
	 * @throws IOException
	 */
	private void drawCover() throws IOException {
		
		// add page and prepare to draw
		PDPage page = new PDPage(PAGESIZE);
		document.addPage(page);
		PDPageContentStream stream = new PDPageContentStream
				(document, page, true, compressEnabled);
		
		// Draw background image
		drawImage(stream, budget.getCover().getImage(), 0, 
				PAGESIZE.getUpperRightY(), PAGESIZE.getWidth(), 
				PAGESIZE.getHeight());
		
		// Draw text
		stream.setNonStrokingColor(bwColor);
		
		// alignment right, start drawing 22mm from top
		cursorPosition -= mmToPdfBox(2);
		
		// define text, font and font size for each line
		// three Arrays with the same length
		String[] lineText = {(budget.isOffer() ? "OFERTA" : "PRESUPUESTO"),
				"Nº " + budget.getBudgetId(), "", budget.getConstructionRef(),
				"", "CLIENTE", budget.getClient().getName()};
		PDFont[] lineFont = {fontLight, fontMedium, fontMedium, fontMedium,
				fontLight, fontLight, fontMedium};
		int[] lineSize = {28, 16, 16, 16, 28, 28, 16};
		
		for (int i = 0; i < lineText.length; i++) {
			writeLineFromRight(lineFont[i],lineSize[i],lineText[i],stream);
		}

		stream.close();
	}
	
	/**
	 * Draws an image in the coordinates specified,
	 * streching the image to fill all the space
	 * 
	 * @param stream The open PDPageContentStream to write
	 * @param image Image file
	 * @param x Coordinate of the left side of the image
	 * @param y Coordinate of the top side of the image
	 * @param width The width of the image
	 * @param height The height of the image
	 * @throws IOException
	 */
	private void drawImage(PDPageContentStream stream, File image, 
			float x, float y, float width, float height) 
					throws IOException {
		
		PDImageXObject pdImage = null;
		
		/* code from http://svn.apache.org/repos/asf/pdfbox/
		 * trunk/examples/src/main/java/org/apache/pdfbox/
		 * examples/pdmodel/ImageToPDF.java
		 */
		
		try {
        if( image.getAbsolutePath().toLowerCase().endsWith( ".jpg" ) )
        {
            pdImage = JPEGFactory.createFromStream(document, 
            		new FileInputStream(image));
        }
        else if (image.getAbsolutePath().toLowerCase().endsWith(".tif") || 
                image.getAbsolutePath().toLowerCase().endsWith(".tiff"))
        {
            pdImage = CCITTFactory.createFromRandomAccess(document, 
            		(RandomAccess) new RandomAccessFile(image,"r"));
        }
        else if (image.getAbsolutePath().toLowerCase().endsWith(".gif") || 
                image.getAbsolutePath().toLowerCase().endsWith(".bmp") || 
                image.getAbsolutePath().toLowerCase().endsWith(".png"))
        {
            BufferedImage bim = ImageIO.read(image);
            pdImage = LosslessFactory.createFromImage(document, bim);
        }
        else
        {
            throw new IOException( "Image type not supported: " + image );
        }

        stream.drawXObject(pdImage, x, y-height, width, height );
		} catch (java.lang.OutOfMemoryError e) {
		} catch (java.lang.NullPointerException e) {
		}

	}
	
	/**
	 * Calculates the height of a image to maintain
	 * its aspect ratio while changing it wideness
	 * 
	 * @param image
	 * @param width
	 * @return The height of the image
	 */
	protected static float getScalatedHeight(File image, float width) {
		
		BufferedImage bimg;
		try {
			bimg = ImageIO.read(image);
		} catch (IOException e) {
			return -1;
		}
		return width / bimg.getWidth() * bimg.getHeight();
	}
	
    /**
     * Breaks the text in lines to make long text
     * to fit in a specified width
     * 
     * @param width The maximum width of each line
     * @param text The text
     * @param fontSize The size of the font
     * @param font The font used to write the text
     * @return A list of lines
     */
    private static List<String> getLines(float width, String text, 
    		int fontSize, PDFont font) {
    	/*
    	 * Code from http://stackoverflow.com/questions/10640152/
    	 * how-can-i-create-fixed-width-paragraphs-with-pdfbox
    	 */
    	
    	List<String> result = new ArrayList<String>();

        String[] split = text.split("(?<=[\\ ])");
        // "(?<=\\W)" has troubles with foreign characters,
        // "(?<=[\\ ])" restricted to blank spaces
        int[] possibleWrapPoints = new int[split.length];
        possibleWrapPoints[0] = split[0].length();
        for ( int i = 1 ; i < split.length ; i++ ) {
            possibleWrapPoints[i] = possibleWrapPoints[i-1] 
            		+ split[i].length();
        }

        int start = 0;
        int end = 0;
        for ( int i : possibleWrapPoints ) {
            float widthSize;
			try {
				widthSize = font.getStringWidth(text.substring(start,i)) 
						/ 1000 * fontSize;
			} catch (IOException e) {
				return null;
			}

            if ( start < end && widthSize > width ) {
                result.add(text.substring(start,end));
                start = end;
            }
            end = i;
        }
        // Last piece of text
        result.add(text.substring(start));
        return result;
    }
    
    /**
     * Encodes the text before being written in the PDF
     * replacing all characters with their codes to
     * avoid illegible characters. If the character
     * does not have a code or does not exist in the
     * font, it will be replaced by a " ".
     * This method must be called before writing text
     * in the PDF
     * 
     * @param text The text to write
     * @return The text to write compatible with the PDF
     */
    protected static String encodeText (String text) {
    	/*
    	 * Code from http://stackoverflow.com/questions/22260344
    	 * /pdfbox-encode-symbol-currency-euro
    	 */
    	
    	char replacement = '.'; // To replace inexistent characters
    	
    	char[] tc = text.toCharArray();
        StringBuilder te = new StringBuilder();
        Encoding e;
		try {
			e = EncodingManager.INSTANCE.getEncoding
					(COSName.WIN_ANSI_ENCODING);
		} catch (IOException e1) {
			return text;
		}           
        for (int i = 0; i < tc.length; i++) {
            Character c = tc[i];
            int code = 0;
            if(Character.isWhitespace(c)){
                try {
					code = e.getCode("space");
				} catch (IOException e1) {
				}
            }else{
                try {
					code = e.getCode(e.getNameForCharacter(c));
				} catch (IOException e1) {
					try {
						code = e.getCode
								(e.getNameForCharacter(replacement));
					} catch (IOException e2) {
					}
				}
            }               
            te.appendCodePoint(code);
        }
        
    	return te.toString();
    	
    }
	
	/**
	 * Draws a text in the specified coordinates
	 * with the specified width. The text can be
	 * aligned to the Left, Right or Center
	 * 
	 * @param x The coordinate of the left side
	 * @param y The coordinate of the top side
	 * @param width The maximum width of the text
	 * @param text The text to write
	 * @param fontSize The size of the font
	 * @param font The font to use to write the text
	 * @param alignment Left, Right or Center
	 * @param out An open stream where the PDF is written
	 * @throws IOException
	 */
	private void drawParagraph(float x, float y, float width, 
			String text, int fontSize, PDFont font, String alignment, 
			PDPageContentStream out) throws IOException {
	    
		// coordinates that are used in the method
	    float currentX = x;
	    float currentY = y + mmToPdfBox(2); // reduce offset
	    
		float lineHeight = font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * fontSize;
		float lineWidth;
		
		// identify alignment
		int align = 1; // 1 Left, -1 Right, 0 Center
		String alignSearch = alignment.substring(0, 1);
		if (alignSearch.equalsIgnoreCase("C"))
			align = 0;
		
		if (alignSearch.equalsIgnoreCase("R"))
			align = -1;

		// split lines and write each one
	    List<String> lines = getLines(width, text, fontSize, font);
	    for (Iterator<String> i = lines.iterator(); i.hasNext(); ) {
	    	out.beginText();
		    out.setFont(font, fontSize);

	    	String toWrite = i.next().trim();
	    	
	    	// if the alignment is centered or right, measure X
	    	if (align < 1) {
	    		// measure string
	    		lineWidth = font.getStringWidth(text) / 1000 * fontSize;
	    		// set the x
	    		if (align == 0) { // alignment center
	    			currentX = x + width / 2 - lineWidth / 2;
	    		} else { // align == -1 alignment right
	    			currentX = x + width - lineWidth;
	    		}
	    	}

	    	// write 
	    	currentY -= lineHeight;
	    	out.moveTextPositionByAmount(currentX, currentY);
	        out.drawString(encodeText(toWrite));
	        
	        out.endText();
	    } // end for
	    
	}
	
	/**
	 * Measures the height of a paragraph without
	 * the need of drawing it or the coordinates
	 * 
	 * @param width The maximum width of the text
	 * @param text The text to write
	 * @param fontSize The size of the font
	 * @param font The font to use to write the text
	 * @return The height of a paragraph with the text
	 */
	protected static float getParagraphHeight(float width, 
			String text, int fontSize, PDFont font) {
		
		try {
			return font.getFontDescriptor().getFontBoundingBox()
					.getHeight() / 1000 * fontSize 
					* getLines(width, text, fontSize, font).size();
		} catch (NullPointerException e) {
			return 0; // it is likely the text is null
		}
	}
	
	/**
	 * Draws a line of text aligned to the right
	 * below the vertical position of cursorPosition
	 * and moves cursorPosition to the bottom
	 * of the line
	 * 
	 * @param font The font to use to write the text
	 * @param fontSize The size of the font
	 * @param text The text to write
	 * @param out An open stream where the PDF is written
	 * @throws IOException
	 */
	private void writeLineFromRight(PDFont font, int fontSize, 
			String text, PDPageContentStream out) throws IOException {
		
		// Calculate line sizes
		float width = font.getStringWidth(text) / 1000 * fontSize;
		float height = font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * fontSize;
		
		// Move cursorPosition
		cursorPosition -= height;
				
		// Draw line
		out.beginText();
	    out.setFont(font, fontSize);
	    out.moveTextPositionByAmount(pageMarginRight - width, cursorPosition);
	    out.drawString(encodeText(text));
	    out.endText();
	}
	
	/**
	 * Draws a line of text aligned to the left
	 * below the vertical position of cursorPosition
	 * and moves cursorPosition to the bottom
	 * of the line
	 * 
	 * @param font The font to use to write the text
	 * @param fontSize The size of the font
	 * @param text The text to write
	 * @param out An open stream where the PDF is written
	 * @throws IOException
	 */
	private void writeLine(PDFont font, int fontSize, String text, 
			PDPageContentStream out) throws IOException {
		
		// Calculate line height
		float height = font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * fontSize;
		
		// Move cursorPosition
		cursorPosition -= height;
				
		// Draw line
		out.beginText();
	    out.setFont(font, fontSize);
	    out.moveTextPositionByAmount(pageMarginLeft, cursorPosition);
	    out.drawString(encodeText(text));
	    out.endText();
	}
	
	/**
	 * Draws a title consisting of a line from marginLeft
	 * to the text of the title and the text, in color,
	 * below cursorPosition. Also moves cursorPosition
	 * to the bottom of the title, with vertical margins.
	 * The titles are used in each section header and in
	 * the budget summary
	 * 
	 * @param text The text of the title
	 * @throws IOException
	 */
	private void drawTitle(String text) throws IOException {
		
		PDPageContentStream out = new PDPageContentStream(document,
				page, true, compressEnabled);
		// font and fontSize declaration
		PDFont font = fontLight;
		int fontSize = 20;
		
		float width = font.getStringWidth(text) / 1000 * fontSize;
		float height = font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * fontSize;

		out.setNonStrokingColor(textColor); // colored text
		
		// margin above the title
		cursorPosition -= height * 2;
		
		// draw text
		out.beginText();
	    out.setFont(font, fontSize);
	    out.moveTextPositionByAmount(pageMarginRight - width, 
	    		cursorPosition);
	    out.drawString(encodeText(text));
	    out.endText();
	    
	    // draw line
	    out.setStrokingColor(textColor); // colored line
	    float lineYPosition = cursorPosition + (height / 4);
		out.drawLine(pageMarginLeft, lineYPosition, pageMarginRight 
				- width - mmToPdfBox(1), lineYPosition);
		
		out.close();
	    
	    // set margin at the bottom as far as the height of the font
	    cursorPosition -= height;
	}
	
	/**
	 * Calculates the height used by the title 
	 * and its margins without drawing it
	 * 
	 * @return The height that would need
	 */
	private float getTitleHeight() {
		
		// font and fontSize declaration
		PDFont font = fontLight;
		int fontSize = 20;
				
		return font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * fontSize * 3;
	}
	
	/**
	 * Draws a product in the last page or in
	 * a new one if there is no enough space.
	 * Draws the product below cursorPosition, and
	 * sets it at the bottom of the product at the end.
	 * The product consists of a name, its description,
	 * a photo and a table with its reference, quantity
	 * and prices
	 * 
	 * @param product The product to draw
	 * @throws IOException
	 */
	private void drawProduct(SectionProduct product) throws IOException {
		
		File image = product.getImage();
		String name = product.getName();
		String description = product.getDescription();
		boolean isOffer = budget.isOffer();
		String productId = formatNumber(product.getProductId());
		String quantity = formatNumber(product.getQuantity());
		long date = budget.getCreationDate();
		String price = formatMoney(product.getPrice(date));
		String total = formatMoney(product.getTotal(date));
		String discount1 = formatPercentage(product.getDiscount1());
		String discount2 = formatPercentage(product.getDiscount2());
		String discount3 = formatPercentage(product.getDiscount3());
		String netPrice = formatMoney(product.getNetPrice(date));
		
		// Check if it fits in the page
		if ( getProductHeight(product) > cursorPosition 
				- pageMarginBottom ) {
			createPage();
			cursorPosition -= mmToPdfBox(5);
		}
		
		PDPageContentStream out = new PDPageContentStream(document, 
				page, true, compressEnabled);
		
		// Draw photo 35mm width only if it has a photo
		if (!image.getAbsolutePath().endsWith("default.png")) {
			float imageHeight = getScalatedHeight(image, 
					mmToPdfBox(35));
			drawImage(out, image, pageMarginLeft, cursorPosition, 
					mmToPdfBox(35), imageHeight);
		}
		
		// draw text area 60mm from left
		float paragraphMargin = mmToPdfBox(60);
		float paragraphWidth = pageMarginRight - paragraphMargin;
		
		// Draw name
		out.setNonStrokingColor(Color.BLACK); // colored text
		int fontSize = 14;
		PDFont font = this.font;
		drawParagraph(paragraphMargin, cursorPosition, paragraphWidth, 
				name, fontSize, font, "Left", out);
		
		// cursor position to draw the description
		float cursorPosition = this.cursorPosition - getParagraphHeight
				(paragraphWidth, name, fontSize, font);
		
		// Draw description
		fontSize = 10;
		font = this.font;
		drawParagraph(paragraphMargin, cursorPosition, paragraphWidth,
				description, fontSize, font, "Left", out);
		out.close();
		
		cursorPosition -= getParagraphHeight(paragraphWidth, 
				description, fontSize, font) + mmToPdfBox(2);
		
		// Draw table
		
		// predefined table attributes
		float columnGap = mmToPdfBox(1);
		float rowGap = mmToPdfBox(0.6f);
		Color tableTitleBackColor = textColor;
		Color tableTitleTextColor = Color.WHITE;
		Color tableContentBackColor = new Color(239, 239, 239);
		Color tableContentTextColor = Color.BLACK;
		int tableTitleFontSize = 8;
		int tableContentFontSize = 8;
		PDFont tableTitleFont = fontBold;
		PDFont tableContentFont = fontLight;
		
		// already checked it fits in the page by getProductHeight()
		
		if (isOffer) {
			// define how to draw the table in the case of an offer
			float[] columnWidths = {mmToPdfBox(19), mmToPdfBox(11),
					mmToPdfBox(17), mmToPdfBox(13), mmToPdfBox(13),
					mmToPdfBox(13), mmToPdfBox(17), mmToPdfBox(20)};
			String[] contentAlignments = {"Center", "Center", "Right",
					"Center", "Center", "Center", "Right", "Right"};
			// any align left, R*/r* align right, C*/c* align center
			
			String[] tableTitles = {"Referencia", "Uds.", "Tarifa ud.",
					"Dto. 1", "Dto. 2", "Dto. 3", "P.neto", "Total"};
			String[] tableContents = {productId, quantity, price, 
					discount1, discount2, discount3, netPrice, total};
			
			// create cells of the table
			Cell[][] cells = createCellsOfProductTable(tableTitles, 
					contentAlignments, tableContents, tableTitleFontSize,
					tableContentFontSize, tableTitleFont, tableContentFont,
					tableTitleTextColor, tableTitleBackColor,
					tableContentTextColor, tableContentBackColor);

			// draw
			drawTable(paragraphMargin, cursorPosition, columnWidths,
					rowGap, columnGap, cells);
			
		} else { // is a bugdet
			// define how to draw the table in the case of a budget
			float[] columnWidths = {mmToPdfBox(36.1f), mmToPdfBox(26.3f),
					mmToPdfBox(33.4f), mmToPdfBox(31.2f)};
			String[] tableTitles = {"Referencia", "Unidades",
					"Tarifa unidad", "Total"};
			String[] contentAlignments = {"Center", "Center",
					"Right", "Right"};
			// any align left, R*/r* align right, C*/c* align center
			
			String[] tableContents = {productId, quantity, price, total};
			
			// create cells of the table
			Cell[][] cells = createCellsOfProductTable(tableTitles,
					contentAlignments, tableContents, tableTitleFontSize,
					tableContentFontSize, tableTitleFont, tableContentFont, 
					tableTitleTextColor, tableTitleBackColor,
					tableContentTextColor, tableContentBackColor);

			// draw
			drawTable(paragraphMargin, cursorPosition, columnWidths,
					rowGap, columnGap, cells);
		}
		
		// update global cursorPosition
		this.cursorPosition = this.cursorPosition 
				- getProductHeight(product) - mmToPdfBox(8);

	}
	
	/**
	 * Measures the drawn of a product, without
	 * drawing it. The drawn consists of a name,
	 * its description, a photo and a table
	 * with its reference, quantity and prices
	 * 
	 * @param product The product to measure its drawn
	 * @throws IOException
	 */
	private float getProductHeight(SectionProduct product)
			throws IOException {
		
		File image = product.getImage();
		String name = product.getName();
		String description = product.getDescription();
		boolean isOffer = budget.isOffer();
		String productId = formatNumber(product.getProductId());
		String quantity = formatNumber(product.getQuantity());
		long date = budget.getCreationDate();
		String price = formatMoney(product.getPrice(date));
		String total = formatMoney(product.getTotal(date));
		String discount1 = formatPercentage(product.getDiscount1());
		String discount2 = formatPercentage(product.getDiscount2());
		String discount3 = formatPercentage(product.getDiscount3());
		String netPrice = formatMoney(product.getNetPrice(date));
		
		// height of image
		float imageHeight = getScalatedHeight(image, mmToPdfBox(35));
		
		// height of text
		float paragraphHeight = 0;
		
		// would draw text area 60mm from left
		float paragraphMargin = mmToPdfBox(60);
		float paragraphWidth = pageMarginRight - paragraphMargin;
		
		// measure name
		int fontSize = 14;
		PDFont font = this.font;
		paragraphHeight += getParagraphHeight(paragraphWidth, 
				name, fontSize, font);
		
		// measure description
		fontSize = 10;
		font = this.font;
		paragraphHeight += getParagraphHeight(paragraphWidth, 
				description, fontSize, font) + mmToPdfBox(2);
		
		// measure table
		// predefined table attributes
		float rowGap = mmToPdfBox(0.6f);
		int tableTitleFontSize = 8;
		int tableContentFontSize = 8;
		PDFont tableTitleFont = fontBold;
		PDFont tableContentFont = fontLight;
		
		// already checked it would in the page by getProductHeight()
		
		if (isOffer) {
			// define how to draw the table in the case of an offer
			
			float[] columnWidths = {mmToPdfBox(19), mmToPdfBox(11),
					mmToPdfBox(17), mmToPdfBox(13), mmToPdfBox(13),
					mmToPdfBox(13), mmToPdfBox(17), mmToPdfBox(20)};
			String[] contentAlignments = {"Center", "Center", "Right",
					"Center", "Center", "Center", "Right", "Right"};
			// any align left, R*/r* align right, C*/c* align center
			
			String[] tableTitles = {"Referencia", "Uds.", "Tarifa ud.",
					"Dto. 1", "Dto. 2", "Dto. 3", "P.neto", "Total"};
			String[] tableContents = {productId, quantity, price, 
					discount1, discount2, discount3, netPrice, total};
			
			// create cells of the table
			Cell[][] cells = createCellsOfProductTable(tableTitles,
					contentAlignments, tableContents, tableTitleFontSize,
					tableContentFontSize, tableTitleFont, tableContentFont, 
					Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE);
	
			// get height
			paragraphHeight += getTableHeight(columnWidths, rowGap, cells);
			
		} else { // is a bugdet
			// define how to draw the table in the case of a budget
			
			float[] columnWidths = {mmToPdfBox(36.1f), mmToPdfBox(26.3f),
					mmToPdfBox(33.4f), mmToPdfBox(31.2f)};
			String[] tableTitles = {"Referencia", "Unidades", 
					"Tarifa unidad", "Total"};
			String[] contentAlignments = {"Center", "Center",
					"Right", "Right"}; 
			// any align left, R*/r* align right, C*/c* align center
			
			String[] tableContents = {productId, quantity, price, total};
			
			// create cells of the table
			Cell[][] cells = createCellsOfProductTable(tableTitles,
					contentAlignments, tableContents, tableTitleFontSize, 
					tableContentFontSize, tableTitleFont, tableContentFont, 
					Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE);
	
			// get height
			paragraphHeight +=  getTableHeight(columnWidths, rowGap, cells);
		
		}
	
		// return the biggest
		return Math.max(paragraphHeight, imageHeight);
	}

	/**
	 * Draws a table from the coordinates specified
	 * with the widths introduced. If it does not fit
	 * in the current page, it will create a new page
	 * and continue drawing the table there. If it
	 * creates a new page, it will edit cursorPosition.
	 * columnWidths should be as long as each row in cells
	 * 
	 * @param x The coordinate of the left side of the table
	 * @param y The coordinate of the top of the table
	 * @param columnWidths A list of the widths of each column
	 * @param rowGap The white space between rows
	 * @param columnGap The white space between columns
	 * @param cells The matrix of cells to draw: Rows of cells
	 * @return The height of the table in the last page it is drawn
	 * @throws IOException
	 */
	private float drawTable(float x, float y, float[] columnWidths,
			float rowGap, float columnGap, Cell[][] cells) 
					throws IOException {

		// calculate horizontal points to start drawing each cell
		float[] columnX = new float[columnWidths.length];
		float currentX = x;
		for (int i=0; i<cells[0].length; i++) {
			columnX[i] = currentX;
			currentX = currentX + columnWidths[i] + columnGap;
		}
		
		// draw table
		float currentY = y; 
		// vertical position of the row (lower y is lower in the page)
		float height;
		float heightInPage = 0;
		
		PDPageContentStream out = new PDPageContentStream(document, 
				page, true, compressEnabled);
		
		for (Cell[] row : cells) { // for each row draw its column
			
			// calculate the row height
			height = getRowHeight(columnWidths, row);
			
			// check if it is necessary to insert a new page
			if ( height > currentY - pageMarginBottom ) {
				out.close();
				createPage();
				out = new PDPageContentStream(document, 
						page, true, compressEnabled);
				cursorPosition -= mmToPdfBox(5);
				currentY = cursorPosition;
				heightInPage = 0;
			}
			
			// draw cells of the row
			for (int i=0; i<row.length; i++) {
				drawCell(columnX[i], currentY, columnWidths[i],
						height, row[i], out);
			}
			
			// calculate the vertical position of the next row 
			// and the total height
			currentY = currentY - height - rowGap;
			heightInPage = heightInPage + height + rowGap;
		}
		
		// draw bottom line as wide as the table is
		out.setNonStrokingColor(textColor);
		out.fillRect(x, currentY, getTableWidth(columnWidths, 
				columnGap), mmToPdfBox(0.2f));
		//out.addLine(x, currentY, x + getTableWidth(columnWidths, 
		//		columnGap), currentY); not working
		
		out.close();
		
		return heightInPage;
		
	}
	
	/**
	 * Calculates the height of a table that would be
	 * drawn with the sizes introduced.
	 * columnWidths should be as long as each row in cells 
	 * 
	 * @param columnWidths A list of the widths of each column
	 * @param rowGap The white space between rows
	 * @param cells The matrix of cells to draw: Rows of cells
	 * @return The height of the table that would be drawn
	 */
	protected static float getTableHeight(float[] columnWidths, 
			float rowGap, Cell[][] cells) {
		
		float height = 0;
		
		for (Cell[] row : cells) { // add the row height
			height += getRowHeight(columnWidths, row);
		}
		
		// add gap spaces
		height += rowGap * cells.length; // final gap used by the line
		
		return height;
	}
	
	/**
	 * Calculates the width of a table that would be
	 * drawn with the widths introduced
	 * 
	 * @param columnWidths A list of the widths of each column
	 * @param columnGap The white space between columns
	 * @return The width of the table that would be drawn
	 */
	protected static float getTableWidth(float[] columnWidths, 
			float columnGap) {
		
		float totalWidth = 0;
		for (float width : columnWidths)
			totalWidth += width;
		
		// add gap spaces
		totalWidth += columnGap * (columnWidths.length - 1);
		
		return totalWidth;
	}
	
	/**
	 * Calculates the height of a row of a table 
	 * that would be drawn with the sizes introduced.
	 * columnWidths should be as long as the row of cells
	 * 
	 * @param widths The widths of each column in the row
	 * @param cells The cells of each column in the row
	 * @return The height of the highest cell in the row
	 */
	protected static float getRowHeight(float[] widths, Cell[] cells) {
		
		float maxHeight = 0;
		for(int i=0; i<cells.length; i++) {
			maxHeight = Math.max(
					getCellHeight(widths[i], cells[i]), maxHeight);
		}
		return maxHeight;
	}
	
	/**
	 * Draws a cell from the coordinates specified
	 * with the sizes introduced
	 * 
	 * @param x The coordinate of the left side of the cell
	 * @param y The coordinate of the top of the cell
	 * @param width The width of the cell rectangle
	 * @param height The height of the cell rectangle
	 * @param cell The cell data
	 * @param out An open stream where the PDF is written
	 * @throws IOException
	 */
	private void drawCell(float x, float y, float width, 
			float height, Cell cell, PDPageContentStream out) 
					throws IOException {

		// draw rectangle
		out.setNonStrokingColor(cell.getBackColor());
		out.fillRect(x, y-height, width, height);
		
		// draw text
		out.setNonStrokingColor(cell.getTextColor());
		// draw text with little margins from sides
		drawParagraph(x+mmToPdfBox(1.5f), y-mmToPdfBox(1.7f), 
				width-mmToPdfBox(3), cell.getText(), 
				cell.getFontSize(), cell.getFont(), 
				cell.getAlignment(), out);
	}
	
	/**
	 * Calculates the height of a cell that 
	 * would be drawn in the specified width
	 * 
	 * @param width The width of the cell rectangle
	 * @param cell The cell data
	 * @return The minimum height that would fit the cell
	 */
	private static float getCellHeight(float width, Cell cell) {
		
		// text with margins 1mm top&bottom
		return getParagraphHeight(width, cell.getText(), 
				cell.getFontSize(), cell.getFont()) + mmToPdfBox(1);
	}
	
	/**
	 * Creates a matrix of cells to create a table
	 * of two rows where the first one is for titles
	 * and the second one is for informations.
	 * All strings should be as long as the others.
	 * This method is auxiliary of drawProduct(SectionProduct)
	 * 
	 * @param tableTitles Texts in the 1st row of the table
	 * @param contentAlignments Alignment in each column of the 2nd row
	 * @param tableContents Texts in each column of the 2nd row
	 * @param tableTitleFontSize Font size in the 1st row
	 * @param tableContentFontSize Font size in the 2nd row
	 * @param tableTitleFont Font in the 1st row
	 * @param tableContentFont Font in the 2nd row
	 * @param tableTitleTextColor Color of the text in the 1st row
	 * @param tableTitleBackColor Color of the rectangle in the 1st row
	 * @param tableContentTextColor Color of the text in the 2nd row
	 * @param tableContentBackColor Color of the rect. in the 2nd row
	 * @return The matrix of cells of two rows
	 */
	private Cell[][] createCellsOfProductTable(String[] tableTitles,
			String[] contentAlignments, String[] tableContents, 
			int tableTitleFontSize, int tableContentFontSize, 
			PDFont tableTitleFont, PDFont tableContentFont, 
			Color tableTitleTextColor, Color tableTitleBackColor, 
			Color tableContentTextColor, Color tableContentBackColor) {
		
		Cell[][] cells = new Cell[2][tableTitles.length];
		for(int i=0; i < tableTitles.length; i++ ) {
			cells[0][i] = new Cell(tableTitles[i], tableTitleFont, 
					tableTitleFontSize, tableTitleTextColor, 
					"Left", tableTitleBackColor);
			cells[1][i] = new Cell(tableContents[i], tableContentFont, 
					tableContentFontSize, tableContentTextColor, 
					contentAlignments[i], tableContentBackColor);
		}
		return cells;

	}

	/**
	 * Draws a section in the last page or in
	 * a new one if there is no enough space to
	 * fit the title and its first product.
	 * A section consists of a title and a list
	 * of products.
	 * Draws the section below cursorPosition, and
	 * sets it at the bottom of the last product
	 * 
	 * 
	 * @param section The section to draw
	 * @throws IOException
	 */
	private void drawSection(Section section) throws IOException {
		
		// check if fits in the page
		if (section.getProducts().length > 0) {
			// measure title and first product
			if ( getProductHeight(section.getProducts()[0]) 
					+ getTitleHeight() > cursorPosition 
					- pageMarginBottom )
				
				createPage();
			
		} else { // no products, only title
			if ( getProductHeight(section.getProducts()[0]) 
					> cursorPosition - pageMarginBottom )
				
				createPage();
			
		}
		
		// draw section title
		drawTitle(section.getName());
		
		// draw products
		for (SectionProduct product : section.getProducts()) {
			drawProduct(product);
		}
		
	}
	
	/**
	 * Draws a table listing all sections and
	 * their totals, and a final table with the
	 * total sum and taxes. Draws the tables below
	 * cursorPosition, and sets it below the last
	 * table. If the tables do not fit in the current
	 * page, will add new pages.
	 * This method is used in the summary
	 * of a Budget or Offer with a global total
	 * adding all its sections.
	 * 
	 * @throws IOException
	 */
	private void drawTotal() throws IOException {
		// from sections. Just if globalTotal

		// Table Section Total
		float width = mmToPdfBox(100);
		float columnGap = mmToPdfBox(1);
		float rowGap = mmToPdfBox(0.6f);
		float columnWidth = (width - columnGap) / 2;
		float columnWidths[] = {columnWidth,columnWidth};
		float x = (pageMarginRight - pageMarginLeft - width) / 2 
				+ pageMarginLeft;
		Cell[][] cells = new Cell[budget.getSections().length + 1][2];
		
		int fontSize = 8;
		PDFont fontTitle = this.fontBold;
		PDFont fontContent = this.fontMedium;
		
		Color row1Color = Color.WHITE;
		Color row2Color = new Color(239, 239, 239);
		boolean isRow1 = true;
		
		// title cells
		cells[0][0] = new Cell("Capítulo", fontTitle, fontSize, 
				Color.WHITE, "Left", textColor);
		cells[0][1] = new Cell("Total", fontTitle, fontSize, 
				Color.WHITE, "Left", textColor);
		
		// content cells
		int rowCount = 1;
		String title;
		String content;
		Color currentColor;
		for (Section section : budget.getSections()) {
			currentColor = (isRow1 ? row1Color : row2Color);
			title = section.getName();
			content = formatMoney(section
					.getTotal(budget.getCreationDate()));
			cells[rowCount][0] = new Cell(title, fontContent, 
					fontSize, Color.BLACK, "Left", currentColor);
			cells[rowCount][1] = new Cell(content, fontContent, 
					fontSize, Color.BLACK, "Right", currentColor);
			isRow1 = !isRow1;
			rowCount++;
		}

		float tableHeight = drawTable(x, cursorPosition, 
				columnWidths, rowGap, columnGap, cells);
		cursorPosition -= tableHeight + mmToPdfBox(16);
		
		// Table of total
		width = mmToPdfBox(70);
		x = (pageMarginRight - pageMarginLeft - width) / 2 
				+ pageMarginLeft;
		title = "";
		String netPrice = formatMoney(budget.getTotal());
		String taxes = formatMoney(budget.getTotal() 
				* (budget.getTaxRate()/100) );
		String total = formatMoney(budget.getTotalPlusTaxes());
		
		float height = getSectionTotalHeight(title, width);
		if ( height > cursorPosition - pageMarginBottom ) {
			createPage();
			cursorPosition -= mmToPdfBox(8);
		}
		
		drawSectionTotal(x, cursorPosition, width, title, 
				netPrice, taxes, total);
		cursorPosition -= height + mmToPdfBox(5);

	}
	
	/**
	 * Draws a table of three rows and two columns
	 * showing the total price, taxes and total
	 * with taxes. The table is headed by a title.
	 * This method is used to draw totals in the case
	 * of each section if there is not a global total
	 * or the global total if all sections are added
	 * 
	 * @param x Coordinate of the left side of the table
	 * @param y Coordinate of the top side of the table
	 * @param width The width of the table
	 * @param title The title text of the table
	 * @param netTotal The text as total before taxes
	 * @param taxes The text as amount to add in taxes
	 * @param total The text as total with taxes
	 * @throws IOException
	 */
	private void drawSectionTotal(float x, float y, float width,
			String title, String netTotal, String taxes,
			String total) throws IOException {
		
		PDPageContentStream out = new PDPageContentStream(document,
				page, true, compressEnabled);
		
		// Paragraph
		PDFont font = this.font;
		int fontSize = 14;
		out.setNonStrokingColor(Color.BLACK);
		drawParagraph(x, y, width, title, fontSize, font, "Left", out);
		out.close();
		
		float currentY = y 
				- getParagraphHeight(width, title, fontSize, font);
		
		// Table
		float columnGap = mmToPdfBox(1);
		float rowGap = mmToPdfBox(0.6f);
		float columnWidth = (width - columnGap) / 2;
		float columnWidths[] = {columnWidth,columnWidth};
		Cell[][] cells = new Cell[3][2];
		
		fontSize = 8;
		PDFont fontTitle = this.fontBold;
		PDFont fontContent = this.fontMedium;
		
		cells[0][0] = new Cell("Importe " + (budget.isOffer() ? 
				"oferta" : "presupuesto"), fontTitle, fontSize, 
				Color.WHITE, "Right", textColor);
		cells[1][0] = new Cell("Impuestos I.V.A.", fontTitle, 
				fontSize, Color.WHITE, "Right", textColor);
		cells[2][0] = new Cell("TOTAL " + (budget.isOffer() ? 
				"OFERTA" : "PRESUPUESTO"), fontTitle, fontSize, 
				Color.WHITE, "Right", textColor);
		cells[0][1] = new Cell(netTotal, fontContent, fontSize, 
				Color.BLACK, "Center", Color.WHITE);
		cells[1][1] = new Cell(taxes, fontContent, fontSize, 
				Color.BLACK, "Center", Color.WHITE);
		cells[2][1] = new Cell(total, fontTitle, fontSize, 
				Color.WHITE, "Center", textColor);
		
		drawTable(x, currentY, columnWidths, rowGap, columnGap, cells);
		
	}
	
	/**
	 * Calculates the height of a table of three 
	 * rows and two columns with the specified width,
	 * showing the total price, taxes and total with
	 * taxes. The table would be headed by a title,
	 * that is counted in the height.
	 * This method is used to ensure that the table
	 * fits in the page before drawing it
	 * 
	 * @param title The title text of the table
	 * @param width The width of the table
	 * @return The height of the table
	 */
	private float getSectionTotalHeight(String title, float width) {
		// measure title
		PDFont font = this.font;
		int fontSize = 14;
		float height = getParagraphHeight(width, title, fontSize, font);
		
		// measure table
		float columnGap = mmToPdfBox(1);
		float rowGap = mmToPdfBox(0.6f);
		float columnWidth = (width - columnGap) / 2;
		float columnWidths[] = {columnWidth};
		Cell[][] cells = new Cell[3][1];
		
		fontSize = 8;
		PDFont fontTitle = this.fontBold;
		cells[0][0] = new Cell("Importe presupuesto", fontTitle,
				fontSize, Color.WHITE, "Right", textColor);
		cells[1][0] = new Cell("Impuestos I.V.A.", fontTitle,
				fontSize, Color.WHITE, "Right", textColor);
		cells[2][0] = new Cell("TOTAL PRESUPUESTO", fontTitle,
				fontSize, Color.WHITE, "Right", textColor);
		height += getTableHeight(columnWidths, rowGap, cells);
		
		return height;
	}
	
	/**
	 * Draws a summary containing the contact
	 * information of the client and the commercial.
	 * This method should be called after
	 * creating a new page.
	 * Sets cursorPosition after the last line
	 * 
	 * @throws IOException
	 */
	private void drawSummary() throws IOException {

		String tmpText = (budget.isOffer() ? "Resumen de la oferta"
				: "Resumen del presupuesto");
		drawTitle(tmpText);
		
		PDPageContentStream out = new PDPageContentStream(document, 
				page, true, compressEnabled);
		
		int fontSize = 12;
		PDFont font = fontLight;
		
		// draw Reference and Date
		out.setNonStrokingColor(Color.BLACK);
		float currentCursor = cursorPosition;
		
		tmpText = (budget.isOffer() ? "Oferta válida hasta el "
				: "Presupuesto válido hasta el ") 
				+ budget.getExpirationDateString().replace("/", ".");
		
		writeLineFromRight(font, fontSize, tmpText, out);
		
		tmpText = (budget.getConstructionRef().length() > 0 
				? "Ref. de obra: " + budget.getConstructionRef() 
						: "");
		drawParagraph(pageMarginLeft, currentCursor, 
				(pageMarginRight - pageMarginLeft) / 2, 
				tmpText, fontSize, font, "Left",  out);
		
		// adjust cursor to write the next data
		cursorPosition = Math.min(cursorPosition, currentCursor 
				- getParagraphHeight(PAGESIZE.getUpperRightX() / 2, 
						tmpText, fontSize, font) );
		cursorPosition -= mmToPdfBox(12);
		
		// Draw photo 35mm width only if it has a photo
		File image = budget.getClient().getImage();
		float imageWidth = mmToPdfBox(35);
		float marginRight = pageMarginRight;
		if (!image.getAbsolutePath().endsWith("default.png")) {
			marginRight = pageMarginRight - imageWidth;
			float imageHeight = getScalatedHeight(image, 
					imageWidth);
			
			drawImage(out, image, marginRight, cursorPosition, 
					imageWidth, imageHeight);
		}
				
		// write contact info
		out.setNonStrokingColor(textColor);
		writeLine(font, fontSize, "Datos del cliente:", out);
		writeLine(font, fontSize, "", out);
		out.setNonStrokingColor(Color.BLACK);
		
		tmpText = budget.getClient().getName() + ", NIF " 
				+ budget.getClient().getClientNumber();
		
		drawParagraph(pageMarginLeft, cursorPosition, 
				marginRight - pageMarginLeft, tmpText, 
				fontSize, font, "Left",  out);
		cursorPosition -= getParagraphHeight(marginRight 
				- pageMarginLeft, tmpText, fontSize, font);
		
		tmpText = budget.getClient().getAddress() + 
				(budget.getClient().getAddress().length() > 0 && 
						budget.getClient().getPostalCode().length() +
						budget.getClient().getTown().length() +
						budget.getClient().getProvince().length() +
						budget.getClient().getCountry().length() > 0 
						? ", " : "")
				+ budget.getClient().getPostalCode() + 
				(budget.getClient().getPostalCode().length() > 0 
						? " " : "")
				+ budget.getClient().getTown() + 
				(budget.getClient().getTown().length() > 0 &&
						budget.getClient().getProvince().length() > 0
						? " - " : "")
				+ budget.getClient().getProvince() +
				(budget.getClient().getTown().length() +
						budget.getClient().getProvince().length() > 0
						&& budget.getClient().getCountry().length() > 0
						? ". " : "")
				+ budget.getClient().getCountry();
		
		if (tmpText.length() > 0) {
			drawParagraph(pageMarginLeft, cursorPosition, 
					marginRight - pageMarginLeft, tmpText, 
					fontSize, font, "Left",  out);
			cursorPosition -= getParagraphHeight(marginRight 
					- pageMarginLeft, tmpText, fontSize, font);
		}
		
		tmpText = budget.getClient().getPhone() + 
				(budget.getClient().getPhone().length() > 0 &&
						budget.getClient().getEmail().length() > 0 
						? ", " : "")
				+ budget.getClient().getEmail();
		
		if (tmpText.length() > 0) {
			drawParagraph(pageMarginLeft, cursorPosition, 
					marginRight - pageMarginLeft, tmpText, 
					fontSize, font, "Left",  out);
			cursorPosition -= getParagraphHeight(marginRight 
					- pageMarginLeft, tmpText, fontSize, font);
		}
		
		writeLine(font, fontSize, "", out);
		writeLine(font, fontSize, "", out);
		
		out.setNonStrokingColor(textColor);
		writeLine(font, fontSize, "Datos del comercial:", out);
		writeLine(font, fontSize, "", out);
		out.setNonStrokingColor(Color.BLACK);
		
		tmpText = budget.getSalesperson().getName() + ", "
				+ budget.getSalesperson().getEmail();
		
		drawParagraph(pageMarginLeft, cursorPosition, 
				marginRight - pageMarginLeft, tmpText, 
				fontSize, font, "Left",  out);
		cursorPosition -= getParagraphHeight(marginRight 
				- pageMarginLeft, tmpText, fontSize, font);
		
		tmpText = COMPANYINFO1;
		
		drawParagraph(pageMarginLeft, cursorPosition, 
				marginRight - pageMarginLeft, tmpText, 
				fontSize, font, "Left",  out);
		cursorPosition -= getParagraphHeight(marginRight 
				- pageMarginLeft, tmpText, fontSize, font);
		
		tmpText = COMPANYINFO2;
		
		drawParagraph(pageMarginLeft, cursorPosition, 
				marginRight - pageMarginLeft, tmpText, 
				fontSize, font, "Left",  out);
		cursorPosition -= getParagraphHeight(marginRight 
				- pageMarginLeft, tmpText, fontSize, font);
		
		tmpText = COMPANYINFO3;
		
		drawParagraph(pageMarginLeft, cursorPosition, 
				marginRight - pageMarginLeft, tmpText, 
				fontSize, font, "Left",  out);
		cursorPosition -= getParagraphHeight(marginRight 
				- pageMarginLeft, tmpText, fontSize, font);
		
		// last margin
		cursorPosition -= mmToPdfBox(12);

		out.close();

	}
	
	/**
	 * Draws the final note of the budget in the bottom 
	 * of the current page or, if it does not fit, 
	 * in the bottom of a new page. Uses cursorPosition
	 * to check whether it fits, and sets cursorPosition
	 * on the pageMarginBottom
	 * 
	 * @throws IOException
	 */
	private void drawNote() throws IOException {

		String text = budget.getNote();
		
		// font and fontSize declaration
		PDFont font = fontLight;
		int fontSize = 10;
		
		float height = getParagraphHeight(pageMarginRight 
				- pageMarginLeft, text, fontSize, font);
		
		if ( height > cursorPosition - pageMarginBottom )
			createPage();
		
		// draw note
		PDPageContentStream out = new PDPageContentStream(document,
				page, true, compressEnabled);
		out.setNonStrokingColor(textColor); // colored text
		
		drawParagraph(pageMarginLeft, pageMarginBottom + height, 
				pageMarginRight - pageMarginLeft, text, fontSize, 
				font, "Left", out);
		
		out.close();
		
		cursorPosition = pageMarginBottom;
		
	}
	
	/**
	 * Sets textColor and bwColor attributes.
	 * Uses the colors of the top right quarter
	 * of the cover image, so it should be called
	 * after loading the budget with the cover.
	 * Sets bwColor Black or White depending
	 * on the color of the cover, and textColor
	 * with the same color as the cover, darkening
	 * it if it is too clear
	 * 
	 * @throws IOException
	 */
	private void setColors() throws IOException {
		
		// select the top right quarter of the image
		BufferedImage coverImg = ImageIO
				.read(budget.getCover().getImage());
		
		BufferedImage bgTopRight = coverImg.getSubimage(coverImg
				.getWidth() / 2, 0, coverImg.getWidth() / 2, 
				coverImg.getHeight() / 2);
		
		long redBucket = 0;
		long greenBucket = 0;
		long blueBucket = 0;
		long pixelCount = 0;
		int color;

		// calculate a color as the average color of the area
		for (int y = 0; y < bgTopRight.getHeight(); y++)
		{
		    for (int x = 0; x < bgTopRight.getWidth(); x++) {
		        color = bgTopRight.getRGB(x, y);
		        redBucket += (color & 0x00ff0000) >> 16;
		    	greenBucket += (color & 0x0000ff00) >> 8;
		        blueBucket += color & 0x000000ff;
		        
		        pixelCount++;
		    }
		}
		redBucket = redBucket / pixelCount;
		greenBucket = greenBucket / pixelCount;
		blueBucket = blueBucket / pixelCount;
		
		// choose font black or white to contrast the background
		bwColor = ( (redBucket + greenBucket*2 + blueBucket /2 ) 
				> 382 ) ? Color.BLACK : Color.WHITE;
			
		// make the background dark enough to contrast on the white
		textColor = new Color((int)redBucket, 
				(int)greenBucket, (int)blueBucket);
		
		while ( (textColor.getRed() + textColor.getGreen()*2 
				+ textColor.getBlue()/2 ) > 382 ) {
			textColor = textColor.darker();
		}
		
	}
	
	/**
	 * Converts the size in millimeters to the
	 * size in the PdfBox units. Should use an
	 * specific method for the paper size.
	 * Returns the input if the paper size is
	 * not implemented.
	 * 
	 * @param mm Millimeters
	 * @return Length in PdfBox units
	 */
	protected static float mmToPdfBox(float mm) {
		// can contain a switch for each PageSize
		if (PAGESIZE.equals(PDPage.PAGE_SIZE_A4))
			return mmToPdfBoxA4(mm);
		else {
			return mm;
		}
	}
	
	/**
	 * Converts the size in millimeters to the
	 * size in PdfBox units for a sheet of size
	 * A4 (297mm high).
	 * 
	 * @param mm Millimeters
	 * @return Length un PdfBox units for an A4 sheet
	 */
	private static float mmToPdfBoxA4(float mm) {
		// A4 sheets have 297mm height
		return mm * (PAGESIZE.getHeight() / 297);
	}
	
	/**
	 * Converts a quantity into an string like
	 * 0,00 EUR
	 * 
	 * @param quantity Money quantity
	 * @return String with the money quantity
	 */
	protected static String formatMoney(float quantity) {
        DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.GERMAN);
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
		return df.format(quantity) + " EUR";
	}
	
	/**
	 * Converts a number to a String with
	 * 0, 1 or 2 decimal positions, such as:
	 * -0.11545 to -0,12
	 * 0.1000 to 0,1
	 * 2.00 to 2
	 * 
	 * @param quantity The number
	 * @return String with the number
	 */
	protected static String formatNumber(float quantity) {
        DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.GERMAN);
		df.setMaximumFractionDigits(2);
		return df.format(quantity);
	}
	
	/**
	 * Converts a number into a percentage
	 * format with two decimals such as:
	 * -0.11545 to -0,12%
	 * 0.1000 to 0,10%
	 * 2.00 to 2,00%
	 * 
	 * 
	 * @param quantity
	 * @return
	 */
	protected static String formatPercentage(float quantity) {
        DecimalFormat df = (DecimalFormat)NumberFormat.getNumberInstance(Locale.GERMAN);
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
		return df.format(quantity) + "%";
	}
	
	/**
	 * Merges the attachments of the budget with
	 * the PDF, appending them at the end of the
	 * document
	 * 
	 * @throws IOException 
	 */
	private void mergeAttachments() throws IOException {
 
		PDDocument part;
	    PDFMergerUtility merger = new PDFMergerUtility();

	    for (Attachment attachment : budget.getAttachments()) {
		    part = PDDocument.load(attachment.getAttachment());
		    merger.appendDocument(document, part);
		    merger.mergeDocuments();
		    part.close();
	    }

	}

}
