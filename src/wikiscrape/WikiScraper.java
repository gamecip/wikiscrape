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

	private static final int MAX_QUERY_SIZE = 50;
	private static final int MAX_PLAINTEXT_EXTRACTS = 20;
	private static final int MAX_WHOLE_ARTICLE_EXTRACTS = 1;

	public static void main(String[] passedArguments) {

		// Construct internal objects
		ScrapeConfig configuration = getConfig("config.json");
		RequestManager scraper = new RequestManager(configuration.getWikiURL());
		QueryBuilder query = Queries.GET_PAGES.clone();

		try {
			SQLInterface sqlInterface = new SQLInterface(configuration.getSQLURL(), configuration.getTableName(), configuration.getUsername(), configuration.getPassword());

			// Build page update map from database
			ResultSet results = sqlInterface.select(EnumEntry.PAGE_ID, EnumEntry.REVISION_ID);
			HashMap<String, String> updateMap = new HashMap<String, String>();
			while (results.next()) {
				String pageID = results.getString(0);
				String revisionID = results.getString(1);
				updateMap.put(pageID, revisionID);
			}
			
			// TODO: Mode for discovery of new pages - use above HashMap
			// TODO: Store "year" in TableEntry using the aforementioned mode
			ArrayList<String> updatesList = new ArrayList<String>();
			String[] listPageUrls = configuration.getListPages();
			// Use links from https://en.wikipedia.org/wiki/Category:Video_games_by_year

			// Compare Revisions
			
			// Get Page Data
			query.setOptions(getCombinedQuery());
			BiConsumer<String, JsonObject> categoriesPopulator = (pageID, object) -> {
				// Get Revisions
				if (object.has(Queries.FIELD_PAGEID) && object.has(Queries.FIELD_REVISIONS)) {
					String discoveredPageID = object.get(Queries.FIELD_PAGEID).getAsString();
					String discoveredRevisionID = object.getAsJsonArray(Queries.FIELD_REVISIONS).get(0).getAsJsonObject().get(Queries.FIELD_REVID).getAsString();
					String storedRevisionID = updateMap.get(discoveredPageID);

					if (!storedRevisionID.equals(discoveredRevisionID)) {
						/* 
						 * Defer revision ID push to database until very end.
						 * 
						 * Reason: if there's an issue and the app crashes and the RevisionID is stored before scrape completes, the database will have
						 * old data keyed to the new revision ID.
						 */
						updateMap.put(pageID, discoveredRevisionID);
						updatesList.add(discoveredPageID);
					}
					else {
						// If the revision ID matches the stored value, assume no further changes; therefore no database updates needed
						return;
					}
				}
				
				// Get Titles
				if (object.has(Queries.FIELD_PAGETITLE)) {
					String discoveredPageTitle = object.get(Queries.FIELD_PAGETITLE).getAsString();
					sqlInterface.updateRaw(discoveredPageTitle, pageID, EnumEntry.TITLE);
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
					sqlInterface.updateRaw(concatenatedCategories, pageID, EnumEntry.CATEGORIES);
				}
				
				// Get Intro text extracts
				if (object.has(Queries.FIELD_EXTRACT)) {
					String extracts = object.get(Queries.FIELD_EXTRACT).getAsString();
					sqlInterface.updateRaw(extracts, pageID, EnumEntry.TEXT_INTRO);
				}
			};
			populateMap(scraper, query, updatesList, categoriesPopulator, MAX_QUERY_SIZE);

			// Redownload text extracts
			query.setOptions(getPagetextQuery());
			BiConsumer<String, JsonObject> extractsPopulator = (pageID, object) -> {
				if (object.has(Queries.FIELD_EXTRACT)) {
					String extracts = object.get(Queries.FIELD_EXTRACT).getAsString();
					sqlInterface.updateRaw(extracts, pageID, EnumEntry.TEXT_FULL);
				}
			};
			populateMap(scraper, query, updatesList, extractsPopulator, MAX_WHOLE_ARTICLE_EXTRACTS);
			
			// Push new revision IDs to database
			for (String iteratedPageID : updatesList) {
				if (updateMap.containsKey(iteratedPageID)) {
					sqlInterface.updateRaw(updateMap.get(iteratedPageID), iteratedPageID, EnumEntry.REVISION_ID);
				}
			}
		}
		
		catch (SQLException passedException) {
			passedException.printStackTrace();
			// If the SQLInterface cannot be constructed, there's no point in continuing
			return;
		}
	}
	
	/* Logic Methods */
	
	
	private static void populateMap(RequestManager passedWikiScraper, QueryBuilder passedQuery, List<String> passedUpdatesList, BiConsumer<String, JsonObject> passedJSONConsumer, int passedQueryBatchSize) {
		BatchIterator<String> iterator = new BatchIterator<String>(passedUpdatesList, passedQueryBatchSize);
		for (List<String> iteratedList : iterator) {
			Argument[] pages = ScrapeUtilities.fromStrings(iteratedList);
			passedQuery.setArguments(pages);
			
			QueryIterator queryIterator = new QueryIterator(passedWikiScraper, passedQuery);
			for (JsonObject returnedJson : queryIterator) {
				JsonArray returnedJsonArray = returnedJson.getAsJsonObject(Queries.FIELD_QUERY).getAsJsonArray(Queries.FIELD_PAGES);
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
	}
	
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
	
	private static QueryBuilder getCombinedQuery() {
		QueryBuilder query = Queries.newWith(Queries.GET_PROPERTIES, Queries.CATEGORIES, Queries.REVISIONS, Queries.EXTRACTS);
		QueryBuilder revisionOptions = Queries.newWith(Queries.OPTION_REVISIONS, Queries.REVISION_FLAGS, Queries.REVISION_IDS, Queries.REVISION_SIZE);
		QueryBuilder introTextOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.ARGUMENT_SECTIONFORMAT_RAW));
		introTextOptions.setOptions(Queries.OPTION_EXTRACT_INTRO);
		query.setOptions(Queries.OPTION_FORMAT_JSON, revisionOptions, introTextOptions);
		return query;
	}

	private static QueryBuilder getPagetextQuery() {
		QueryBuilder extracts = Queries.newWith(Queries.GET_PROPERTIES, Queries.EXTRACTS);
		QueryBuilder extractOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.ARGUMENT_SECTIONFORMAT_RAW));
		extracts.setOptions(Queries.OPTION_FORMAT_JSON, extractOptions);
		return extracts;
	}
}
