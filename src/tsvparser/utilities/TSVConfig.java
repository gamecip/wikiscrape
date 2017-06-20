package tsvparser.utilities;

public class TSVConfig {

	private String USERNAME;
	private String PASSWORD;
	private String SQL_URL;
	private String TSV_DIRECTORY;
	private String TABLENAME;
	
	/**
	 * Returns the username in the SQL database to use
	 * 
	 * @return - The SQL username to use
	 */
	public String getUsername() {
		return this.USERNAME;
	}
	
	/**
	 * Returns the password of the specified user in the SQL database
	 * 
	 * @return - The password to use for the SQL database
	 */
	public String getPassword() {
		return this.PASSWORD;
	}
	
	/**
	 * The link to the SQL database, through the specified driver
	 * 
	 * @return - The URL to the SQL database to use
	 */
	public String getSQLURL() {
		return this.SQL_URL;
	}
	
	/**
	 * Returns the name of the table within the SQL database to use
	 * 
	 * @return - The name of the table to use within the specified SQL database
	 */
	public String getTableName() {
		return this.TABLENAME;
	}
	
	/**
	 * The URL of the wiki to use for requests
	 * 
	 * @return - The URL of the wiki to use
	 */
	public String getDirectory() {
		return this.TSV_DIRECTORY;
	}
	
	public void clear() {
		this.USERNAME = "";
		this.PASSWORD = "";
		this.SQL_URL = "";
		this.TABLENAME = "";
		this.TSV_DIRECTORY = "";
	}

}
