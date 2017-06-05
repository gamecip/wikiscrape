package wikiscrape.queries;

import wikiscrape.ScraperConfig;
import wikiscrape.utilities.ScrapeUtilities;

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
	
	// Known Arguments
	public static final Argument REVISION_IDS = new Argument("ids", "revids");
	public static final Argument REVISION_FLAGS = new Argument("flags", "minor");
	public static final Argument REVISION_SIZE = new Argument("size", "size");
	
	public static final Argument OPTION_EXTRACTS_PLAIN = new Argument("plain", EXTRACT);
	public static final Argument OPTION_EXTRACTS_WIKI = new Argument("wiki", EXTRACT);
	public static final Argument OPTION_EXTRACTS_RAW = new Argument("raw", EXTRACT);
	
	public static final Argument REVISIONS = new Argument("revisions", "revisions");
	
	// Known Queries
	public static final Query GET_PLAINTEXT = new Query("explaintext", EXTRACT);
	public static final Query GET_PROPERTIES = new Query(new Argument(PROPERTIES));
	
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
		String baseURL = String.format("%s%s", ScraperConfig.WIKI_URL, ScrapeUtilities.getQueryTerm(ACTION, QUERY));
		String pageSpecification = ScrapeUtilities.getQueryTerm(passedPageSpecification.getType(), passedPageStrings);
		return ScrapeUtilities.concatenateCommands(baseURL, pageSpecification);
	}
}