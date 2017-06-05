package tsvparser.utilities;

/**
 * Container class representing a single row in the original TSV.
 * 
 * @author Malcolm Riley
 */
public class TableEntry {
	
	private final String TITLE;
	private final String PAGE_ID;
	private final String REVISION_ID;
	private final String YEAR;
	private final String TEXT_INTRO;
	private final String TEXT_FULL;
	private final String CATEGORIES;
	
	/**
	 * Constructs a new {@link TableEntry} using the passed values.
	 * 
	 * @param passedTitle - The Title of the corresponding page
	 * @param passedPageID - The PageID of the corresponding page
	 * @param passedRevisionID - The Revision ID of the corresponding page
	 * @param passedYear - The year of the corresponding page
	 * @param passedTextIntro - The introduction text of the corresponding page
	 * @param passedTextFull - The full plaintext of the corresponding page
	 * @param passedCategories - The categories list of the corresponding page
	 */
	public TableEntry(String passedTitle, String passedPageID, String passedRevisionID, String passedYear, String passedTextIntro, String passedTextFull, String passedCategories) {
		this.TITLE = passedTitle;
		this.PAGE_ID = passedPageID;
		this.REVISION_ID = passedRevisionID;
		this.YEAR = passedYear;
		this.TEXT_INTRO = passedTextIntro;
		this.TEXT_FULL = passedTextFull;
		this.CATEGORIES = passedCategories;
	}
	
	/**
	 * Returns the Title of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The title.
	 */
	public String getTitle() {
		return this.TITLE;
	}
	
	/**
	 * Returns the Page ID of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The Page ID.
	 */
	public String getPageID() {
		return this.PAGE_ID;
	}
	
	/**
	 * Returns the Revision ID of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The Revision ID.
	 */
	public String getRevisionID() {
		return this.PAGE_ID;
	}
	
	/**
	 * Returns the year of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The year.
	 */
	public String getYear() {
		return this.YEAR;
	}
	
	/**
	 * Returns the introduction text of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The introductory text.
	 */
	public String getIntroText() {
		return this.TEXT_INTRO;
	}
	
	/**
	 * Returns the full text of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The full text.
	 */
	public String getFullText() {
		return this.TEXT_INTRO;
	}
	
	/**
	 * Returns the categories list of the page corresponding to this {@link TableEntry}.
	 * 
	 * @return The categories list.
	 */
	public String getCategoryList() {
		return this.CATEGORIES;
	}
}
