import java.io.PrintStream;
import java.util.List;

/**
 * {@link GraphWriter} implementation that writes a self-contained HTML page
 * containing a Mermaid.js top-down flowchart of the full lineage graph.
 *
 * <p>The constructor writes the HTML header and opens the {@code <pre class="mermaid">}
 * block. {@link #writeNames(List)} and {@link #writeRelationships(List)} append node
 * and edge definitions respectively. {@link #close()} closes all open tags and
 * injects the Mermaid CDN script.
 */
public class MermaidWriter implements GraphWriter {
	private PrintStream out;

	/**
	 * Constructs the writer and emits the HTML preamble and Mermaid block opener.
	 *
	 * @param out stream to write the HTML output to (typically a file stream)
	 */
	public MermaidWriter(PrintStream out) {
		super();
		this.out = out;
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("  <head>");
		out.println("    <meta charset=\"utf-8\">");
		out.println("    <title>Martial Arts history map</title>");
		out.println("  </head>");
		out.println("  <body>");
		out.println("    <div style=\"border:1px solid;width:1800px;overflow:auto;\">");
		out.println("      <pre class=\"mermaid\">");
		out.println("\t%%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%");
		out.println("\tflowchart TD");
	}

	/**
	 * Writes the Mermaid node definitions for all people.
	 * Each node is rendered via {@link GraphNode#toString()}.
	 *
	 * @param names list of graph nodes to emit
	 */
	@Override
	public void writeNames(List<GraphNode> names) {
		out.println("\n\t%% Name section");
		for (GraphNode gn : names)
			out.println(gn);
	}

	/**
	 * Writes the Mermaid edge definitions for all relationships.
	 *
	 * @param relationships list of Mermaid edge strings (e.g. {@code "A --> B"})
	 */
	@Override
	public void writeRelationships(List<String> relationships) {
		out.println("\n\t%% Relationship section");
		for (String rel : relationships)
			out.println("\t" + rel);
	}

	/**
	 * Closes the Mermaid block, injects the Mermaid CDN {@code <script>} tag,
	 * and finishes the HTML document.
	 */
	@Override
	public void close() {
		out.println("      </pre>");
		out.println("    </div>");
		out.println("    <script type=\"module\">");
		out.println("      import mermaid from '" + HistoryFileProcessor.MERMAID_CDN + "';");
		out.println("      mermaid.initialize({ startOnLoad: true });");
		out.println("    </script>");
		out.println("  </body>");
		out.println("</html>");
	}

}
