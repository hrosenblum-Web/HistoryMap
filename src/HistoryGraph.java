import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import com.opencsv.exceptions.CsvException;

/**
 * Pipeline stage 2: generates the main Mermaid.js relationship graph.
 *
 * <p>Reads the cleaned {@code History.csv}, builds node and edge definitions
 * using {@link MermaidReader}, and writes a self-contained {@code index.html}
 * via {@link MermaidWriter}. The result is a top-down flowchart of the full
 * martial arts lineage.
 */
public class HistoryGraph {
	private static final boolean DEBUG = false;

	/**
	 * Entry point for the graph generation stage. Writes {@code index.html} to
	 * the configured path. When {@code DEBUG} is {@code true}, output goes to
	 * stdout instead of the file.
	 *
	 * @param args optional: args[0] is the base path (defaults to hardcoded path)
	 */
	public static void main(String[] args) {
		String path = args.length > 0 ? args[0] : HistoryFileProcessor.DEFAULT_PATH;
		try {
			GraphNode.setPath(path);
			RelationshipReader rr = new MermaidReader(path + "History.csv");
			rr.load();

			if (DEBUG) {
				GraphWriter gw = new MermaidWriter(System.out);
				gw.writeNames(rr.getNodes());
				gw.writeRelationships(rr.getRelationships());
				gw.close();
			} else {
				try (PrintStream out = new PrintStream(new File(path + "index.html"))) {
					GraphWriter gw = new SvgWriter(out);
					gw.writeNames(rr.getNodes());
					gw.writeRelationships(rr.getRelationships());
					gw.close();
				}
				System.out.println("file " + path + "index.html created");
			}
		} catch (IOException | CsvException e) {
			throw new RuntimeException("HistoryGraph failed: " + e.getMessage(), e);
		}
	}

}
