package queries;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import utilities.StringUtilities;

public class QueryCommand implements IQueryCommand {

	private final String COMMAND;
	private final String RESULT;
	private final List<IQueryCommand> OPTIONS;

	public QueryCommand(String passedCommandSyntax, String passedResultSyntax) {
		this(passedCommandSyntax, passedResultSyntax, (IQueryCommand[]) null);
	}

	public QueryCommand(String passedCommandSyntax, String passedResultSyntax, IQueryCommand... passedQueryOptions) {
		this.COMMAND = passedCommandSyntax;
		this.RESULT = passedResultSyntax;
		this.OPTIONS = Collections.unmodifiableList(Arrays.asList(passedQueryOptions));
	}

	@Override
	public String getCommandSyntax() {
		return this.COMMAND;
	}

	@Override
	public String getResultSyntax() {
		return this.RESULT;
	}

	/**
	 * Returns the command syntax of this {@link QueryCommand} using the passed arguments. If the passed arguments are not valid
	 * for this {@link QueryCommand}, they will not be used.
	 *
	 * @param passedOptions - The options to pass to the query command
	 * @return A suitably formatted Query command.
	 */
	public String getCommandSyntax(IQueryCommand... passedOptions) {
		if (this.validateOptions(passedOptions)) {
			if (passedOptions.length > 1) {
				Function<IQueryCommand, String> queryCommandToString = (IQueryCommand command) -> {
					return command.getCommandSyntax();
				};
				String[] queryOptions = (String[]) Stream.of(passedOptions).map(queryCommandToString).toArray();
				return StringUtilities.getQueryTerm(this.getCommandSyntax(), queryOptions);
			}
			else {
				return StringUtilities.getQueryTerm(this.getCommandSyntax(), passedOptions[0].getCommandSyntax());
			}
		}
		else {
			return this.getCommandSyntax();
		}
	}

	/* Logic Methods */

	private boolean validateOptions(IQueryCommand... passedOptions) {
		if (this.OPTIONS != null) {
			return this.OPTIONS.containsAll(Arrays.asList(passedOptions));
		}
		return false;
	}

}
