package wikiscrape.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

import wikiscrape.queries.Argument;
import wikiscrape.queries.Queries;
import wikiscrape.queries.QueryBuilder;

/**
 * {@link Iterator} object to automatically handle the continuation of queries.
 *
 * @author Malcolm Riley
 */
public class QueryIterator implements Iterator<JsonObject>, Iterable<JsonObject> {

	private RequestManager MANAGER_REFERENCE;
	private QueryBuilder QUERY_ORIGINAL;
	private JsonObject RETRIEVED_JSON;

	private QueryBuilder OPTION_CONTINUE;
	private QueryBuilder OPTION_CONTINUE_FROM;

	public QueryIterator(RequestManager passedManager, QueryBuilder passedQuery) {
		this.MANAGER_REFERENCE = passedManager;
		this.QUERY_ORIGINAL = passedQuery;

		this.OPTION_CONTINUE = Queries.OPTION_CONTINUE.clone();
		this.OPTION_CONTINUE_FROM = Queries.OPTION_CONTINUE_FROM.clone();
		this.OPTION_CONTINUE.setOptions(this.OPTION_CONTINUE_FROM);
	}

	/* Iterable Compliance Methods */
	@Override
	public Iterator<JsonObject> iterator() {
		return this;
	}

	/* Iterator Compliance Methods */
	@Override
	public boolean hasNext() {
		if (jsonIsNull(this.RETRIEVED_JSON)) {
			return false;
		}
		return this.hasContinues();
	}

	@Override
	public JsonObject next() {
		this.updateJson();
		return this.RETRIEVED_JSON;
	}

	/* Internal Methods */

	private void updateJson() {
		QueryBuilder queryToDo;
		if (this.hasContinues()) {
			String continueString = this.RETRIEVED_JSON.getAsJsonObject(Queries.FIELD_CONTINUE).get(Queries.FIELD_CONTINUE).getAsString();
			String continueFromString = this.RETRIEVED_JSON.getAsJsonObject(Queries.FIELD_CONTINUE_FROM).get(Queries.FIELD_CONTINUE_FROM).getAsString();
			this.OPTION_CONTINUE.setArguments(new Argument(continueString));
			this.OPTION_CONTINUE_FROM.setArguments(new Argument(continueFromString));

			List<QueryBuilder> options = new ArrayList<QueryBuilder>(this.QUERY_ORIGINAL.getOptions());
			options.add(this.OPTION_CONTINUE);
			queryToDo = this.QUERY_ORIGINAL.clone().setOptions(options);
		}
		else {
			queryToDo = this.QUERY_ORIGINAL;
		}
		this.RETRIEVED_JSON = this.MANAGER_REFERENCE.request(queryToDo);
		if (jsonIsNull(this.RETRIEVED_JSON)) {
			System.out.println(String.format("Null JSON returned for query: %s", queryToDo.build()));
			this.RETRIEVED_JSON = new JsonObject(); // set retrieved JSON to be empty JsonObject, avoids null checks later
		}
	}

	private boolean hasContinues() {
		if (!jsonIsNull(this.RETRIEVED_JSON)) {
			boolean continueFields = this.RETRIEVED_JSON.has(Queries.FIELD_CONTINUE);
			boolean batchIncomplete = !this.RETRIEVED_JSON.has(Queries.FIELD_BATCH_COMPLETE);
			return (batchIncomplete || continueFields);
		}
		return false;
	}

	/* Logic Methods */

	private static boolean jsonIsNull(JsonObject passedJsonObject) {
		return (passedJsonObject == null) || (passedJsonObject.isJsonNull());
	}

}
