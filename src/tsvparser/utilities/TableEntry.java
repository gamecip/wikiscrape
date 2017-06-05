package tsvparser.utilities;

/**
 * Container class representing a single row in the original TSV.
 * 
 * @author Malcolm Riley
 */
public class TableEntry {
	
	private String[] ENTRIES;
	
	public TableEntry(String[] passedEntries) {
		this.ENTRIES = passedEntries;
	}
	
	public String getEntry(int passedEntryIndex) {
		return this.ENTRIES[passedEntryIndex];
	}
}
