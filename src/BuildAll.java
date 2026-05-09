
/**
 * Orchestrates the full website build by running all pipeline stages in order.
 *
 * <p>Stages executed:
 * <ol>
 *   <li>{@link HistoryClean} — normalizes and sorts {@code History.csv}</li>
 *   <li>{@link HistoryGraph} — generates the main Mermaid visualization</li>
 *   <li>{@link HistoryRelationships} — generates per-person relationship pages</li>
 *   <li>{@link CreateTemplates} — creates stub HTML pages for new entries</li>
 *   <li>{@link ConvertIframe} — inlines iframe content into root HTML pages</li>
 * </ol>
 *
 * <p>Pass the base website path as the first argument; if omitted, each stage
 * falls back to its own hardcoded default.
 */
public class BuildAll {

	/**
	 * Runs all pipeline stages sequentially, printing a separator between each.
	 * Aborts with exit code 1 if any stage throws a {@link RuntimeException}.
	 *
	 * @param args optional: args[0] is the base path passed to every stage
	 */
	public static void main(String[] args) {
		int length = 80;
		try {
			HistoryClean.main(args);
			System.out.println("=".repeat(length));
			HistoryGraph.main(args);
			System.out.println("=".repeat(length));
			HistoryRelationships.main(args);
			System.out.println("=".repeat(length));
			CreateTemplates.main(args);
			System.out.println("=".repeat(length));
			ConvertIframe.main(args);
		} catch (RuntimeException e) {
			System.err.println("Build aborted: " + e.getMessage());
			System.exit(1);
		}
	}
}
