package wikiscrape.utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;

/**
 * Utility class for reading a POJO from a JSON file.
 * 
 * @author Malcolm Riley
 * 
 * @param <T> - The Object type to be converted from JSON
 */
public class JsonObjectParser<T> {
	
	private Gson GSON;
	private FileReader FILE_READER;
	private Class<T> OBJECT_CLASS;
	
	/**
	 * Creates a new {@link JsonObjectParser} instance from the passed parameters.
	 * 
	 * @param passedFileName - The name and/or path to the configuration file to use
	 * @param passedClass - The class of object to be deserialized from JSON
	 * @throws FileNotFoundException - If the specified file or path is invalid or otherwise unavailable
	 */
	public JsonObjectParser(String passedFileName, Class<T> passedClass) throws FileNotFoundException {
		this.FILE_READER = new FileReader(passedFileName);
		this.GSON = new Gson();
	}
	
	/**
	 * Returns a new instance of the stored {@code Class<T>}, read from the specified JSON file.
	 * @return A new instance of the specified class.
	 */
	public T fromJson() {
		return this.GSON.fromJson(this.FILE_READER, this.OBJECT_CLASS);
	}
}
