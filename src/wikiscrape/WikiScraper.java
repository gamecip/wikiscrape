package wikiscrape;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

			/* 
			 * TODO: Transition from TableEntry implementation; no need to store all that data in memory (may not be possible)
			 * 
			 * TableEntry was convenient for TSVParser but not scalable. Perhaps only push actual values to database instead of persisting comparatively weighty TableEntry objects
			 */
			HashMap<String, TableEntry> databaseUpdates = new HashMap<String, TableEntry>();
			// TODO: Mode for discovery of new pages - use above HashMap
			// TODO: Store "year" in TableEntry using the aforementioned mode
			String[] listPageUrls = configuration.getListPages();
			// Use links from https://en.wikipedia.org/wiki/Category:Video_games_by_year

			// Compare Revisions
			Function<JsonObject, String> keyMapper = (object) -> { return object.get(Queries.FIELD_PAGEID).getAsString(); };
			Supplier<TableEntry> tableEntrySupplier = () -> { return new TableEntry(new String[EnumEntry.values().length]); };
			
			// Get Page Data
			query.setOptions(getCombinedQuery());
			BiConsumer<TableEntry, JsonObject> categoriesPopulator = (entry, object) -> {
				// Get Revisions
				if (object.has(Queries.FIELD_PAGEID) && object.has(Queries.FIELD_REVISIONS)) {
					String discoveredPageID = object.get(Queries.FIELD_PAGEID).getAsString();
					String discoveredRevisionID = object.getAsJsonArray(Queries.FIELD_REVISIONS).get(0).getAsJsonObject().get(Queries.FIELD_REVID).getAsString();
					String storedRevisionID = updateMap.get(discoveredPageID);

					if (!storedRevisionID.equals(discoveredRevisionID)) {
						entry.setEntry(EnumEntry.PAGE_ID, discoveredPageID);
						entry.setEntry(EnumEntry.REVISION_ID, discoveredRevisionID);
						databaseUpdates.put(discoveredPageID, entry);

						sqlInterface.update(entry, EnumEntry.REVISION_ID);
					}
					else {
						// If the revision ID matches the stored value, assume no further changes; therefore no database upgrades needed
						return;
					}
				}
				
				// Get Titles
				if (object.has(Queries.FIELD_PAGETITLE)) {
					String discoveredPageTitle = object.get(Queries.FIELD_PAGETITLE).getAsString();
					entry.setEntry(EnumEntry.TITLE, discoveredPageTitle);
					sqlInterface.update(entry, EnumEntry.TITLE);
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
					entry.setEntry(EnumEntry.CATEGORIES, ScrapeUtilities.concatenateArguments(categories)); // Concatenate using "|" sandwiched between
					sqlInterface.update(entry, EnumEntry.CATEGORIES);
				}
				
				// Get Intro text extracts
				if (object.has(Queries.FIELD_EXTRACT)) {
					String extracts = object.get(Queries.FIELD_EXTRACT).getAsString();
					entry.setEntry(EnumEntry.TEXT_INTRO, extracts);
					sqlInterface.update(entry, EnumEntry.TEXT_INTRO);
				}
			};
			populateMap(scraper, query, databaseUpdates, keyMapper, categoriesPopulator, tableEntrySupplier, MAX_QUERY_SIZE);

			// Redownload text extracts
			query.setOptions(getPagetextQuery());
			BiConsumer<TableEntry, JsonObject> extractsPopulator = (entry, object) -> {
				if (object.has(Queries.FIELD_EXTRACT)) {
					String extracts = object.get(Queries.FIELD_EXTRACT).getAsString();
					entry.setEntry(EnumEntry.TEXT_FULL, extracts);
					sqlInterface.update(entry, EnumEntry.TEXT_FULL);
				}
			};
			populateMap(scraper, query, databaseUpdates, keyMapper, extractsPopulator, tableEntrySupplier, MAX_WHOLE_ARTICLE_EXTRACTS);
		}
		
		catch (SQLException passedException) {
			passedException.printStackTrace();
			// If the SQLInterface cannot be constructed, there's no point in continuing
			return;
		}
	}
	
	/* Logic Methods */
	
	/**
	 * Populates the {@code Object} instances in the passed {@link HashMap} using the passed objects.
	 * <p>
	 * Of particular importance is the {@link BiConsumer<T, JsonElement>} object; this should populate the passed {@link TableEntry} from the passed {@link JsonObject}.
	 * 
	 * @param passedWikiScraper - The {@link RequestManager} instance to use
	 * @param passedQuery - The {@link QueryBuilder} that will be used for the query
	 * @param passedMap - The {@link HashMap} of {@code Object<T>} instances, that will be populated and/or edited
	 * @param passedKeyMapper - A {@link Function} that generates a single {@code Object<U>} from a single {@link JsonObject}
	 * @param passedPopulator - A {@link BiConsumer<T, JsonObject>} object that populates a single {@code Object<T>} from a single {@link JsonObject}
	 * @param passedTypeSupplier - A {@link Supplier<T>} instance to use provided the {@code Object<T>} does not already exist in {@code passedTableMap}
	 * @param passedQueryBatchSize - The batch size to use while making queries.
	 */
	private static <T, U> void populateMap(RequestManager passedWikiScraper, QueryBuilder passedQuery, HashMap<U, T> passedMap, Function<JsonObject, U> passedKeyMapper, BiConsumer<T, JsonObject> passedPopulator, Supplier<T> passedTypeSupplier, int passedQueryBatchSize) {
		BatchIterator<String> iterator = new BatchIterator<String>(passedMap.keySet().toArray(new String[]{}), passedQueryBatchSize);
		for (List<String> iteratedList : iterator) {
			Argument[] pages = ScrapeUtilities.fromStrings(iteratedList);
			passedQuery.setArguments(pages);
			
			QueryIterator queryIterator = new QueryIterator(passedWikiScraper, passedQuery);
			for (JsonObject returnedJson : queryIterator) {
				JsonArray returnedJsonArray = returnedJson.getAsJsonObject(Queries.FIELD_QUERY).getAsJsonArray(Queries.FIELD_PAGES);
				for (JsonElement iteratedElement : returnedJsonArray) {
					if (!iteratedElement.getAsJsonObject().has(Queries.FIELD_MISSING)) {
						U key = passedKeyMapper.apply(iteratedElement.getAsJsonObject());
						T entry = passedMap.get(key);
						if (passedMap.containsKey(key)) {
							entry = passedMap.get(key);
						}
						else {
							entry = passedTypeSupplier.get();
						}
						passedPopulator.accept(entry, iteratedElement.getAsJsonObject());
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
		QueryBuilder introTextOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.OPTION_SECTIONFORMAT_RAW));
		introTextOptions.setOptions(Queries.OPTION_EXTRACT_INTRO);
		query.setOptions(revisionOptions, introTextOptions);
		return query;
	}

	private static QueryBuilder getPagetextQuery() {
		QueryBuilder extracts = Queries.newWith(Queries.GET_PROPERTIES, Queries.EXTRACTS);
		QueryBuilder extractOptions = Queries.newWith(Queries.OPTION_EXTRACT_PLAINTEXT, Queries.newWith(Queries.OPTION_SECTIONFORMAT, Queries.OPTION_SECTIONFORMAT_RAW));
		extracts.setOptions(extractOptions);
		return extracts;
	}
}
