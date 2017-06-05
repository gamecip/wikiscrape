package tsvparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import tsvparser.utilities.TableEntry;

public class TSVParser {

	public static void main(String[] passedArguments) {
		String testpath = "/Users/macintosh/Documents/Java Projects/gamecip/corpus/corpus_0.tsv";
		buildEntries(Paths.get(testpath));
	}
	
	public static List<TableEntry> buildEntries(Path passedFilePath) {
		ArrayList<TableEntry> entries = new ArrayList<TableEntry>();
		try (BufferedReader reader = Files.newBufferedReader(passedFilePath)) {
			Stream<String> lines = reader.lines();
		}
		catch (IOException passedException) {
			passedException.printStackTrace();
		}
		return entries;
	}
}
