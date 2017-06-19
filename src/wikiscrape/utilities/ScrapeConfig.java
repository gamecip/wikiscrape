package wikiscrape.utilities;

public class ScrapeConfig {
	
	private String USERNAME;
	private String PASSWORD;
	private String SQL_URL;
	private String WIKI_URL;
	private String TABLENAME;
	
	public String getUsername() {
		return USERNAME;
	}
	
	public String getPassword() {
		return PASSWORD;
	}
	
	public String getSQLURL() {
		return SQL_URL;
	}
	
	public String getTableName() {
		return TABLENAME;
	}
	
	public String getWikiURL() {
		return WIKI_URL;
	}
	
	public void clear() {
		this.USERNAME = null;
		this.PASSWORD = null;
		this.SQL_URL = null;
		this.TABLENAME = null;
		this.WIKI_URL = null;
	}
}
