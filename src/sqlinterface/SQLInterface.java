package sqlinterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Class that provides support for several common SQL operations, using {@link TableEntry} and {@link EnumEntry} for ease of use.
 * 
 * @author Malcolm Riley
 */
public class SQLInterface {

	private Connection CONNECTION;
	private String TABLE_NAME;

	private static final String SYNTAX_INSERT = "INSERT INTO %s VALUES %s";
	private static final String SYNTAX_UPDATE = "UPDATE %s SET %s";
	private static final String SYNTAX_SELECT = "SELECT %s FROM %s";

	private static final Function<EnumEntry, String> MAPPER_SELECT = (entry) -> { return "?"; };
	private static final Function<TableEntry, String> MAPPER_INSERT = (entry) -> { return String.format("(%s)", fromTableEntry(entry)); };
	private static final Function<TableEntry, String> MAPPER_UPDATE = (entry) -> { return String.format("%s WHERE %s", fromTableEntry(entry), String.format("%s = ?", EnumEntry.PAGE_ID.getEntryName())); };

	/**
	 * Constructs a new {@link SQLInterface} object using the passed parameters.
	 * 
	 * @param passedDatabaseURL - The URL of the database to access
	 * @param passedTableName - The name of the table that this {@link SQLInterface} will alter
	 * @param passedUsername - The username to use for accessing the SQL database
	 * @param passedPassword - The password that will be used for accessing the SQL database
	 * @throws SQLException If an exception occurs while setting up the connection to the SQL database
	 */
	public SQLInterface(String passedDatabaseURL, String passedTableName, String passedUsername, String passedPassword) throws SQLException {
		this.CONNECTION = DriverManager.getConnection(passedDatabaseURL, passedUsername, passedPassword);
		this.TABLE_NAME = passedTableName;
	}

	/**
	 * Performs a {@code SELECT} operation on this instance's table's columns specified by the passed {@link EnumEntry} instances.
	 * 
	 * @param passedEntries - The columns to select from this instance's table
	 * @return The {@link ResultSet} from the command's execution.
	 */
	public ResultSet select(EnumEntry... passedEntries) {
		String command = String.format(SYNTAX_SELECT, buildForEach(MAPPER_SELECT, passedEntries), this.TABLE_NAME) + ";";
		try {
			PreparedStatement statement = this.obtain(command);
			for (int iterator = 0; iterator < passedEntries.length; iterator++) {
				statement.setString(iterator, passedEntries[iterator].getEntryName());
			}
			return this.executeCommand(statement);
		}
		catch (SQLException passedException) {
			System.out.println(String.format("Exception when attempting to send command \"%s\"", command));
			passedException.printStackTrace();
		}
		return null;
	}

	/**
	 * Inserts the passed {@link TableEntry} instances into the {@link SQLInterface} instance's table.
	 * <p>
	 * Transmits a command equivalent to {@code INSERT INTO [table] VALUES (TableEntry ...)}
	 * @param passedTableEntries - The data to insert into the table
	 * @return The {@link ResultSet} from the command's execution.
	 */
	public ResultSet insert(TableEntry... passedTableEntries) {
		String command = String.format(SYNTAX_INSERT, this.TABLE_NAME, buildForEach(MAPPER_INSERT, passedTableEntries)) + ";";
		try {
			PreparedStatement statement = this.obtain(command);
			for (int iterator = 0; iterator < passedTableEntries.length; iterator++) {
				for (EnumEntry iteratedEnum : EnumEntry.values()) {
					statement.setString(iterator, passedTableEntries[iterator].getEntry(iteratedEnum));
				}
			}
			return this.executeCommand(statement);
		}
		catch (SQLException passedException) {
			System.out.println(String.format("Exception when attempting to send command \"%s\"", command));
			passedException.printStackTrace();
		}
		return null;
	}

	/**
	 * Updates this {@link SQLInterface} instance's table using the passed {@link TableEntry}.
	 * <p>
	 * Transmits a command equivalent to {@code UPDATE [table] SET [TableEntry] WHERE [pageid] = [TableEntry pageID]}
	 * @param passedTableEntry The {@link TableEntry} instance to use for updating
	 * @return The {@link ResultSet} from the command's execution.
	 */
	public ResultSet update(TableEntry passedTableEntry) {
		String command = String.format(SYNTAX_UPDATE, this.TABLE_NAME, buildForEach(MAPPER_UPDATE, new TableEntry[]{passedTableEntry})) + ";";
		try {
			PreparedStatement statement = this.obtain(command);
			for (int iterator = 0; iterator < EnumEntry.values().length; iterator++) {
				statement.setString(iterator, passedTableEntry.getEntry(iterator));
			}
			return this.executeCommand(statement);
		}
		catch (SQLException passedException) {
			System.out.println(String.format("Exception when attempting to send command \"%s\"", command));
			passedException.printStackTrace();
		}
		return null;
	}

	/* Internal Methods */

	private static String fromTableEntry(TableEntry passedEntry) {
		int length = passedEntry.getEntries().length;
		if (length > 1) {
			StringBuilder builder = new StringBuilder("?");
			for (int iterator = 1; iterator < length; iterator++) {
				builder.append(", ?");
			}
			return builder.toString();
		}
		else {
			return "?";
		}
	}

	private static <T> String buildForEach(Function<T, String> passedMapper, T[] passedObjects) {
		if (passedObjects.length > 1) {
			StringBuilder builder = new StringBuilder();
			for (int iterator = 0; iterator < passedObjects.length; iterator++) {
				builder.append(passedMapper.apply(passedObjects[iterator]));
				if ((iterator + 1) < passedObjects.length) {
					builder.append(", ");
				}
			}
			return builder.toString();
		}
		return passedMapper.apply(passedObjects[0]);
	}

	private PreparedStatement obtain(String passedCommand) {
		PreparedStatement statement = null;
		try {
			statement = this.CONNECTION.prepareStatement(passedCommand);
			statement.closeOnCompletion();
		}
		catch (SQLException passedException) {
			passedException.printStackTrace();
		}
		return statement;
	}

	private ResultSet executeCommand(PreparedStatement passedStatement) {
		try {
			return passedStatement.executeQuery();
		}
		catch (SQLException passedException) {
			passedException.printStackTrace();
			return null;
		}
	}
}
