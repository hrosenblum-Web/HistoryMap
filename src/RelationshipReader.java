import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

/**
 * Abstract base class that parses {@code History.csv} and builds a map of
 * {@link GraphNode} objects and a list of relationship strings.
 *
 * <p>Subclasses implement {@link #createNode(String)} and
 * {@link #createRelationship(String, String, String)} to produce output in a
 * specific format (Mermaid syntax, HTML, etc.) without duplicating CSV parsing
 * logic.
 */
public abstract class RelationshipReader implements HistoryFileProcessor {

	private CSVReader fileReader;
	/** People encountered in the CSV, keyed by their sanitized ID. */
	protected Map<String, GraphNode> nodes = new HashMap<>();
	/** Relationship strings in the format produced by the subclass. */
	protected List<String> relationships = new ArrayList<>();

	/**
	 * Reads the CSV file, skipping the header row, and populates {@link #nodes}
	 * and {@link #relationships} by delegating to the abstract factory methods.
	 * Rows with fewer than three columns are silently skipped.
	 *
	 * @param fileName absolute path to {@code History.csv}
	 * @throws IOException  if the file cannot be read
	 * @throws CsvException if the CSV is malformed
	 */
	public RelationshipReader(String fileName) throws IOException, CsvException {

		GraphNode node1, node2;
		fileReader = new CSVReaderBuilder(new FileReader(fileName)).build();
		fileReader.readNext();  //ignore header

		for (String[] row : fileReader.readAll()) {
			if (row.length < 3)
				continue;
			node1 = createNode(row[SENIOR_PERSON]);
			// putIfAbsent so that a person who appears in multiple rows keeps the
			// node created from their first occurrence rather than being overwritten.
			nodes.putIfAbsent(node1.getId(), node1);

			if (row[JUNIOR_PERSON].isBlank()) // standalone entry with no relationship
				continue;

			node2 = createNode(row[JUNIOR_PERSON]);
			nodes.putIfAbsent(node2.getId(), node2);

			createRelationship(node1.getId(), node2.getId(), row[RELATIONSHIP]);
		}
	}

//	protected abstract GraphNode createNode(String name, String url);

	/**
	 * Factory method: creates the appropriate {@link GraphNode} subtype for a
	 * person name.
	 *
	 * @param name display name from the CSV column
	 * @return a new node representing this person
	 */
	protected abstract GraphNode createNode(String name);

	/**
	 * Records the directed relationship between two people in
	 * {@link #relationships} using the subclass-specific format.
	 *
	 * @param id1          sanitized ID of the senior person
	 * @param id2          sanitized ID of the junior person
	 * @param relationship raw relationship label from the CSV
	 */
	protected abstract void createRelationship(String id1, String id2, String relationship);

	/**
	 * Returns all nodes parsed from the CSV.
	 *
	 * @return list of graph nodes (order not guaranteed)
	 */
	public List<GraphNode> getNodes() {
		return nodes.values().stream().toList();
	}

	/**
	 * Returns all relationship strings produced by
	 * {@link #createRelationship(String, String, String)}.
	 *
	 * @return list of relationship strings
	 */
	public List<String> getRelationships() {
		return relationships;
	}

}
