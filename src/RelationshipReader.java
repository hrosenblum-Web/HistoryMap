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
 * <p>Subclasses implement {@link #createNode(String, String)} and
 * {@link #createRelationship(String, String, String)} to produce output in a
 * specific format (Mermaid syntax, HTML, etc.) without duplicating CSV parsing
 * logic.
 *
 * <p>Construction stores the filename; call {@link #load()} to trigger parsing.
 * Separating construction from parsing allows subclass instance fields to be
 * fully initialized before any abstract methods are invoked.
 */
public abstract class RelationshipReader implements HistoryFileProcessor {

	private final String fileName;
	/** People encountered in the CSV, keyed by their sanitized ID. */
	protected Map<String, GraphNode> nodes = new HashMap<>();
	/** Relationship strings in the format produced by the subclass. */
	protected List<String> relationships = new ArrayList<>();

	/**
	 * Stores the CSV filename. Call {@link #load()} to parse it.
	 *
	 * @param fileName absolute path to {@code History.csv}
	 */
	public RelationshipReader(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Reads the CSV file, skipping the header row, and populates {@link #nodes}
	 * and {@link #relationships} by delegating to the abstract factory methods.
	 * Rows with fewer than three columns are silently skipped.
	 *
	 * <p>When a {@code SENIOR_URL} column is present, the URL is passed to
	 * {@link #createNode(String, String)} so subclasses can use it if needed.
	 * A URL-bearing node for the same person takes precedence over a no-URL node
	 * recorded from an earlier row.
	 *
	 * @throws IOException  if the file cannot be read
	 * @throws CsvException if the CSV is malformed
	 */
	public void load() throws IOException, CsvException {
		try (CSVReader fileReader = new CSVReaderBuilder(new FileReader(fileName)).build()) {
			fileReader.readNext(); // ignore header

			for (String[] row : fileReader.readAll()) {
				if (row.length < 3)
					continue;

				String seniorUrl = (row.length > SENIOR_URL) ? row[SENIOR_URL].trim() : "";
				GraphNode node1 = createNode(row[SENIOR_PERSON], seniorUrl);
				// URL-bearing nodes take precedence so external links are not lost
				// if a person appears first as a junior (no URL) in an earlier row.
				if (!seniorUrl.isEmpty())
					nodes.put(node1.getId(), node1);
				else
					nodes.putIfAbsent(node1.getId(), node1);

				if (row[JUNIOR_PERSON].isBlank()) // standalone entry with no relationship
					continue;

				GraphNode node2 = createNode(row[JUNIOR_PERSON], "");
				// catch the case where the only mention of the person is only as a junior person
				nodes.putIfAbsent(node2.getId(), node2);

				createRelationship(node1.getId(), node2.getId(), row[RELATIONSHIP]);
			}
		}
	}

	/**
	 * Factory method: creates the appropriate {@link GraphNode} subtype for a person.
	 *
	 * @param name display name from the CSV column
	 * @param url  external URL from the {@code SENIOR_URL} column; empty string if absent
	 * @return a new node representing this person
	 */
	protected abstract GraphNode createNode(String name, String url);

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
