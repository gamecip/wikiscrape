package tsvparser;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sqlinterface.SQLInterface;
import sqlinterface.TableEntry;
import tsvparser.utilities.TSVUtilities;

public class TSVParser {
	
	private static String TSV_EXTENSION = "tsv";

	public static void main(String[] passedArguments) {
		// Construct objects
		String path = "";
		
		// TODO!
		String sqlurl = "";
		String tablename = "";
		String username = "";
		String password = "";
		
		List<Path> discoveredFiles = TSVUtilities.getFilesInDirectory(path, TSV_EXTENSION);
		List<TableEntry> generatedEntries = new ArrayList<TableEntry>();
		
		try {
			SQLInterface sqlInterface = new SQLInterface(sqlurl, tablename, username, password);
			
			// Generate entries
			discoveredFiles.forEach(filepath -> TSVUtilities.buildEntries(generatedEntries, filepath));
			generatedEntries.forEach(entry -> entry.swapEntries(0, 1)); // Swap first two entries so that the primary index is page ID (nonvolatile) and not page title (volatile)
			
			// Push entries to database
			sqlInterface.insert(generatedEntries.toArray(new TableEntry[]{}));
		}
		catch (SQLException passedException) {
			passedException.printStackTrace();
			// If the SQLInterface cannot be constructed, there's no point in continuing
			return;
		}
	}
}
