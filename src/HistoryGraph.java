import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import com.opencsv.exceptions.CsvException;

/**
 * Pipeline stage 2: generates the main relationship graph as {@code index.html}.
 *
 * <p>Reads the cleaned {@code History.csv} and writes a self-contained HTML page.
 * Set {@link #USE_CYTOSCAPE} to {@code true} for an interactive Cytoscape.js graph
 * (default) or {@code false} to fall back to the Mermaid.js renderer (controlled
 * by {@link #MERMAID}). Reader and writer are always selected as a matched pair.
 */
public class HistoryGraph {
	/**
	 * Selects the renderer: {@code true} → {@link CytoscapeWriter} (interactive);
	 * {@code false} → Mermaid-based renderer chosen by {@link #MERMAID}.
	 */
	private static final boolean USE_CYTOSCAPE = true;
	/**
	 * Only consulted when {@link #USE_CYTOSCAPE} is {@code false}.
	 * {@code true} → {@link MermaidWriter} (client-side JS);
	 * {@code false} → {@link SvgWriter} (pre-rendered SVG, no JS).
	 */
	private static final boolean MERMAID = true;

	/**
	 * Entry point for the graph generation stage. Writes {@code index.html} to
	 * the configured path.
	 *
	 * @param args optional: args[0] is the base path (defaults to {@link HistoryFileProcessor#DEFAULT_PATH})
	 */
	public static void main(String[] args) {
		String path = args.length > 0 ? args[0] : HistoryFileProcessor.DEFAULT_PATH;
		try {
			GraphNode.setPath(path);
			RelationshipReader rr = USE_CYTOSCAPE
					? new CytoscapeReader(path + "History.csv")
					: new MermaidReader(path + "History.csv");
			rr.load();

			try (PrintStream out = new PrintStream(new File(path + "index.html"))) {
				GraphWriter gw;
				if (USE_CYTOSCAPE)   gw = new CytoscapeWriter(out);
				else if (MERMAID)    gw = new MermaidWriter(out);
				else                 gw = new SvgWriter(out);
				gw.writeNames(rr.getNodes());
				gw.writeRelationships(rr.getRelationships());
				gw.close();
				System.out.println("file " + path + "index.html created");
			}
		} catch (IOException | CsvException e) {
			throw new RuntimeException("HistoryGraph failed: " + e.getMessage(), e);
		}
	}

}
