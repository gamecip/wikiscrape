package tsvparser;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sqlinterface.SQLInterface;
import sqlinterface.TableEntry;
import tsvparser.utilities.TSVConfig;
import tsvparser.utilities.TSVUtilities;
import wikiscrape.utilities.JsonObjectParser;

public class TSVParser {

	private static String TSV_EXTENSION = "tsv";

	public static void main(String[] passedArguments) {
		
		String configPath = "";
		TSVConfig configuration = getConfig(configPath);

		List<Path> discoveredFiles = TSVUtilities.getFilesInDirectory(configuration.getDirectory(), TSV_EXTENSION);
		List<TableEntry> generatedEntries = new ArrayList<TableEntry>();

		try {
			SQLInterface sqlInterface = new SQLInterface(configuration.getSQLURL(), configuration.getTableName(), configuration.getUsername(), configuration.getPassword());

			// Generate entries
			discoveredFiles.forEach(filepath -> TSVUtilities.buildEntries(generatedEntries, filepath));
			generatedEntries.forEach(entry -> entry.swapEntries(0, 1)); // Swap first two entries so that the primary index is page ID (nonvolatile) and not page title (volatile)

			// Push entries to database
			sqlInterface.insert(generatedEntries.toArray(new TableEntry[] {}));
		}
		catch (SQLException passedException) {
			passedException.printStackTrace();
			// If the SQLInterface cannot be constructed, there's no point in continuing
			return;
		}
	}
	
	private static TSVConfig getConfig(String passedFilePath) {
		try {
			JsonObjectParser<TSVConfig> configReader = new JsonObjectParser<TSVConfig>(passedFilePath, TSVConfig.class);
			return configReader.fromJson();
		}
		catch (FileNotFoundException passedException) {
			passedException.printStackTrace();
			return null;
		}
	}
}
