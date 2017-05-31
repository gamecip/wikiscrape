package queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import utilities.StringUtilities;

/**
 * Flexible class for defining a wiki api query.
 * 
 * Recursive - supports adding other Query objects as options.
 * 
 * Various levels of caching are used to avoid rebuilding Strings when mutable aspects are changed, i.e. the arguments of the command or the options passed to it.
 * 
 * @author Malcolm Riley
 */
public class Query {

	// Local values
	private String SYNTAX_COMMAND;
	private String SYNTAX_RESULT;
	private String[] ARGUMENTS;
	private List<Query> OPTIONS;

	// Cached Results
	private String CACHE_FINAL = null;
	private String CACHE_COMMAND = null;
	private String CACHE_ARGUMENTS = null;
	private String CACHE_OPTIONS = null;
	private String[] CACHE_RESULT;

	// Change tracking
	private boolean CHANGED_RESULTS = true;
	private boolean CHANGED_ARGUMENTS = true;
	private boolean CHANGED_OPTIONS = true;

	/**
	 * Constructs a new {@link Query} without arguments or options, with the passed command and result syntaxes.
	 * 
	 * @param passedCommandSyntax - The syntax of the 
	 * @param passedResultSyntax
	 */
	public Query(String passedCommandSyntax, String passedResultSyntax) {
		this(passedCommandSyntax, passedResultSyntax, null, (Query[])null);
	}
	
	public Query(String passedCommandSyntax, String passedResultSyntax, String[] passedArguments) {
		this(passedCommandSyntax, passedResultSyntax, passedArguments, (Query[])null);
	}

	public Query(String passedCommandSyntax, String passedResultSyntax, String[] passedArguments, Query... passedOptions) {
		this.SYNTAX_COMMAND = passedCommandSyntax;
		this.SYNTAX_RESULT = passedResultSyntax;
		this.setArguments(passedArguments);
		this.setOptions(passedOptions);
		this.CHANGED_RESULTS = true;
	}
	
	/**
	 * Removes all arguments from this {@link Query}.
	 */
	public void resetArguments() {
		this.setArguments((String[])null);
	}
	
	/**
	 * Removes all options from this {@link Query}.
	 */
	public void resetOptions() {
		this.setOptions((Query[]) null);
	}

	/**
	 * Sets the arguments of this {@link Query} to the passed arguments.
	 * 
	 * @param passedArguments - The new arguments to use for this {@link Query}.
	 */
	public void setArguments(String... passedArguments) {
		this.ARGUMENTS = passedArguments;
		this.CHANGED_ARGUMENTS = true;
	}

	/**
	 * Sets the options of this {@link Query} to the passed {@link Query}s.
	 * 
	 * @param passedOptions - the {@link Query}s to set as the options for this {@link Query} intance.
	 */
	public void setOptions(Query... passedOptions) {
		this.OPTIONS = Collections.unmodifiableList(Arrays.asList(passedOptions));
		this.CHANGED_OPTIONS = true;
	}

	/**
	 * Returns the total {@code String} form build of this {@link Query}, including all current arguments and options.
	 * 
	 * @return This {@link Query}, in finalized {@code String} form.
	 */
	public String build() {
		boolean requiresRebuild = (this.CHANGED_RESULTS || this.CHANGED_ARGUMENTS || this.CHANGED_OPTIONS);
		if (requiresRebuild) {
			this.CACHE_FINAL = this.buildCommand();
			if (this.hasOptions()) {
				this.CACHE_FINAL = concatenateOptions(this.CACHE_FINAL, this.buildOptions());
			}
		}
		return this.CACHE_FINAL;
	}

	/**
	 * Returns the array of all expected result keys - that is, the result fields in the returned json - for this {@link Query} and all its child option {@link Query}s.
	 * 
	 * @return An array of all expected result keys.
	 */
	public String[] getResultKeys() {
		return this.buildResults();
	}

	/**
	 * Returns an immutable, unmodifiable {@link List} containing all currently attached {@link Query} options.
	 * @return
	 */
	public List<Query> getOptions() {
		return this.OPTIONS;
	}

	/**
	 * Returns the simple command syntax of this {@link Query} alone, without any arguments or options.
	 * 
	 * @return The syntax of this command.
	 */
	public String getCommand() {
		return this.SYNTAX_COMMAND;
	}

	/**
	 * Returns the expected result key for this {@link Query} alone, not including any attached options. This is the field in the returned json that corresponds
	 * to the result of this particular {@link Query}.
	 * 
	 * @return The result key for this {@link Query}.
	 */
	public String getResult() {
		return this.SYNTAX_RESULT;
	}

	/**
	 * Returns whether this {@link Query} has any other {@link Query}s attached as options.
	 * 
	 * @return Whether this {@link Query} has options attached.
	 */
	public boolean hasOptions() {
		return (this.OPTIONS != null) && (!this.OPTIONS.isEmpty());
	}

	/**
	 * Returns whether this {@link Query} currently has any arguments.
	 * 
	 * @return Whether this {@link Query} has any arguments.
	 */
	public boolean hasArguments() {
		return (this.ARGUMENTS != null) && (this.ARGUMENTS.length > 0);
	}
	
	@Override
	public Query clone() {
		Query newQuery = new Query(this.SYNTAX_COMMAND, this.SYNTAX_RESULT, this.ARGUMENTS, this.OPTIONS.toArray(new Query[]{}));
		newQuery.setCacheArguments(this.buildArguments());
		newQuery.setCacheCommand(this.buildCommand());
		newQuery.setCacheOptions(this.buildOptions());
		newQuery.setCacheResults(this.buildResults());
		newQuery.setCacheFinalized(this.build());
		return newQuery;
	}
	
	/* Protected methods */
	
	protected void setCacheFinalized(String passedString) {
		this.CACHE_FINAL = passedString;
	}
	
	protected void setCacheArguments(String passedString) {
		this.CACHE_ARGUMENTS = passedString;
		this.CHANGED_ARGUMENTS = false;
	}
	
	protected void setCacheCommand(String passedString) {
		this.CACHE_COMMAND = passedString;
		this.CHANGED_ARGUMENTS = false;
	}
	
	protected void setCacheOptions(String passedString) {
		this.CACHE_OPTIONS = passedString;
		this.CHANGED_OPTIONS = false;
	}
	
	private void setCacheResults(String[] passedStrings) {
		this.CACHE_RESULT = Arrays.copyOf(passedStrings, passedStrings.length);
		this.CHANGED_RESULTS = false;
	}

	/* Local Methods */

	private String buildCommand() {
		if (this.CHANGED_ARGUMENTS) {
			if (this.hasArguments()) {
				this.CACHE_COMMAND = StringUtilities.getQueryTerm(this.SYNTAX_COMMAND, this.buildArguments());
			}
			else {
				this.CACHE_COMMAND = this.SYNTAX_COMMAND;
			}
		}
		this.CHANGED_ARGUMENTS = false;
		return this.CACHE_COMMAND;
	}

	private String buildArguments() {
		if (this.CHANGED_ARGUMENTS) {
			if (this.hasArguments()) {
				if (this.ARGUMENTS.length > 0) {
					this.CACHE_ARGUMENTS = concatenateArguments(this.ARGUMENTS);
				}
				else {
					this.CACHE_ARGUMENTS = this.ARGUMENTS[0];
				}
			}
			else {
				this.CACHE_ARGUMENTS = "";
			}
		}
		this.CHANGED_ARGUMENTS = false;
		return this.CACHE_ARGUMENTS;
	}

	private String[] buildResults() {
		if ((this.CHANGED_RESULTS) || (this.CHANGED_OPTIONS)) {
			this.CACHE_RESULT = new String[1];
			this.CACHE_RESULT[0] = this.getResult();
			for (Query iteratedQuery : this.getOptions()) {
				this.CACHE_RESULT = joinArrays(this.CACHE_RESULT, iteratedQuery.getResultKeys());
			}
		}
		this.CHANGED_RESULTS = false;
		return this.CACHE_RESULT;
	}

	private String buildOptions() {
		if (this.CHANGED_OPTIONS) {
			if (this.hasOptions()) {
				ArrayList<String> discoveredOptions = new ArrayList<String>();
				for (Query iteratedQuery : this.getOptions()) {
					discoveredOptions.add(iteratedQuery.build());
				}
				if (!discoveredOptions.isEmpty()) {
					this.CACHE_OPTIONS = concatenateOptions(discoveredOptions.toArray(new String[] {}));
				}
			}
		}
		this.CHANGED_OPTIONS = false;
		return this.CACHE_OPTIONS;
	}

	/* Logic Methods */

	private static <Type> Type[] joinArrays(Type[] passedFirstArray, Type[] passedSecondArray) {
		Type[] joinedArray = Arrays.copyOf(passedFirstArray, passedFirstArray.length + passedSecondArray.length);
		System.arraycopy(passedSecondArray, 0, joinedArray, passedFirstArray.length, passedSecondArray.length);
		return joinedArray;
	}

	private static String concatenateArguments(String... passedStrings) {
		return StringUtilities.concatenateStrings("|", passedStrings);
	}

	private static String concatenateOptions(String... passedStrings) {
		return StringUtilities.concatenateStrings("&", passedStrings);
	}

}
