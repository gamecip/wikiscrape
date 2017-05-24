package queries;

import utilities.StringUtilities;

public class QueryBuilder {
	
	public QueryBuilder() {
		
	}
	
	/**
	 * Returns a single String-form query term using the passed query parameter String and the 
	 * passed arguments.
	 * <p>
	 * Format of the returned String will be {@code"queryParameter=argument1|argument2|...|argumentN"}.
	 * <p>
	 * This method does not check whether the passed query parameter String is a valid query,
	 * neither does it check whether the passed query arguments exceed the configured max
	 * query threshold in quantity. Such checking should be performed elsewhere.
	 * <p>
	 * @param passedQueryParameter - The Wiki-defined query parameter to be used
	 * @param passedQueryArguments - The arguments to be passed to the aforementioned query
	 * @return A properly formatted API query term.
	 */
	public String getQueryTerm(String passedQueryParameter, String... passedQueryArguments) {
		String queryParameters = StringUtilities.concatenateStrings("|", passedQueryArguments);
		return String.format("%s=%s", passedQueryParameter, queryParameters);
	}
}