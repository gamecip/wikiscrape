package sqlinterface;

/**
 * Container class representing a single row in the original TSV.
 * 
 * @author Malcolm Riley
 */
public class TableEntry {
	
	private final String[] ENTRIES;
	
	/**
	 * Creates a new TableEntry object initialized with the passed {@code String} array as its entries.
	 * 
	 * @param passedEntries - The entries to use
	 */
	public TableEntry(String[] passedEntries) {
		this.ENTRIES = passedEntries;
	}
	
	/**
	 * Returns an entry from this {@link TableEntry} corresponding to the passed {@code int}.
	 * <p>
	 * The passed {@code int} can be thought of as the column number, starting at zero.
	 * <p>
	 * This method will return a null {@code String} if the passed index is negative or otherwise
	 * out of range.
	 * @param passedEntryIndex - The index of the entry to retrieve
	 * @return The String stored at that column index.
	 */
	public String getEntry(int passedEntryIndex) {
		if (validate(passedEntryIndex)) {
			return this.ENTRIES[passedEntryIndex];
		}
		return null;
	}
	
	/**
	 * Convenience method that calls {@link #getEntry(int)} using {@link EnumEntry#getIndex()} as the index.
	 * 
	 * @param passedEntry - The {@link EnumEntry} to use
	 * @return The String stored at that column index.
	 * @see {@link EnumEntry}, {@link #getEntry(int)}
	 */
	public String getEntry(EnumEntry passedEntry) {
		return this.getEntry(passedEntry.getIndex());
	}
	
	/**
	 * Returns the entire array of entries from this {@link TableEntry} object.
	 * 
	 * @return The array of entries from this instance.
	 */
	public String[] getEntries() {
		return this.ENTRIES;
	}
	
	// Logic Methods
	private boolean validate(int passedInteger) {
		return (passedInteger >= 0) && (passedInteger < this.ENTRIES.length);
	}
}
