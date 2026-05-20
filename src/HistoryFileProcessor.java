
/**
 * Defines column indices for the History.csv input file.
 * Implemented by classes that read the CSV directly.
 */
public interface HistoryFileProcessor {
	/** Column 0: the more senior person in the relationship (teacher, parent, earlier generation). */
	public static final int SENIOR_PERSON = 0;
	/** Column 1: the more junior person in the relationship; may be blank for standalone entries. */
	public static final int JUNIOR_PERSON = 1;
	/** Column 2: relationship label, e.g. "sensei", "family", "partner", "trained by". */
	public static final int RELATIONSHIP  = 2;
	/** Column 3: external URL for the senior person's profile page; may be blank. */
	public static final int SENIOR_URL    = 3;

	/** CDN URL for the Mermaid.js ESM module used in all generated HTML pages. */
	public static final String MERMAID_CDN =
			"https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs";

	/** Mermaid diagram init directive that disables HTML labels for all generated charts. */
	public static final String MERMAID_INIT = "%%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%";

	/** Default base path used by all pipeline stages when no argument is supplied. */
	public static final String DEFAULT_PATH = "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";

	/** Returns args[0] if provided, otherwise DEFAULT_PATH. */
	public static String resolvePath(String[] args) {
		return args.length > 0 ? args[0] : DEFAULT_PATH;
	}

	/**
	 * Wraps {@code value} in double quotes and escapes embedded quotes if the value
	 * contains a double quote, comma, or newline — characters that would break CSV parsing.
	 */
	public static String csvQuote(String value) {
		if (value.contains("\"") || value.contains(",") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}
}
