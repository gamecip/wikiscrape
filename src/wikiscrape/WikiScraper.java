package wikiscrape;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sqlinterface.EnumEntry;
import sqlinterface.SQLInterface;
import wikiscrape.queries.Argument;
import wikiscrape.queries.Queries;
import wikiscrape.queries.QueryBuilder;
import wikiscrape.utilities.BatchIterator;
import wikiscrape.utilities.JsonObjectParser;
import wikiscrape.utilities.QueryIterator;
import wikiscrape.utilities.RequestManager;
import wikiscrape.utilities.ScrapeConfig;
import wikiscrape.utilities.ScrapeUtilities;

public class WikiScraper {

	// Unused!
	// private static final int MAX_QUERY_SIZE = 50;
	private static final int MAX_PLAINTEXT_EXTRACTS = 20;
	private static final int MAX_WHOLE_ARTICLE_EXTRACTS = 1;
	private static final int TIMEOUT_SECONDS = 5;

	public static void main(String[] passedArguments) {

		// Construct internal objects
		ScrapeConfig configuration = getConfig("config.json");
		RequestManager scraper = new RequestManager(configuration.getWikiURL());
		QueryBuilder query = Queries.GET_PAGES.clone();

		try {
			SQLInterface sqlInterface = new SQLInterface(configuration.getSQLURL(), configuration.getTableName(), configuration.getUsername(), configuration.getPassword());
			HashMap<String,String> databaseMap = new HashMap<String, String>();
			ArrayList<String> updatesList = new ArrayList<String>();

			// Build page update map from database
			populateDatabaseMap(databaseMap, sqlInterface);			
			
			// Get pages from Categories listings
			buildPagesList(scraper, configuration.getCategoryPages(), updatesList);

			// Get Revisions, Titles, Categories
			updatePageData(query, scraper, sqlInterface, updatesList, databaseMap);

			// Redownload text extracts
			updateExtracts(query, scraper, sqlInterface, updatesList);
			
			// Push new revision IDs to database
			for (String iteratedPageID : updatesList) {
				if (databaseMap.containsKey(iteratedPageID)) {
					sqlInterface.updateRaw(databaseMap.get(iteratedPageID), iteratedPageID, EnumEntry.REVISION_ID);
				}
			}
		}
		
		catch (SQLException passedException) {
			passedException.printStackTrace();
			return; // If the SQLInterface fails, there's no point in continuing
		}
	}
	
	/* Logic Methods */
	
	private static void populateDatabaseMap(HashMap<String,String> passedDatabaseMap, SQLInterface passedSQLInterface) throws SQLException {
		ResultSet results = passedSQLInterface.select(EnumEntry.PAGE_ID, EnumEntry.REVISION_ID);
		while (results.next()) {
			String pageID = results.getString(0);
			String revisionID = results.getString(1);
			passedDatabaseMap.put(pageID, revisionID);
		}
	}
	
	private static void buildPagesList(RequestManager passedRequestManager, String[] passedCategoryPages, List<String> passedUpdatesList) {
		// Use links from https://en.wikipedia.org/wiki/Category:Video_games_by_year
		QueryBuilder query = Queries.LIST_CATEGORYMEMBERS;
		QueryBuilder categoryTitleOption = new QueryBuilder(Queries.ARGUMENT_CATEGORYMEMBERS_TITLE);
		for (String iteratedString : passedCategoryPages) {
			categoryTitleOption.setArguments(new Argument(iteratedString));
			final BiConsumer<String, JsonObject> updatePopulator = (pageID, object) -> {
				
			};
			iterateOverQuery(passedRequestManager, query, updatePopulator, Queries.FIELD_CATEGORYMEMBERS);
		}
	}
	
	private static void updatePageData(QueryBuilder passedQuery, RequestManager passedRequestManager, SQLInterface passedSQLInterface, List<String> passedUpdatesList, HashMap<String,String> passedDatabaseMap) {
		passedQuery.setOptions(getCombinedQuery());
		final BiConsumer<String, JsonObject> categoriesPopulator = (pageID, object) -> {
			// Get Revisions
			if (object.has(Queries.FIELD_PAGEID) && object.has(Queries.FIELD_REVISIONS)) {
				String discoveredPageID = object.get(Queries.FIELD_PAGEID).getAsString();
				String discoveredRevisionID = object.getAsJsonArray(Queries.FIELD_REVISIONS).get(0).getAsJsonObject().get(Queries.FIELD_REVID).getAsString();
				String storedRevisionID = passedDatabaseMap.get(discoveredPageID);

				if (!storedRevisionID.equals(discoveredRevisionID)) {
					/* 
					 * Defer revision ID push to database until very end.
					 * 
					 * Reason: if there's an issue and the app crashes and the RevisionID is stored before scrape completes, the database will have
					 * old data keyed to the new revision ID.
					 */
					passedDatabaseMap.put(pageID, discoveredRevisionID);
					passedUpdatesList.add(discoveredPageID);
				}
				else {
					// If the revision ID matches the stored value, assume no further changes; therefore no database updates needed
					return;
				}
			}
			
			// Get Titles
			if (object.has(Queries.FIELD_PAGETITLE)) {
				String discoveredPageTitle = object.get(Queries.FIELD_PAGETITLE).getAsString();
				passedSQLInterface.updateRaw(discoveredPageTitle, pageID, EnumEntry.TITLE);
			}
			
			// Get Categories
			if (object.has(Queries.FIELD_CATEGORIES)) {
				JsonArray categoriesArray = object.getAsJsonArray(Queries.FIELD_CATEGORIES);
				String[] categories = new String[categoriesArray.size()];
				for (int iterator = 0; iterator < categoriesArray.size(); iterator++) {
					String categoriesString = categoriesArray.getAsJsonObject().get(Queries.FIELD_PAGETITLE).getAsString();
					categoriesString = categoriesString.substring("Category:".length()); // prune "Category:" from each returned category "title"
					categories[iterator] = categoriesString;
				}
				String concatenatedCategories = ScrapeUtilities.concatenateArguments(categories); // Concatenate using "|" sandwiched between
				passedSQLInterface.updateRaw(concatenatedCategories, pageID, EnumEntry.CATEGORIES);
			}
			
			// Get Intro text extracts
			if (object.has(Queries.FIELD_EXTRACT)) {
				String extracts = object.get(Queries.FIELD_EXTRACT).getAsString();
				passedSQLInterface.updateRaw(extracts, pageID, EnumEntry.TEXT_INTRO);
			}
		};
		updatePagesUsing(passedRequestManager, passedQuery, passedUpdatesList, categoriesPopulator, MAX_PLAINTEXT_EXTRACTS); // Since this query gets introtext extracts, use max for plaintext extracts
	}
	
	private static void updateExtracts(QueryBuilder passedQuery, RequestManager passedRequestManager, SQLInterface passedSQLInterface, List<String> passedUpdatesList) {
		passedQuery.setOptions(getPagetextQuery());
		final BiConsumer<String, JsonObject> extractsPopulator = (pageID, object) -> {
			if (object.has(Queries.FIELD_EXTRACT)) {
				String extracts = object.get(Queries.FIELD_EXTRACT).getAsString();
				passedSQLInterface.updateRaw(extracts, pageID, EnumEntry.TEXT_FULL);
			}
		};
		updatePagesUsing(passedRequestManager, passedQuery, passedUpdatesList, extractsPopulator, MAX_WHOLE_ARTICLE_EXTRACTS);
	}
	
	/**
	 * Method to automatically iterate over PageID-keyed results provided by the passed {@link QueryBuilder} instance.
	 * <p>
	 * It is up to the passed {@link BiConsumer<String, JsonObject>} to perform any operations necessary
	 * per element in the array; one call to {@link BiConsumer#accept(Object, Object)} is performed per element in the greater JSON object. Whatever the program needs to do with the data in the
	 * JSON element in the array needs to happen in that {@link BiConsumer<String, JsonObject>}.
	 * 
	 * @param passedRequestManager - The {@link RequestManager} instance to use for the operation
	 * @param passedQuery - The {@link QueryBuilder} to use for querying
	 * @param passedUpdatesList - A {@link List<String>} containing all the PageIDs that require updates
	 * @param passedJSONConsumer - A {@link BiConsumer<String, JsonObject>} that will operate on each returned element in the greater returned JSON object
	 * @param passedQueryBatchSize - The number of PageIDs to poll for each query
	 */
	private static void updatePagesUsing(RequestManager passedRequestManager, QueryBuilder passedQuery, List<String> passedUpdatesList, BiConsumer<String, JsonObject> passedJSONConsumer, int passedQueryBatchSize) {
		BatchIterator<String> iterator = new BatchIterator<String>(passedUpdatesList, passedQueryBatchSize);
		for (List<String> iteratedList : iterator) {
			Argument[] pages = ScrapeUtilities.fromStrings(iteratedList);
			passedQuery.setArguments(pages);
			iterateOverQuery(passedRequestManager, passedQuery, passedJSONConsumer, Queries.FIELD_PAGES);
		}
	}
	
	/**
	 * Method to automatically iterated over a JSON array returned within another JSON object. 
	 * <p>
	 * It is up to the passed {@link BiConsumer<String, JsonObject>} to perform any operations necessary
	 * per element in the array; one call to {@link BiConsumer#accept(Object, Object)} is performed per element in the greater JSON object. Whatever the program needs to do with the data in the
	 * JSON element in the array needs to happen in that {@link BiConsumer<String, JsonObject>}.
	 * 
	 * @param passedRequestManager - The {@link RequestManager} instance to use for the operation
	 * @param passedQuery - The {@link QueryBuilder} to use for querying
	 * @param passedJSONConsumer - A {@link BiConsumer<String, JsonObject>} that will operate on each returned element in the greater returned JSON object
	 * @param passedJSONArrayName - The name key of the JSON Array to operate on within the greater JSON object returned by the query specified by {@code passedQuery}.
	 */
	private static void iterateOverQuery(RequestManager passedRequestManager, QueryBuilder passedQuery, BiConsumer<String, JsonObject> passedJSONConsumer, String passedJSONArrayName) {
		QueryIterator queryIterator = new QueryIterator(passedRequestManager, passedQuery, TIMEOUT_SECONDS);
		for (JsonObject returnedJson : queryIterator) {
			JsonArray returnedJsonArray = returnedJson.getAsJsonObject(Queries.FIELD_QUERY).getAsJsonArray(passedJSONArrayName);
			for (JsonElement iteratedElement : returnedJsonArray) {
				JsonObject object = iteratedElement.getAsJsonObject();
				if (!object.has(Queries.FIELD_MISSING)) {
					String discoveredPageID = object.get(Queries.FIELD_PAGEID).getAsString();
					passedJSONConsumer.accept(discoveredPageID, object);
				}
				else {
					// TODO: Additional handling? What to do if page is missing?
					System.out.println(String.format("Page with id [%d] is missing!", iteratedElement.getAsJsonObject().get(Queries.FIELD_PAGEID)));
				}
			}
		}
	}
	
	/**
	 * Returns the {@link ScrapeConfig} object instance constructed from the JSON configuration file at the passed file path.
	 * 
	 * @param passedFilePath - The file path at which to search for the configuration file
	 * @return A suitably instantiated {@link ScrapeConfig} instance, or null if the file failed to read.
	 */
	private static ScrapeConfig getConfig(String passedFilePath) {
		try {
			JsonObjectParser<ScrapeConfig> configReader = new JsonObjectParser<ScrapeConfig>(passedFilePath, ScrapeConfig.class);
			return configReader.fromJson();
		}
		catch (FileNotFoundException passedException) {
			passedException.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a {@link QueryBuilder} instance that combines requests for categories, revisions, and the page's intro text extract.
	 * 
	 * @return A combined query of categories, revisions, and introtext.
	 */
	private static QueryBuilder getCombinedQuery() {
		QueryBuilder query = Queries.newWith(Queries.GET_PROPERTIES, Queries.CATEGORIES, Queries.REVISIONS, Queries.EXTRACTS);
		QueryBuilder revisionOptions = Queries.newWith(Queries.OPTION_REVISIONS, Queries.REVISION_FLAGS, Queries.REVISION_IDS, Queries.REVISION_SIZE);
		QueryBuilder introTextOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.ARGUMENT_SECTIONFORMAT_RAW));
		introTextOptions.setOptions(Queries.OPTION_EXTRACT_INTRO);
		query.setOptions(Queries.OPTION_FORMAT_JSON, revisionOptions, introTextOptions);
		return query;
	}

	/**
	 * Returns a {@link QueryBuilder} instance that requests a page's full text.
	 * 
	 * @return A full-text query.
	 */
	private static QueryBuilder getPagetextQuery() {
		QueryBuilder extracts = Queries.newWith(Queries.GET_PROPERTIES, Queries.EXTRACTS);
		QueryBuilder extractOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.ARGUMENT_SECTIONFORMAT_RAW));
		extracts.setOptions(Queries.OPTION_FORMAT_JSON, extractOptions);
		return extracts;
	}
}
