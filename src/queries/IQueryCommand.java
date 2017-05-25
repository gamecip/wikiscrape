package queries;

public interface IQueryCommand {
	/**
	 * This method should return the String command syntax for this object.
	 * 
	 * @return
	 */
	public String getCommandSyntax();

	/**
	 * This method should return the String result syntax for this object; that is,
	 * the expected key of the JSON field returned from the API after parsing this command.
	 * 
	 * @return
	 */
	public String getResultSyntax();
}