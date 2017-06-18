package wikiscrape;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sqlinterface.EnumEntry;
import sqlinterface.SQLInterface;
import sqlinterface.TableEntry;
import wikiscrape.queries.Argument;
import wikiscrape.queries.Queries;
import wikiscrape.queries.QueryBuilder;
import wikiscrape.utilities.BatchIterator;
import wikiscrape.utilities.QueryIterator;
import wikiscrape.utilities.RequestManager;
import wikiscrape.utilities.ScrapeUtilities;

public class WikiScraper {

	private static final int MAX_QUERY_SIZE = 50;
	private static final int MAX_PLAINTEXT_EXTRACTS = 20;

	public static void main(String[] passedArguments) {

		// TODO: Parse launch arguments passed to application, use as arguments to objects here

		// Construct internal objects
		String wiki_url = "en.wikipedia.org";
		RequestManager scraper = new RequestManager(wiki_url);
		QueryBuilder query = new QueryBuilder(Queries.PAGES_BY_ID);

		// TODO: Solution to username password security problem
		String sqlurl = "jdbc:mysql://localhost:3306/"; // see https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html
		String tablename = "";
		String username = "";
		String password = "";

		try {
			SQLInterface sqlInterface = new SQLInterface(sqlurl, tablename, username, password);

			// Build page update map from database
			ResultSet results = sqlInterface.select(EnumEntry.PAGE_ID, EnumEntry.REVISION_ID);
			HashMap<String, String> updateMap = new HashMap<String, String>();
			while (results.next()) {
				String pageID = results.getString(0);
				String revisionID = results.getString(1);
				updateMap.put(pageID, revisionID);
			}

			// TODO: Mode for discovery of new pages

			// Of page list, compare revision ids to build list of extracts to redownload
			BatchIterator<String> iterator = new BatchIterator<String>(updateMap.keySet().toArray(new String[] {}), MAX_QUERY_SIZE);
			ArrayList<String> requiresUpdate = new ArrayList<String>();

			query.setOptions(getRevisionsQuery());
			QueryBuilder optionContinuation = Queries.OPTION_CONTINUE.clone();
			QueryBuilder optionContinuationFrom = Queries.OPTION_CONTINUE_FROM.clone();
			optionContinuation.setOptions(optionContinuationFrom);

			for (List<String> iteratedList : iterator) {
				Argument[] pages = ScrapeUtilities.fromStrings(iteratedList);
				query.setArguments(pages);
				QueryIterator queryIterator = new QueryIterator(scraper, query);
				
				for (JsonObject returnedJson : queryIterator) {
					// Perform actual query
					JsonArray array = returnedJson.getAsJsonObject(Queries.FIELD_QUERY).getAsJsonArray(Queries.FIELD_PAGES);
					for (JsonElement iteratedElement : array) {
						if (!iteratedElement.getAsJsonObject().has(Queries.FIELD_MISSING)) {
							String discoveredPageID = iteratedElement.getAsJsonObject().get(Queries.FIELD_PAGEID).getAsString();
							String discoveredRevisionID = iteratedElement.getAsJsonObject().getAsJsonArray(Queries.FIELD_REVISIONS).get(0).getAsJsonObject().get(Queries.FIELD_REVID).getAsString();
							String storedRevisionID = updateMap.get(discoveredPageID);

							if (!storedRevisionID.equals(discoveredRevisionID)) {
								requiresUpdate.add(discoveredPageID);
							}
						}
						else {
							// TODO: Additional handling?
							System.out.println(String.format("Page with id %d is missing!", iteratedElement.getAsJsonObject().get(Queries.FIELD_PAGEID)));
						}
					}
				}
			}

			// Redownload extracts
			iterator = new BatchIterator<String>(requiresUpdate, MAX_PLAINTEXT_EXTRACTS);
			ArrayList<TableEntry> updatedEntries = new ArrayList<TableEntry>();
			query.setOptions(getExtractsQuery());
			for (List<String> iteratedList : iterator) {
				Argument[] pages = ScrapeUtilities.fromStrings(iteratedList);
				query.setArguments(pages);
				QueryIterator queryIterator = new QueryIterator(scraper, query);
				
				for (JsonObject returnedJson : queryIterator) {
					JsonArray array = returnedJson.getAsJsonObject(Queries.FIELD_QUERY).getAsJsonArray(Queries.FIELD_PAGES);
					for (JsonElement iteratedElement : array) {
						if (!iteratedElement.getAsJsonObject().has(Queries.FIELD_MISSING)) {
							
							// TODO: Edit tableEntries
							
						}
						else {
							System.out.println(String.format("Page with id %d is missing!", iteratedElement.getAsJsonObject().get(Queries.FIELD_PAGEID)));
						}
					}
				}
			}

			// Push to database
			for (TableEntry iteratedEntry : updatedEntries) {
				sqlInterface.update(iteratedEntry);
			}
		}
		catch (SQLException passedException) {
			passedException.printStackTrace();
			// If the SQLInterface cannot be constructed, there's no point in continuing
			return;
		}
	}

	private static QueryBuilder getRevisionsQuery() {
		QueryBuilder revisions = Queries.newWith(Queries.GET_PROPERTIES, Queries.REVISIONS);
		QueryBuilder revisionOptions = Queries.newWith(Queries.OPTION_REVISIONS, Queries.REVISION_FLAGS, Queries.REVISION_IDS, Queries.REVISION_SIZE);
		revisions.setOptions(revisionOptions);
		return revisions;
	}

	private static QueryBuilder getExtractsQuery() {
		QueryBuilder extracts = Queries.newWith(Queries.GET_PROPERTIES, Queries.EXTRACTS);
		QueryBuilder extractOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.OPTION_SECTIONFORMAT_RAW));
		extracts.setOptions(extractOptions);
		return extracts;
	}
}
