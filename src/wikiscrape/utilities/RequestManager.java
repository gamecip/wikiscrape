package wikiscrape.utilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wikiscrape.queries.Queries;
import wikiscrape.queries.QueryBuilder;

public class RequestManager {

	private String BASE_URL;
	private String CONNECTION_TYPE;
	private String API_STRING;
	private String ACTION_TYPE;

	/**
	 * Creates a new {@link RequestManager} with the wiki-standard connection types and api strings.
	 *
	 * @param passedStringURL
	 */
	public RequestManager(String passedStringURL) {
		this("https", passedStringURL, "/w/api.php?", Queries.ACTION_QUERY);
	}

	/**
	 * Creates a new {@link RequestManager} with the passed connection type (i.e. "https"), the passed base URL (i.e. "en.wikipedia.org"),
	 * the passed API access string (i.e. "/w/api.php?"), and the passedActionType (i.e. "action=query").
	 *
	 * @param passedConnectionType - The type of network connection
	 * @param passedStringURL - The base URL to access
	 * @param passedAPIString - The access point to the desired json object
	 * @param passedActionType - The type of action being taken with this request.
	 */
	public RequestManager(String passedConnectionType, String passedStringURL, String passedAPIString, String passedActionType) {
		this.BASE_URL = passedStringURL;
		this.CONNECTION_TYPE = passedConnectionType;
		this.API_STRING = passedAPIString;
		this.ACTION_TYPE = passedActionType;
	}

	/**
	 * Retrieves a {@link JsonObject} using the passed {@link QueryBuilder} instance.
	 *
	 * @param passedQuery - The {@link QueryBuilder} instance to use
	 * @return A {@link JsonObject} retrieved using the passed {@link QueryBuilder} or {@code null} if an exception occurred.
	 */
	public JsonObject request(QueryBuilder passedQuery) {
		String generatedString = String.format("%s://%s%s%s&%s", this.CONNECTION_TYPE, this.BASE_URL, this.API_STRING, this.ACTION_TYPE, passedQuery.build());
		return requestUsing(passedQuery, generatedString);
	}

	/**
	 * Retrieves a {@link JsonObject} using the passed {@link QueryBuilder} instance.
	 *
	 * @param passedQuery - The {@link QueryBuilder} instance to use
	 * @param passedPostfix - An arbitrary String to be postfixed to the 
	 * @return A {@link JsonObject} retrieved using the passed {@link QueryBuilder} or {@code null} if an exception occurred.
	 */
	public JsonObject requestWithPostfix(QueryBuilder passedQuery, String passedPostfix) {
		String generatedString = String.format("%s://%s%s%s&%s&%s", this.CONNECTION_TYPE, this.BASE_URL, this.API_STRING, this.ACTION_TYPE, passedQuery.build(), passedPostfix);
		return requestUsing(passedQuery, generatedString);
	}

	/* Logic Methods */
	private static JsonObject requestUsing(QueryBuilder passedQueryBuilder, String passedURLString) {
		try {
			URL generatedURL = new URL(passedURLString);
			System.out.println(generatedURL.toExternalForm());
			HttpURLConnection request = (HttpURLConnection) generatedURL.openConnection();
			request.connect();
			JsonParser parser = new JsonParser();
			JsonElement rootObject = parser.parse(new InputStreamReader((InputStream) request.getContent())); // Convert the input stream to a json element
			request.disconnect();
			return rootObject.getAsJsonObject();
		}
		catch (Exception passedException) {
			passedException.printStackTrace();
		}
		return null;
	}
}
