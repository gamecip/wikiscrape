package queries;

public class Argument {
	
	// Local values
	private String SYNTAX_ARGUMENT;
	private String SYNTAX_RESULT;
	
	public Argument(String passedArgumentSyntax, String passedResultSyntax) {
		this.SYNTAX_ARGUMENT = passedArgumentSyntax;
		this.SYNTAX_RESULT = passedResultSyntax;
	}
	
	/**
	 * Returns the {@code String} value syntax for this {@link Argument}.
	 * 
	 * @return The syntax of this {@link Argument}.
	 */
	public String getArgumentSyntax() {
		return this.SYNTAX_ARGUMENT;
	}
	
	/**
	 * Returns the {@code String} value syntax for this {@link Argument}'s expected result key.
	 * 
	 * @return The syntax of this {@link Argument}'s result.
	 */
	public String getResultSyntax() {
		return this.SYNTAX_RESULT;
	}
	
	/**
	 * Returns whether or not this {@link Argument} will have its own field in the result.
	 * 
	 * @return Whether or not including this argument will generate a new JSON field.
	 */
	public boolean hasOwnResult() {
		return (this.SYNTAX_RESULT != null) && (!this.SYNTAX_RESULT.isEmpty());
	}

}