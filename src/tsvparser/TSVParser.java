package tsvparser;

import java.nio.file.Path;
import java.util.List;

import tsvparser.utilities.TSVUtilities;

public class TSVParser {

	public static void main(String[] passedArguments) {
		String testpath = "/Users/macintosh/Documents/Java Projects/gamecip/corpus/";
		List<Path> discoveredFiles = TSVUtilities.getFilesInDirectory(testpath, "tsv");
		discoveredFiles.stream().forEach(System.out::println);
	}
}
