package queries;

import utilities.StringUtilities;
import wikiscrape.ScraperConfig;

/**
 * Defines a list of known Wiki query API calls.
 *
 * @author Malcolm Riley
 */
public class QueryFactory {

	private QueryFactory() {
	};
	
	private static final String ACTION = "action";
	private static final String QUERY = "query";
	
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
		String baseURL = String.format("%s%s", ScraperConfig.WIKI_URL, StringUtilities.getQueryTerm(ACTION, QUERY));
		String pageSpecification = StringUtilities.getQueryTerm(passedPageSpecification.getType(), passedPageStrings);
		return StringUtilities.concatenateCommands(baseURL, pageSpecification);
	}

//	/* Revision metadata queries */
//	public static final QueryCommand OPTION_REVISIONS_IDS = new QueryCommand("ids", "revids");
//	public static final QueryCommand OPTION_REVISIONS_FLAGS = new QueryCommand("flags", "minor");
//	public static final QueryCommand OPTION_REVISIONS_SIZE = new QueryCommand("size", "size");
//	public static final QueryCommand ARGUMENT_REVISIONS = new QueryCommand("rvprop", "revisions", OPTION_REVISIONS_IDS, OPTION_REVISIONS_FLAGS, OPTION_REVISIONS_SIZE);
//
//	/* Page content extraction queries */
//	public static final QueryCommand OPTION_EXTRACTS_PLAIN = new QueryCommand("plain", "extract");
//	public static final QueryCommand OPTION_EXTRACTS_WIKI = new QueryCommand("wiki", "extract");
//	public static final QueryCommand OPTION_EXTRACTS_RAW = new QueryCommand("raw", "extract");
//	public static final QueryCommand ARGUMENT_SECTIONFORMAT = new QueryCommand("exsectionformat", "extract", OPTION_EXTRACTS_PLAIN, OPTION_EXTRACTS_WIKI, OPTION_EXTRACTS_RAW);
//	public static final QueryCommand ARGUMENT_PLAINTEXT = new QueryCommand("explaintext", "extract");
//
//	public static final String TYPE_PROPERTIES = "prop";
}
