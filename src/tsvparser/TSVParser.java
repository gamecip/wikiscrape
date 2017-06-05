package tsvparser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import tsvparser.utilities.TSVUtilities;
import tsvparser.utilities.TableEntry;

public class TSVParser {
	
	private static String TSV_EXTENSION = "tsv";

	public static void main(String[] passedArguments) {
		String testpath = "/Users/macintosh/Documents/Java Projects/gamecip/corpus/";
		List<Path> discoveredFiles = TSVUtilities.getFilesInDirectory(testpath, TSV_EXTENSION);
		List<TableEntry> generatedEntries = new ArrayList<TableEntry>();
		discoveredFiles.stream().forEach(path -> TSVUtilities.buildEntries(generatedEntries, path));
	}
}
