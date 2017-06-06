package wikiscrape;

import wikiscrape.queries.Queries;
import wikiscrape.queries.QueryBuilder;
import wikiscrape.utilities.RequestManager;

public class WikiScraper {
	
	private static final int MAX_QUERY_SIZE = 50;
	private static final int MAX_PLAINTEXT_EXTRACTS = 20;

	public static void main(String[] passedArguments) {
		
		// Construct internal objects
		String wiki_url = "en.wikipedia.org";
		RequestManager requestManager = new RequestManager(wiki_url);
		QueryBuilder query = new QueryBuilder(Queries.PAGES_BY_ID);
		
		// Build page update list from database
		
		// Of page list, compare revision ids to build list of extracts to redownload
		
		// Redownload extracts
		
		// Push to database
	}
}
