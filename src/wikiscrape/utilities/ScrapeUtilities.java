package wikiscrape.utilities;

import java.util.Arrays;
import java.util.function.Function;

import wikiscrape.queries.Argument;

public class ScrapeUtilities {
	
	private static final Function<String, Argument>ARGUMENT_MAPPER = (String string) -> { return new Argument(string); }; 

	private ScrapeUtilities() {
	};

	/**
	 * Method concatenates the passed Strings with the passed linking Strings sandwiched between them.
	 *
	 * @param linkingString - The String to sandwich between the other Strings
	 * @param passedStrings - The Strings to concatenate
	 * @return A suitably formatted String.
	 */
	public static String concatenateStrings(String linkingString, String... passedStrings) {
		StringBuilder builder = new StringBuilder(passedStrings[0]);
		for (int iterator = 1; iterator < passedStrings.length; iterator++) {
			builder.append(linkingString).append(passedStrings[iterator]);
		}
		return builder.toString();
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
	 *
	 * @param passedQueryParameter - The Wiki-defined query parameter to be used
	 * @param passedQueryArguments - The arguments to be passed to the aforementioned query
	 * @return A properly formatted API query term.
	 */
	public static String getQueryTerm(String passedQueryParameter, String... passedQueryArguments) {
		String queryParameters = concatenateArguments(passedQueryArguments);
		return ScrapeUtilities.getQueryTerm(passedQueryParameter, queryParameters);
	}

	/**
	 * Returns a single String-form query term using the passed query parameter String and the single
	 * passed argument String.
	 * <p>
	 * Format of the returned String will be {@code "queryParameter=queryArgument"}.
	 *
	 * @param passedQueryParameter - The Wiki-defined query parameter to be used
	 * @param passedQueryArgument - The argument to be passed to the aforementioned query
	 * @return A suitably formatted API query term
	 */
	public static String getQueryTerm(String passedQueryParameter, String passedQueryArgument) {
		return String.format("%s=%s", passedQueryParameter, passedQueryArgument);
	}
	
	/**
	 * Convenience Method concatenates the passed Strings with a pipe character "|" sandwiched between them.
	 * 
	 * @param passedArgumentStrings - The Strings to concatenate
	 * @return A suitably formatted String.
	 */
	public static String concatenateArguments(String ... passedArgumentStrings) {
		return concatenateStrings("|", passedArgumentStrings);
	}
	
	/**
	 * Convenience Method concatenates the passed Strings with an ampersand character "&" sandwiched between them.
	 * 
	 * @param passedArgumentStrings - The Strings to concatenate
	 * @return A suitably formatted String.
	 */
	public static String concatenateCommands(String ... passedCommandStrings) {
		return concatenateStrings("&", passedCommandStrings);
	}
	
	/**
	 * Creates an array of {@link Arguments} from an array of {@code Strings} representing the array of command syntaxes to use.
	 * 
	 * @param passedCommands - The array of command syntaxes to use
	 * @return An array of suitably instantiated {@link Argument} instances.
	 */
	public static Argument[] fromStrings(String ... passedStrings) {
		Argument[] argumentArray = new Argument[passedStrings.length];
		for (int iterator = 0; iterator < passedStrings.length; iterator++) {
			argumentArray[iterator] = new Argument(passedStrings[iterator]);
		}
		return argumentArray;
	}
	
	/**
	 * Creates an array of {@link Arguments} from two arrays of {@code Strings}, the first representing the array of command syntaxes,
	 * and the second representing the array of result syntaxes.
	 * <p>
	 * The passedCommands array must equal or exceed the passedResults array in size.
	 * @param passedCommands - The array of command syntaxes to use
	 * @param passedResults - The array of result syntaxes to use
	 * @return An array of suitably instantiated {@link Argument} instances.
	 * @throws IllegalArgumentException - If the {@code passedCommands} array is less in length than the {@code passedResults} array.
	 */
	public static Argument[] fromStrings(String[] passedCommands, String[] passedResults) {
		if (passedCommands.length < passedResults.length) {
			throw new IllegalArgumentException("Commands array must exceed or equal results array in size.");
		}
		Argument[] argumentArray = new Argument[passedCommands.length];
		for (int iterator = 0; iterator < passedCommands.length; iterator++) {
			argumentArray[iterator] = new Argument(passedCommands[iterator], passedResults[iterator]);
		}
		return argumentArray;
	}
	
	/**
	 * Joins the two passed arrays.
	 * 
	 * @param passedFirstArray - The first array
	 * @param passedSecondArray - The array to join with the second array.
	 * @return A new array consisting of the joined arrays.
	 */
	public static <Type> Type[] joinArrays(Type[] passedFirstArray, Type[] passedSecondArray) {
		Type[] joinedArray = Arrays.copyOf(passedFirstArray, passedFirstArray.length + passedSecondArray.length);
		System.arraycopy(passedSecondArray, 0, joinedArray, passedFirstArray.length, passedSecondArray.length);
		return joinedArray;
	}
 
}
