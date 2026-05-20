import java.io.IOException;

import com.opencsv.exceptions.CsvException;

/**
 * Pipeline stage 3: generates per-person HTML relationship pages.
 *
 * <p>Reads the cleaned {@code History.csv} using {@link SimpleReader}, which
 * builds a bidirectional relationship map, then writes one HTML file per person
 * into the {@code Relationships/} directory. Each file is a self-contained
 * Mermaid flowchart centred on that person, with click-through links to related
 * people's biography pages.
 */
public class HistoryRelationships {
	/** When {@code true}, prints all relationships to stdout instead of saving files. */
	private static final boolean DEBUG = false;

	/**
	 * Entry point for the relationship page generation stage. When {@code DEBUG}
	 * is {@code true}, prints all relationships to stdout instead of saving files.
	 *
	 * @param args optional: args[0] is the base path (defaults to hardcoded path)
	 */
	public static void main(String[] args) {
		String path = HistoryFileProcessor.resolvePath(args);
		try {
			SimpleReader sr = new SimpleReader(path + "History.csv", path);
			sr.load();
			if (DEBUG)
				sr.print();
			else
				sr.save();
		} catch (IOException | CsvException e) {
			throw new RuntimeException("HistoryRelationships failed: " + e.getMessage(), e);
		}
	}

}
