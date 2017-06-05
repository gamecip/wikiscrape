package tsvparser;

import java.nio.file.Paths;

import tsvparser.utilities.Entries;
import tsvparser.utilities.TSVUtilities;
import tsvparser.utilities.TableEntry;

public class TSVParser {

	public static void main(String[] passedArguments) {
		String testpath = "/Users/macintosh/Documents/Java Projects/gamecip/corpus/corpus_0.tsv";
		for (TableEntry iteratedEntry : TSVUtilities.buildEntries(Paths.get(testpath))) {
			System.out.println(iteratedEntry.getEntry(Entries.PAGE_ID));
		}
	}
}
