package tsvparser.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TSVUtilities {
	
	private TSVUtilities(){};
	
	/**
	 * Returns a {@link Stream} containing {@link Path} objects of all files in the passed directory with the passed extension.
	 * <p>
	 * {@link Stream#empty()} will be returned if no such file exists, or if an {@link IOException} occurs.
	 * 
	 * @param passedFilePath - The {@link Path} to look for files in
	 * @param passedFileExtension - The extension to get files for
	 * @return A {@link Stream} of {@link Path} objects corresponding to any such files
	 */
	public static Stream<Path> getFilesInDirectory(String passedFilePath, String passedFileExtension) {
		FileVisitOption options = FileVisitOption.FOLLOW_LINKS;
		try (Stream<Path> discoveredPaths = Files.walk(Paths.get(passedFilePath), options)) {
			return discoveredPaths.filter(Files::isRegularFile).filter(passedPath -> { return (Files.isReadable(passedPath) && TSVUtilities.checkExtension(passedPath, passedFileExtension)); });
		}
		catch (IOException passedException) {
			passedException.printStackTrace();
		}
		return Stream.empty();
	}
	
	/**
	 * Returns whether the {@link File} at the passed {@link Path} has the passed {@code String} extension.
	 * 
	 * @param passedPath - The {@link Path} at which to look for a {@link File}
	 * @param passedExtension - The file exension to check against
	 * @return Whether the {@link File} at the passed {@link Path} has the passed extension, or {@code false} if no such file exists.
	 */
	public static boolean checkExtension(Path passedPath, String passedExtension) {
		String fileName = passedPath.toFile().getName();
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex == -1) {
			return false;
		}
		else {
			return fileName.substring(dotIndex + 1).equals(passedExtension);
		}
	}
	
	/**
	 * Builds a {@link List} of {@link TableEntry} objects from a .tsv file at the {@link Path}.
	 * 
	 * @param passedFilePath - The path at which the .tsv file resides
	 * @return A list of {@link TableEntry} objects constructed from the .tsv file at the passed path, or an empty list if no such file exists.
	 */
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
	
	/**
	 * Returns a single {@link TableEntry} object from the passed {@code String} line.
	 * <p>
	 * Internally, the method calls {@link String#split("\t")} and builds a TableEntry from the
	 * results. No checking for validity of data is performed at this stage.
	 * @param passedString - The {@code String} to split
	 * @return A suitably instantiated {@link TableEntry}.
	 */
	public static TableEntry fromLine(String passedString) {
		return new TableEntry(passedString.split("\t"));
	}

}
