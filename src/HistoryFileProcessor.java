
/**
 * Shared constants and utilities for the HistoryMap pipeline.
 *
 * <p>This interface is implemented by classes that read {@code History.csv}
 * directly ({@link RelationshipReader} subclasses) and is referenced statically
 * by all pipeline stages for shared path resolution, CDN URLs, and CSV quoting.
 *
 * <h2>CSV column layout</h2>
 * <table>
 *   <tr><th>Index</th><th>Constant</th><th>Description</th></tr>
 *   <tr><td>0</td><td>{@link #SENIOR_PERSON}</td><td>Teacher, parent, or earlier-generation figure</td></tr>
 *   <tr><td>1</td><td>{@link #JUNIOR_PERSON}</td><td>Student or later-generation figure; may be blank</td></tr>
 *   <tr><td>2</td><td>{@link #RELATIONSHIP}</td><td>Relationship type string; see arrow-style table in CLAUDE.md</td></tr>
 *   <tr><td>3</td><td>{@link #SENIOR_URL}</td><td>Optional external URL for the senior person</td></tr>
 * </table>
 */
public interface HistoryFileProcessor {
	/** Column 0: the more senior person in the relationship (teacher, parent, earlier generation). */
	public static final int SENIOR_PERSON = 0;
	/** Column 1: the more junior person in the relationship; may be blank for standalone entries. */
	public static final int JUNIOR_PERSON = 1;
	/** Column 2: relationship label, e.g. {@code "sensei"}, {@code "family"}, {@code "partner"}, {@code "trained by"}. */
	public static final int RELATIONSHIP  = 2;
	/** Column 3: external URL for the senior person's profile page; may be blank. */
	public static final int SENIOR_URL    = 3;

	/** CDN URL for the Mermaid.js ESM module used in all generated HTML pages. */
	public static final String MERMAID_CDN =
			"https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs";

	/**
	 * Mermaid diagram init directive prepended to every generated chart.
	 * Disables HTML labels so node text renders as plain text, which avoids
	 * rendering issues in environments that sanitize HTML inside SVG.
	 */
	public static final String MERMAID_INIT = "%%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%";

	/**
	 * Default base path used by all pipeline stages when no command-line argument is supplied.
	 * Points to the local testing copy of the website; switch to the production path for a
	 * live deploy.
	 */
	public static final String DEFAULT_PATH = "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";

	/**
	 * Returns the base path to use for this pipeline run.
	 *
	 * @param args command-line arguments passed to {@code main}
	 * @return {@code args[0]} if present, otherwise {@link #DEFAULT_PATH}
	 */
	public static String resolvePath(String[] args) {
		return args.length > 0 ? args[0] : DEFAULT_PATH;
	}

	/**
	 * Quotes a CSV field value only when necessary.
	 *
	 * <p>Returns {@code value} unchanged if it contains no double quotes, commas,
	 * or newlines. Otherwise wraps it in double quotes and escapes any embedded
	 * double quotes by doubling them, per RFC 4180.
	 *
	 * @param value the raw field value to quote
	 * @return a CSV-safe representation of {@code value}
	 */
	public static String csvQuote(String value) {
		if (value.contains("\"") || value.contains(",") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}
}
