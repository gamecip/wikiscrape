package queries;

/**
 * Defines a list of known Wiki query API calls.
 * 
 * @author Malcolm Riley
 */
public class Query {
	
	private Query(){};
	
	public static class PageSpecification {
		
		private PageSpecification(){};
		
		/* Page Specifications by: */
		public static final String BY_IDS = "pageids";
		public static final String BY_TITLES = "titles";
		@Deprecated
		/**
		 * Polling Wikipedia pages by revision ID is reportedly very expensive for their servers.
		 * <p>
		 * The query parameter is included here for completeness but use of it should always be avoided if possible.
		 */
		public static final String BY_REVISION_IDS = "revids";
	}
	
	public static class Properties {
		
		private Properties(){};
		
		public static final String NAME = "prop";
		
		public static class Revisions {
			private Revisions(){};
			
			public static final String NAME = "revisions";
			
			/* Revision property parameters */
			public static final String OPTION_META = "rvprop";

			public static final String OPTION_META_IDS = "ids";
			public static final String OPTION_META_FLAGS = "flags";
			public static final String OPTION_META_CONTENT = "content";
			public static final String OPTION_META_SIZE = "size";
		}
		
		public static class Extracts {
			private Extracts(){};
			
			public static final String NAME = "extracts";
			
			public static final String OPTION_PLAINTEXT = "explaintext";
			
			public static final String OPTION_SECTIONFORMAT = "exsectionformat";
			public static final String OPTION_SECTIONFORMAT_PLAIN = "plain";
			public static final String OPTION_SECTIONFORMAT_WIKI = "wiki";
			public static final String OPTION_SECTIONFORMAT_RAW = "raw";
		}
	}
}
