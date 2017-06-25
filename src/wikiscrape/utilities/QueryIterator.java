package wikiscrape.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import wikiscrape.queries.Queries;
import wikiscrape.queries.QueryBuilder;

/**
 * {@link Iterator} object to automatically handle the continuation of queries.
 *
 * @author Malcolm Riley
 */
public class QueryIterator implements Iterator<JsonObject>, Iterable<JsonObject> {

	private RequestManager MANAGER_REFERENCE;
	private QueryBuilder QUERY;
	private JsonObject RETRIEVED_JSON;
	private boolean REQUEST_PERFORMED;

	public QueryIterator(RequestManager passedManager, QueryBuilder passedQuery) {
		this.MANAGER_REFERENCE = passedManager;
		this.QUERY = passedQuery;
	}

	/* Iterable Compliance Methods */
	@Override
	public Iterator<JsonObject> iterator() {
		return this;
	}

	/* Iterator Compliance Methods */
	@Override
	public boolean hasNext() {
		if (!this.REQUEST_PERFORMED) {
			return true;
		}
		else if (jsonIsNull(this.RETRIEVED_JSON)) {
			return false;
		}
		return this.hasContinues();
	}

	@Override
	public JsonObject next() {
		this.REQUEST_PERFORMED = true;
		if (this.hasContinues()) {
			JsonObject continuations = getContinueElements(this.RETRIEVED_JSON);
			Set<Entry<String, JsonElement>> entrySet = continuations.entrySet();
			ArrayList<String> continuationStrings = new ArrayList<String>();
			entrySet.forEach((entry) -> { continuationStrings.add(String.format("%s=%s", entry.getKey(), entry.getValue())); });
			String postfix = ScrapeUtilities.concatenateArguments(continuationStrings.toArray(new String[]{}));
			this.RETRIEVED_JSON = this.MANAGER_REFERENCE.requestWithPostfix(this.QUERY, postfix);
		}
		else {
			this.RETRIEVED_JSON = this.MANAGER_REFERENCE.request(this.QUERY);
		}
		return this.RETRIEVED_JSON;
	}

	/* Internal Methods */

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
	
	private static JsonObject getContinueElements(JsonObject passedJsonObject) {
		return passedJsonObject.getAsJsonObject(Queries.FIELD_CONTINUE);
	}
}
