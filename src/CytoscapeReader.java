/**
 * A {@link RelationshipReader} that produces Cytoscape.js element JSON strings.
 *
 * <p>Each relationship is stored as a full Cytoscape edge element:
 * {@code {data:{source:"A",target:"B",type:"sensei"}}}. Empty relationships
 * produce no edge. The {@code type} value is the lowercased CSV relationship
 * label, which {@link CytoscapeWriter} maps to distinct visual styles.
 */
public class CytoscapeReader extends RelationshipReader {

    /**
     * Constructs a reader for the given CSV file. Call {@link #load()} to parse it.
     *
     * @param fileName absolute path to {@code History.csv}
     */
    public CytoscapeReader(String fileName) {
        super(fileName);
    }

    /**
     * Creates a plain {@link GraphNode} preserving the external URL when present,
     * so that click-through navigation in the Cytoscape graph uses the correct link.
     *
     * @param name display name from the CSV
     * @param url  external URL; empty string falls back to the local biography page
     * @return new {@code GraphNode}
     */
    @Override
    protected GraphNode createNode(String name, String url) {
        return new GraphNode(name, url);
    }

    /**
     * Appends a Cytoscape edge element JSON string to {@link RelationshipReader#relationships}.
     * Empty relationships are silently skipped.
     *
     * @param id1          sanitized ID of the senior person
     * @param id2          sanitized ID of the junior person
     * @param relationship relationship label from the CSV (e.g. {@code "sensei"})
     */
    @Override
    protected void createRelationship(String id1, String id2, String relationship) {
        if (relationship.isEmpty()) return;
        relationships.add(String.format(
            "{data:{source:\"%s\",target:\"%s\",type:\"%s\"}}",
            id1, id2, relationship.toLowerCase()));
    }
}
