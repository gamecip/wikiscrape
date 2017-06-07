package sqlinterface;

public enum EnumEntry {

	PAGE_ID(0, "pageid", "INTEGER"),
	TITLE(1, "title", "TEXT"),
	REVISION_ID(2, "revisionid", "INTEGER"),
	YEAR(3, "year", "INTEGER"),
	TEXT_INTRO(4, "textintro", "TEXT"),
	TEXT_FULL(5, "textfull", "TEXT"),
	CATEGORIES(6, "categories", "TEXT"),
	;
	
	private int INDEX;
	private String NAME;
	private String TYPE;
	
	private EnumEntry(int passedIndex, String passedName, String passedSQLType) {
		this.INDEX = passedIndex;
		this.NAME = passedName;
		this.TYPE = passedSQLType;
	}
	
	public int getIndex() {
		return this.INDEX;
	}
	
	public String getEntryName() {
		return this.NAME;
	}
	
	public String getSQLType() {
		return this.TYPE;
	}
}
