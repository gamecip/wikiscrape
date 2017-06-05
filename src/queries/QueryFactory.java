package queries;

import utilities.Utilities;
import wikiscrape.ScraperConfig;

/**
 * Defines a list of known Wiki query API calls.
 *
 * @author Malcolm Riley
 */
public class QueryFactory {

	private QueryFactory() {
	};
	
	// Strings
	private static final String ACTION = "action";
	private static final String QUERY = "query";
	private static final String EXTRACT = "extract";
	private static final String PROPERTIES = "prop";
	
	// Arguments
	public static final Argument REVISION_IDS = new Argument("ids", "revids");
	public static final Argument REVISION_FLAGS = new Argument("flags", "minor");
	public static final Argument REVISION_SIZE = new Argument("size", "size");
	
	public static final Argument EXTRACTS_PLAIN = new Argument("plain", EXTRACT);
	public static final Argument EXTRACTS_WIKI = new Argument("wiki", EXTRACT);
	public static final Argument EXTRACTS_RAW = new Argument("raw", EXTRACT);
	
	// Known Queries
	public static final Query GET_PLAINTEXT = new Query("explaintext", EXTRACT);
	public static final Query GET_REVISIONS = new Query("rvprop", "revisions");
	
	// Specifying Pages
	public static enum PageSpecification {

		BY_IDS("pageids"),
		BY_TITLES("titles"),
		BY_REVISION_IDS("revids"),;

		private String TYPE;

		private PageSpecification(String passedArgument) {
			this.TYPE = passedArgument;
		}

		public String getType() {
			return this.TYPE;
		}
	}
	
	public static String getQueryURL(PageSpecification passedPageSpecification, String ... passedPageStrings) {
		String baseURL = String.format("%s%s", ScraperConfig.WIKI_URL, Utilities.getQueryTerm(ACTION, QUERY));
		String pageSpecification = Utilities.getQueryTerm(passedPageSpecification.getType(), passedPageStrings);
		return Utilities.concatenateCommands(baseURL, pageSpecification);
	}
}
