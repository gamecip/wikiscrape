package queries;

/**
 * Defines a list of known Wiki query API calls.
 * 
 * @author Malcolm Riley
 */
public class QueryParameters {
	
	private QueryParameters(){};
	
	// Page specification parameters
	public static final String PAGE_IDS = "pageids";
	public static final String PAGE_TITLES = "titles";
	@Deprecated
	/**
	 * Polling Wikipedia pages by revision ID is reportedly very expensive for their servers.
	 * <p>
	 * The query parameter is included here for completeness but use of it should always be avoided if possible.
	 */
	public static final String PAGE_REVISION_IDS = "revids";
	
}
