package wikiscrape.queries;

/**
 * Defines a list of known Wiki query API calls.
 *
 * @author Malcolm Riley
 */
public class Queries {

	private Queries() {
	};
	
	// Strings
	private static final String STRING_EXTRACT = "extract";
	private static final String STRING_PROPERTIES = "prop";
	private static final String STRING_REVISIONS = "revisions";
	
	// Known Actions
	public static final String QUERY = "query";
	
	// Known Arguments
	public static final Argument PAGES_BY_ID = new Argument("pageids");
	public static final Argument PAGES_BY_TITLE = new Argument("titles");
	public static final Argument PAGES_BY_REVISION_ID = new Argument("revids");
	
	public static final Argument REVISION_IDS = new Argument("ids", "revids");
	public static final Argument REVISION_FLAGS = new Argument("flags", "minor");
	public static final Argument REVISION_SIZE = new Argument("size", "size");
	
	public static final Argument OPTION_EXTRACTS_PLAIN = new Argument("plain", STRING_EXTRACT);
	public static final Argument OPTION_EXTRACTS_WIKI = new Argument("wiki", STRING_EXTRACT);
	public static final Argument OPTION_EXTRACTS_RAW = new Argument("raw", STRING_EXTRACT);
	
	public static final Argument REVISIONS = new Argument(STRING_REVISIONS, STRING_REVISIONS);
	
	// Known "Get" Queries
	public static final QueryBuilder GET_PLAINTEXT = new QueryBuilder("explaintext", STRING_EXTRACT);
	public static final QueryBuilder GET_PROPERTIES = new QueryBuilder(new Argument(STRING_PROPERTIES));
	
	// Optional Queries
	public static final QueryBuilder OPTION_REVISIONS = new QueryBuilder("rvprop", STRING_REVISIONS);
}
