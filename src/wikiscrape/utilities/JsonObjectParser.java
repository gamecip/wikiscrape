package wikiscrape.utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;

public class JsonObjectParser<T> {
	
	private Gson GSON;
	private FileReader FILE_READER;
	private Class<T> OBJECT_CLASS;
	
	public JsonObjectParser(String passedFileName, Class<T> passedClass) throws FileNotFoundException {
		this.FILE_READER = new FileReader(passedFileName);
		this.GSON = new Gson();
	}
	
	public T fromJson() {
		return this.GSON.fromJson(this.FILE_READER, this.OBJECT_CLASS);
	}
}
