package wikiscrape.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import wikiscrape.utilities.ScrapeUtilities;

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
	
	// Functions
	private static final Function<Argument, String> ARGUMENT_MAPPER = (Argument argument) -> {return argument.getArgumentSyntax();};

	// Local values
	private Argument COMMAND;
	private Argument[] ARGUMENTS;
	private List<Query> OPTIONS;

	// Cached Results
	private String CACHE_FINAL = null;
	private String CACHE_COMMAND = null;
	private String CACHE_ARGUMENTS = null;
	private String CACHE_OPTIONS = null;

	// Change tracking
	private boolean CHANGED_RESULTS = true;
	private boolean CHANGED_ARGUMENTS = true;
	private boolean CHANGED_OPTIONS = true;
	
	// Strings
	private static final String EXCEPTION_ILLEGAL_ARGUMENT = "Argument may not be null, neither may it have empty or null syntax or results.";
	
	/**
	 * Constructs a new {@link Query} without child arguments or options, with the passed {@link Argument} representing its command and result syntaxes.
	 * 
	 * @param passedCommand - An {@link Argument} representing the syntax to be used for this {@link Query}'s command and results
	 */
	public Query(Argument passedArgument) {
		this(passedArgument, null, (Query[])null);
	}

	/**
	 * Constructs a new {@link Query} without arguments or options, with the passed {@link Argument} representing its command and result syntaxes.
	 * 
	 * @param passedCommandSyntax - The syntax of the command to be used
	 * @param passedResultSyntax - The expected result field given a successful query
	 * 
	 * @throws IllegalArgumentException - If passedCommandSyntax or passedResultSyntax are empty or null.
	 */
	public Query(String passedCommandSyntax, String passedResultSyntax) {
		this(new Argument(passedCommandSyntax, passedResultSyntax), null, (Query[])null);
	}
	
	/**
	 * Constructs a new {@link Query} with the passed arguments and no options.
	 * 
	 * @param passedCommand - An {@link Argument} representing the syntax to be used for this {@link Query}'s command and results
	 * @param passedArguments - The arguments to be used for this command
	 * 
	 * @throws IllegalArgumentException - If passedCommandSyntax or passedResultSyntax are empty or null.
	 */
	public Query(Argument passedArgument, Argument[] passedArguments) {
		this(passedArgument, passedArguments, (Query[])null);
	}

	/**
	 * Constructs a new {@link Query} with the passed arguments and options.
	 * 
	 * @param passedCommand - An {@link Argument} representing the syntax to be used for this {@link Query}'s command and results
	 * @param passedArguments - The arguments to be used for this command
	 * @param passedOptions - The attached {@link Query}s to be used as options.
	 * 
	 * @throws IllegalArgumentException - If passedCommandSyntax or passedResultSyntax are empty or null.
	 */
	public Query(Argument passedCommand, Argument[] passedArguments, Query... passedOptions) {
		if (validateArgument(passedCommand)) {
			throw new IllegalArgumentException(EXCEPTION_ILLEGAL_ARGUMENT);
		}
		this.COMMAND = passedCommand;
		this.setArguments(passedArguments);
		this.setOptions(passedOptions);
		this.CHANGED_RESULTS = true;
		this.CHANGED_ARGUMENTS = true;
		this.CHANGED_OPTIONS = true;
	}
	
	/**
	 * Removes all arguments from this {@link Query}.
	 */
	public void resetArguments() {
		this.setArguments((Argument[])null);
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
	 * @return {@code this}, for the purpose of method chaining.
	 */
	public Query setArguments(Argument ... passedArguments) {
		this.ARGUMENTS = passedArguments;
		this.CHANGED_ARGUMENTS = true;
		return this;
	}

	/**
	 * Sets the options of this {@link Query} to the passed {@link Query}s.
	 * 
	 * @param passedOptions - the {@link Query}s to set as the options for this {@link Query} intance.
	 * @return {@code this}, for the purpose of method chaining.
	 */
	public Query setOptions(Query... passedOptions) {
		this.OPTIONS = Collections.unmodifiableList(Arrays.asList(passedOptions));
		this.CHANGED_OPTIONS = true;
		return this;
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
		return this.COMMAND.getArgumentSyntax();
	}

	/**
	 * Returns the expected result key for this {@link Query} alone, not including any attached options. This is the field in the returned json that corresponds
	 * to the result of this particular {@link Query}.
	 * 
	 * @return The result key for this {@link Query}.
	 */
	public String getResult() {
		return this.COMMAND.getResultSyntax();
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
		Query newQuery = new Query(this.COMMAND, this.ARGUMENTS, this.OPTIONS.toArray(new Query[]{}));
		newQuery.setCacheArguments(this.buildArguments());
		newQuery.setCacheCommand(this.buildCommand());
		newQuery.setCacheOptions(this.buildOptions());
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

	/* Local Methods */

	private String buildCommand() {
		if (this.CHANGED_ARGUMENTS) {
			if (this.hasArguments()) {
				this.CACHE_COMMAND = ScrapeUtilities.getQueryTerm(this.COMMAND.getArgumentSyntax(), this.buildArguments());
			}
			else {
				this.CACHE_COMMAND = this.COMMAND.getArgumentSyntax();
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
					this.CACHE_ARGUMENTS = this.ARGUMENTS[0].getArgumentSyntax();
				}
			}
		}
		this.CHANGED_ARGUMENTS = false;
		return this.CACHE_ARGUMENTS;
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
	
	private static boolean validateArgument(Argument passedArgument) {
		if (passedArgument == null) {
			return false;
		}
		return validateString(passedArgument.getArgumentSyntax()) && validateString(passedArgument.getResultSyntax());
	}
	
	private static boolean validateString(String passedString) {
		return (passedString != null) && (!passedString.isEmpty());
	}

	private static String concatenateArguments(String... passedStrings) {
		return ScrapeUtilities.concatenateStrings("|", passedStrings);
	}
	
	private static String concatenateArguments(Argument ... passedArguments) {
		return concatenateArguments(buildArgumentArray(passedArguments));
	}

	private static String concatenateOptions(String... passedStrings) {
		return ScrapeUtilities.concatenateStrings("&", passedStrings);
	}
	
	private static String[] buildArgumentArray(Argument ... passedArguments) {
		return (String[]) Arrays.stream(passedArguments).map(ARGUMENT_MAPPER).toArray();
	}

}
