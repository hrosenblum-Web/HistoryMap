import java.util.List;

/**
 * Output sink for a relationship graph. Implementations write nodes and edges
 * to a specific format (e.g. Mermaid.js HTML).
 */
public interface GraphWriter {

	/**
	 * Writes the node definitions for all people in the graph.
	 *
	 * @param names list of nodes to write
	 */
	public void writeNames(List<GraphNode> names);

	/**
	 * Writes the edge definitions connecting nodes.
	 *
	 * @param relationships list of relationship strings in the target format
	 */
	public void writeRelationships(List<String> relationships);

	/**
	 * Flushes and closes the underlying output, writing any required footer markup.
	 */
	public void close();

}