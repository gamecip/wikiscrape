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
	public static final String STRING_EXTRACT = "extract";
	public static final String STRING_PROPERTIES = "prop";
	public static final String STRING_PAGEIDS = "pageids";
	
	public static final String FIELD_QUERY = "query";
	public static final String FIELD_PAGES = "pages";
	public static final String FIELD_PAGEID = "pageid";
	public static final String FIELD_MISSING = "missing";
	public static final String FIELD_REVISIONS = "revisions";
	public static final String FIELD_REVID = "revid";
	public static final String FIELD_BATCH_COMPLETE = "batchcomplete";
	public static final String FIELD_CONTINUE = "continue";
	public static final String FIELD_CONTINUE_FROM = "accontinue";
	public static final String FIELD_PAGETITLE = "title";
	
	// Known Actions
	public static final String ACTION_QUERY = "action=query";
	
	// Known Arguments	
	public static final Argument EXTRACTS = new Argument("extracts");
	public static final Argument CATEGORIES = new Argument("categories");
	
	public static final Argument PAGES_BY_ID = new Argument(STRING_PAGEIDS, STRING_PAGEIDS);
	public static final Argument PAGES_BY_TITLE = new Argument("titles");
	public static final Argument PAGES_BY_REVISION_ID = new Argument("revids");
	
	public static final Argument REVISION_IDS = new Argument("ids", "revids");
	public static final Argument REVISION_FLAGS = new Argument("flags", "minor");
	public static final Argument REVISION_SIZE = new Argument("size", "size");
	
	public static final Argument OPTION_SECTIONFORMAT_PLAIN = new Argument("plain", STRING_EXTRACT);
	public static final Argument OPTION_SECTIONFORMAT_WIKI = new Argument("wiki", STRING_EXTRACT);
	public static final Argument OPTION_SECTIONFORMAT_RAW = new Argument("raw", STRING_EXTRACT);
	public static final Argument OPTION_INDEX_PAGEIDS = new Argument("indexpageids", STRING_PAGEIDS);
	
	public static final Argument REVISIONS = new Argument(FIELD_REVISIONS, FIELD_REVISIONS);
	
	// Known "Get" Queries - Clone these
	public static final QueryBuilder GET_PROPERTIES = new QueryBuilder(new Argument(STRING_PROPERTIES));
	
	// Optional Queries - Clone these
	public static final QueryBuilder OPTION_REVISIONS = new QueryBuilder("rvprop", FIELD_REVISIONS);
	public static final QueryBuilder OPTION_SECTIONFORMAT = new QueryBuilder("exsectionformat", STRING_EXTRACT);
	public static final QueryBuilder OPTION_EXTRACTLIMIT = new QueryBuilder("exlimit", STRING_EXTRACT);
	public static final QueryBuilder OPTION_EXTRACT_PLAINTEXT = new QueryBuilder("explaintext", STRING_EXTRACT);
	public static final QueryBuilder OPTION_CONTINUE = new QueryBuilder(FIELD_CONTINUE, FIELD_CONTINUE);
	public static final QueryBuilder OPTION_CONTINUE_FROM = new QueryBuilder(FIELD_CONTINUE_FROM, FIELD_CONTINUE_FROM);
	
	/**
	 * Convenience Factory method that {@link #clone()}s the passed {@link QueryBuilder} and calls {@link QueryBuilder#setOptions(QueryBuilder...)} on it.
	 * 
	 * @param passedQuery - The {@link QueryBuilder} to {@link #clone()}
	 * @param passedOptions - The {@link QueryBuilder} instances to set as options
	 * @return A new {@link QueryBuilder} instance, with the passed options.
	 */
	public static QueryBuilder newWith(QueryBuilder passedQuery, QueryBuilder ... passedOptions) {
		return passedQuery.clone().setOptions(passedOptions);
	}
	
	/**
	 * Convenience Factory method that {@link #clone()}s the passed {@link QueryBuilder} and calls {@link QueryBuilder#setArguments(Argument...)} on it.
	 * 
	 * @param passedQuery - The {@link QueryBuilder} to {@link #clone()}
	 * @param passedArguments - The {@link Argument} instances to set as arguments
	 * @return A new {@link QueryBuilder} instance, with the passed arguments.
	 */
	public static QueryBuilder newWith(QueryBuilder passedQuery, Argument ... passedArguments) {
		return passedQuery.clone().setArguments(passedArguments);
	}
	
	/**
	 * Convenience Factory method that {@link #clone()}s the passed {@link QueryBuilder} and calls both {@link QueryBuilder#setCacheOptions(String)} and {@link QueryBuilder#setArguments(Argument...)} on it.
	 * 
	 * @param passedQuery - The {@link QueryBuilder} to {@link #clone()}
	 * @param passedArguments - The {@link Argument} instances to set as arguments
	 * @param passedOptions - The {@link QueryBuilder} instances to set as options
	 * @return A new {@link QueryBuilder} instance, with the passed arguments and options.
	 */
	public static QueryBuilder newWith(QueryBuilder passedQuery, Argument[] passedArguments, QueryBuilder[] passedOptions) {
		return passedQuery.clone().setArguments(passedArguments).setOptions(passedOptions);
	}
}
