package wikiscrape.utilities;

/**
 * Object that represents the various configuration settings for this run of the {@link WikiScraper}.
 * 
 * @author Malcolm Riley
 */
public class ScrapeConfig {
	
	private String USERNAME;
	private String PASSWORD;
	private String SQL_URL;
	private String WIKI_URL;
	private String TABLENAME;
	private String[] LIST_PAGES;
	
	/**
	 * Returns the username in the SQL database to use
	 * 
	 * @return - The SQL username to use
	 */
	public String getUsername() {
		return USERNAME;
	}
	
	/**
	 * Returns the password of the specified user in the SQL database
	 * 
	 * @return - The password to use for the SQL database
	 */
	public String getPassword() {
		return PASSWORD;
	}
	
	/**
	 * The link to the SQL database, through the specified driver
	 * 
	 * @return - The URL to the SQL database to use
	 */
	public String getSQLURL() {
		return SQL_URL;
	}
	
	/**
	 * Returns the name of the table within the SQL database to use
	 * 
	 * @return - The name of the table to use within the specified SQL database
	 */
	public String getTableName() {
		return TABLENAME;
	}
	
	/**
	 * The URL of the wiki to use for requests
	 * 
	 * @return - The URL of the wiki to use
	 */
	public String getWikiURL() {
		return WIKI_URL;
	}

	/**
	 * Returns an array of Strings representing the URLS to the pages that should be used for the scrape.
	 * These are the "List" pages that will be polled for the actual pages to be used during the scrape.
	 * 
	 * @return - An array of all List pages to use for the scrape
	 */
	public String[] getListPages() {
		return LIST_PAGES;
	}
	
	public void clear() {
		this.USERNAME = "";
		this.PASSWORD = "";
		this.SQL_URL = "";
		this.TABLENAME = "";
		this.WIKI_URL = "";
		this.LIST_PAGES = new String[]{};
	}
}
