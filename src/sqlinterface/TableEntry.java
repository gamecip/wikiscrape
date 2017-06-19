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
	 * Convenience method to swap the entries at the passed indices.
	 * <p>
	 * This is a fix for the fact that the first column in the original tsv files is the article name, which
	 * is volatile and therefore should not be used as the SQL primary key.
	 * 
	 * @param passedFirstIndex - The index of the entry to swap with {@code passedSecondIndex}
	 * @param passedSecondIndex - The index of the entry to swap with {@code passedFirstIndex}
	 * @throws IllegalArgumentException If {@code passedFirstIndex} or {@code passedSecondIndex} are out of array bounds.
	 */
	public void swapEntries(int passedFirstIndex, int passedSecondIndex) {
		if (!validate(passedFirstIndex) || !validate(passedSecondIndex)) {
			throw new IllegalArgumentException("May not swap indices out of array bounds!");
		}
		String firstIndex = this.ENTRIES[passedFirstIndex];
		String secondIndex = this.ENTRIES[passedSecondIndex];
		
		this.ENTRIES[passedFirstIndex] = secondIndex;
		this.ENTRIES[passedSecondIndex] = firstIndex;
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
	 * Sets an entry in this {@link TableEntry} corresponding to the passed {@code int}.
	 * <p>
	 * The passed {@code int} can be thought of as the column number, starting at zero.
	 * <p>
	 * This method will do nothing if the passed index is negative or otherwise out of range.
	 * @param passedEntryIndex - The index of the entry to set
	 * @param passedEntryValue - The new value to set at that index
	 */
	public void setEntry(int passedEntryIndex, String passedEntryValue) {
		if (validate(passedEntryIndex)) {
			this.ENTRIES[passedEntryIndex] = passedEntryValue;
		}
	}
	
	/**
	 * Convenience method that calls {@link #setEntry(int, String)} using {@link EnumEntry#getIndex()} as the index.
	 * 
	 * @param passedEntry - The entry to set
	 * @param passedEntryValue - The new value to set at that entry index
	 */
	public void setEntry(EnumEntry passedEntry, String passedEntryValue) {
		this.setEntry(passedEntry.getIndex(), passedEntryValue);
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
