package tsvparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import tsvparser.utilities.Entries;
import tsvparser.utilities.TSVUtilities;
import tsvparser.utilities.TableEntry;

public class TSVParser {

	public static void main(String[] passedArguments) {
		String testpath = "/Users/macintosh/Documents/Java Projects/gamecip/corpus/corpus_0.tsv";
		for (TableEntry iteratedEntry : buildEntries(Paths.get(testpath))) {
			System.out.println(iteratedEntry.getEntry(Entries.PAGE_ID));
		}
	}
	
	public static List<TableEntry> buildEntries(Path passedFilePath) {
		ArrayList<TableEntry> entries = new ArrayList<TableEntry>();
		try (BufferedReader reader = Files.newBufferedReader(passedFilePath)) {
			Stream<String> lines = reader.lines();
			// Skip header row - contains labels that are not needed
			lines.skip(1).forEach(string -> entries.add(TSVUtilities.fromLine(string)));
		}
		catch (IOException passedException) {
			passedException.printStackTrace();
		}
		return entries;
	}
}
