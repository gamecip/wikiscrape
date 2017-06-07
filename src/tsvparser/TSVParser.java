package tsvparser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import sqlinterface.TableEntry;
import tsvparser.utilities.TSVUtilities;

public class TSVParser {
	
	private static String TSV_EXTENSION = "tsv";

	public static void main(String[] passedArguments) {
		// Generate table entries
		String testpath = "/Users/macintosh/Documents/Java Projects/gamecip/corpus/";
		List<Path> discoveredFiles = TSVUtilities.getFilesInDirectory(testpath, TSV_EXTENSION);
		List<TableEntry> generatedEntries = new ArrayList<TableEntry>();
		discoveredFiles.forEach(path -> TSVUtilities.buildEntries(generatedEntries, path));
		generatedEntries.forEach(entry -> entry.swapEntries(0, 1)); // Swap first two entries so that the primary index is page ID (nonvolatile) and not page title (volatile)
		
		// Push entries to database
	}
}
