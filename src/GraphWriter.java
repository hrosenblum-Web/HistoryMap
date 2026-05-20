import java.util.List;

/**
 * Output sink for a relationship graph.
 *
 * <p>Implementations buffer node and edge data during {@link #writeNames} and
 * {@link #writeRelationships}, then emit a complete self-contained HTML document
 * in {@link #close()}. Three implementations are provided:
 * <ul>
 *   <li>{@link MermaidWriter} — client-side Mermaid.js rendering via CDN script</li>
 *   <li>{@link SvgWriter} — server-fetched SVG from {@code mermaid.ink}, no JS required</li>
 *   <li>{@link CytoscapeWriter} — interactive Cytoscape.js graph with dagre layout</li>
 * </ul>
 */
public interface GraphWriter {

	/**
	 * Writes (or buffers) the node definitions for all people in the graph.
	 *
	 * @param names list of nodes to write
	 */
	public void writeNames(List<GraphNode> names);

	/**
	 * Writes (or buffers) the edge definitions connecting nodes.
	 *
	 * @param relationships list of relationship strings in the target format
	 */
	public void writeRelationships(List<String> relationships);

	/**
	 * Flushes any buffered content and closes the underlying output,
	 * writing any required footer markup to complete the HTML document.
	 */
	public void close();

}