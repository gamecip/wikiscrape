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

import sqlinterface.TableEntry;

public class TSVUtilities {
	
	private TSVUtilities(){};
	
	/**
	 * Builds a {@link List} containing {@link Path} objects of all files in the passed directory with the passed extension.
	 * <p>
	 * An empty {@link List} will be returned if no such file exists, or if an {@link IOException} occurs.
	 * 
	 * @param passedFilePath - The {@link Path} to look for files in
	 * @param passedFileExtension - The extension to get files for
	 * @return A {@link List} of {@link Path} objects corresponding to any such files
	 */
	public static List<Path> getFilesInDirectory(String passedFilePath, String passedFileExtension) {
		ArrayList<Path> paths = new ArrayList<Path>();
		try (Stream<Path> discoveredPaths = Files.walk(Paths.get(passedFilePath), FileVisitOption.FOLLOW_LINKS)) {
			discoveredPaths.forEach(path -> { if (validatePath(path) && checkExtension(path, passedFileExtension)) paths.add(path); } );
		}
		catch (IOException passedException) {
			passedException.printStackTrace();
		}
		return paths;
	}
	
	/**
	 * Returns whether the {@link File} at the passed {@link Path} has the passed {@code String} extension.
	 * 
	 * @param passedPath - The {@link Path} at which to look for a {@link File}
	 * @param passedExtension - The file extension to check against
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
	 * Adds to a passed {@link List} all the {@link TableEntry} objects constructed from a file at the {@link Path}.
	 * <p>
	 * Note that no {@link TableEntry} objects will be added to the list if the {@link Path}-indicated file cannot be
	 * read.
	 * @param passedFilePath - The path at which the .tsv file resides
	 * @param passedList - The {@link List} to add the constructed {@link TableEntry} objects to
	 */
	public static void buildEntries(List<TableEntry> passedList, Path passedFilePath) {
		try (BufferedReader reader = Files.newBufferedReader(passedFilePath)) {
			Stream<String> lines = reader.lines();
			// Skip header row - contains labels that are not needed
			lines.skip(1).forEach(string -> passedList.add(TSVUtilities.fromLine(string)));
		}
		catch (IOException passedException) {
			passedException.printStackTrace();
		}
	}
	
	/**
	 * Returns a single {@link TableEntry} object from the passed {@code String} line.
	 * <p>
	 * Internally, the method calls {@link String#split()} and builds a {@link TableEntry} from the
	 * results. No checking for validity of data is performed at this stage.
	 * @param passedString - The {@code String} to split
	 * @return A suitably instantiated {@link TableEntry}.
	 */
	public static TableEntry fromLine(String passedString) {
		return new TableEntry(passedString.split("\t"));
	}
	
	// Logic Methods 
	
	private static boolean validatePath(Path passedPath) {
		return Files.isReadable(passedPath) && Files.isRegularFile(passedPath);
	}

}
