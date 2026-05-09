import java.io.IOException;

import com.opencsv.exceptions.CsvException;

/**
 * A {@link RelationshipReader} that produces Mermaid.js flowchart edge syntax.
 *
 * <p>Relationship types map to distinct Mermaid arrow styles:
 * <ul>
 *   <li>{@code sensei} — solid arrow ({@code -->})</li>
 *   <li>{@code family} — thick arrow ({@code ==>})</li>
 *   <li>{@code partner} — bidirectional dashed arrow ({@code <-.->})</li>
 *   <li>anything else — dashed arrow with label ({@code -.label.->})</li>
 * </ul>
 */
public class MermaidReader extends RelationshipReader {

	/**
	 * Parses the given CSV file and produces Mermaid node and edge definitions.
	 *
	 * @param fileName absolute path to {@code History.csv}
	 * @throws IOException  if the file cannot be read
	 * @throws CsvException if the CSV is malformed
	 */
	public MermaidReader(String fileName) throws IOException, CsvException {
		super(fileName);
	}

	/**
	 * Appends a Mermaid edge string to {@link RelationshipReader#relationships}
	 * using an arrow style determined by the relationship type.
	 *
	 * @param id1          sanitized ID of the senior person
	 * @param id2          sanitized ID of the junior person
	 * @param relationship relationship label from the CSV (e.g. {@code "sensei"})
	 */
	@Override
	protected void createRelationship(String id1, String id2, String relationship) {
		switch (relationship) {
		case "family":
			relationships.add(id1 + " ==> " + id2);
			break;
		case "sensei":
			relationships.add(id1 + " --> " + id2);
			break;
		case "partner":
			relationships.add(id1 + " <-.-> " + id2);
			break;
		case "": // no relationship
			break;
		default:
			relationships.add(id1 + " -." + relationship + ".-> " + id2);
			break;
		}
	}

	/**
	 * Creates a {@link MermaidNode} for the given person name.
	 *
	 * @param name display name from the CSV
	 * @return new {@code MermaidNode} instance
	 */
	@Override
	protected GraphNode createNode(String name) {
		return new MermaidNode(name);
	}

}
