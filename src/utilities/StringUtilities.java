package utilities;

public class StringUtilities {
	
	private StringUtilities(){};
	
	/**
	 * Method concatenates the passed Strings with the passed linking Strings sandwiched between them.
	 * 
	 * @param linkingString - The String to sandwich between the other Strings
	 * @param passedStrings - The Strings to concatenate
	 * @return A suitably formatted String.
	 */
	public static String concatenateStrings(String linkingString, String ... passedStrings) {
		StringBuilder builder = new StringBuilder(passedStrings[0]);
		for (int iterator = 1; iterator < passedStrings.length; iterator++) {
			builder.append(linkingString).append(passedStrings[iterator]);
		}
		return builder.toString();
	}

}
