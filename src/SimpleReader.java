import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.exceptions.CsvException;

/**
 * A {@link RelationshipReader} that generates per-person HTML relationship pages.
 *
 * <p>Relationships are stored bidirectionally in {@link #relationshipMap}: if
 * person A is the sensei of person B, the map records "Sensei → [A]" for B and
 * "Deshi → [B]" for A. Calling {@link #save()} writes one HTML file per person
 * into the {@code Relationships/} directory.
 */
public class SimpleReader extends RelationshipReader {
	/** Nested map: personId → (relationshipLabel → list of related personIds). */
	private Map<String, Map<String, List<String>>> relationshipMap = new HashMap<>();
	private String path;

	/**
	 * Constructs a reader for the given CSV file and configures output paths.
	 * Creates the {@code Relationships/} subdirectory if it does not already exist.
	 * Call {@link #load()} to parse the CSV.
	 *
	 * @param fileName absolute path to {@code History.csv}
	 * @param path     absolute base path ending with a backslash
	 */
	public SimpleReader(String fileName, String path) {
		super(fileName);
		this.path = path + "Relationships\\";
		GraphNode.setPath(path);
		File file = new File(path + "Relationships");
		if (!file.exists()) {
			if (file.mkdirs())
				System.err.println("CREATE " + file.getAbsolutePath());
			else
				System.err.println("Unable to create " + file.getAbsolutePath());
		}
	}

	/**
	 * Creates a plain {@link GraphNode} for the given person.
	 * The external URL is intentionally ignored so relationship pages always
	 * link to local biography pages for consistent site navigation.
	 *
	 * @param name display name from the CSV
	 * @param url  external URL (ignored)
	 * @return new {@code GraphNode} with a local biography page URL
	 */
	@Override
	protected GraphNode createNode(String name, String url) {
		return new GraphNode(name);
	}

	/**
	 * Records the relationship in both directions in {@link #relationshipMap}.
	 * The senior-to-junior direction is stored as-is (e.g. "Sensei"); the
	 * junior-to-senior direction uses the mirrored label (e.g. "Deshi").
	 *
	 * @param id1          sanitized ID of the senior person
	 * @param id2          sanitized ID of the junior person
	 * @param relationship raw relationship label from the CSV
	 */
	@Override
	protected void createRelationship(String id1, String id2, String relationship) {
		if (relationship.isEmpty())
			return;
		Map<String, List<String>> relationships;
		List<String> ids;
		// Capitalize so labels display consistently regardless of CSV casing.
		relationship = Character.toUpperCase(relationship.charAt(0)) + relationship.substring(1).toLowerCase();

		// Expand abbreviated labels that OpenCSV may split on whitespace.
		if (relationship.equals("Trained"))
			relationship += " by";

		if (relationship.equals("Maybe"))
			relationship += " taught by";

		// "Family" is ambiguous — disambiguate by direction before storing.
		if (relationship.equals("Family"))
			relationship = "Earlier generation";

		// Store id1 under id2's entry using the as-written label (senior's perspective).
		// e.g. for row "Ueshiba, Tohei, Sensei": Tohei's page lists Ueshiba as "Sensei".
		relationships = relationshipMap.getOrDefault(id2, new HashMap<>());
		ids = relationships.getOrDefault(relationship, new ArrayList<>());
		ids.add(id1);
		relationships.put(relationship, ids);
		relationshipMap.put(id2, relationships);

		// Flip the label before storing the reverse direction (junior's perspective).
		// e.g. Ueshiba's page lists Tohei as "Deshi".
		if (relationship.equals("Sensei"))
			relationship = "Deshi";

		if (relationship.equals("Maybe taught by"))
			relationship = "Maybe taught";

		if (relationship.equals("Trained by"))
			relationship = "Trained";

		if (relationship.equals("Earlier generation"))
			relationship = "Later generation";

		relationships = relationshipMap.getOrDefault(id1, new HashMap<>());
		ids = relationships.getOrDefault(relationship, new ArrayList<>());
		ids.add(id2);
		relationships.put(relationship, ids);
		relationshipMap.put(id1, relationships);
	}

	/**
	 * Prints a full HTML summary of all relationships to {@code System.out}.
	 * Intended for debug use; {@link #save()} is the production equivalent.
	 */
	public void print() {
		Set<String> personIds = relationshipMap.keySet();
		System.out.println("<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "\t<title>Relationship summary</title>\n"
				+ "</head>\n"
				+ "<body>\n");
		for (String personId : personIds) {
			System.out.println("\t<h2>" + personId + "</h2>");
			printTextData(personId, System.out);
		}
		System.out.println("</body>\n"
				+ "</html>");
	}

	/**
	 * Writes one HTML relationship page per person into the {@code Relationships/}
	 * directory. Each file is named {@code <id>.html}.
	 */
	public void save() {
		Set<String> personIds = relationshipMap.keySet();
		for (String personId : personIds) {
			try (PrintStream out = new PrintStream(new File(path + personId + ".html"))) {
				printChartData(personId, out);
				System.out.println(path + personId + ".html created");
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Failed to write relationship page for " + personId, e);
			}
		}
	}

	private void printTextData(String personId, PrintStream out) {
		out.println("<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "\t<title>" + personId + " Relationships</title>\n"
				+ "</head>\n"
				+ "<body>\n");
		Map<String, List<String>> relationships = relationshipMap.get(personId);
		for (String relationship : relationships.keySet()) {
			out.println("\t<h3>" + relationship + "</h3>");
			out.println("\t<ul>");
			for (String person : relationships.get(relationship)) {
				GraphNode gn = nodes.get(person);
				String name = gn.getName().replace("\n", " ");
				out.print("\t\t<li>");
				out.print("<a href=\"" + gn.getUrl() + "\" target=\"_parent\">" + name + "</a>");
				out.println("</li>");
			}
			out.println("\t</ul>");
		}
		out.println("</body>\n"
				+ "</html>");
	}

	private void printChartData(String personId, PrintStream out) {
		out.println(""
				+ "<div>\n"
				+ "  <hr/>\n"
				+ "  <h3>Relationship Chart</h3>\n"
				+ "   <pre class=\"mermaid\">\n"
				+ "      %%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%\n"
				+ "      flowchart LR\n");

		Map<String, List<String>> relationships = relationshipMap.get(personId);
		GraphNode gn = nodes.get(personId);
		out.printf("      %s((%s))%n", personId, cleanName(gn.getName()));
		if (gn.hasImage()) {
			out.printf("      %s@{ img: \"%s\", label: \"%s\", h: 100, constraint: \"on\" }%n", gn.getId(), gn.getImage(), gn.getName());
		}
		boolean rightArrow;
		for (String relationship : relationships.keySet()) {
			out.printf("%n      %%%% %s relationships%n", relationship);
			// Arrow points toward the subject when the relationship is "incoming"
			// (the subject is the junior/recipient). For outgoing relationships the
			// subject points outward to the relationship node.
			if (relationship.equalsIgnoreCase("sensei") ||
					relationship.equalsIgnoreCase("Maybe taught by") ||
					relationship.equalsIgnoreCase("Trained by") ||
					relationship.equalsIgnoreCase("Earlier generation"))
				rightArrow = false;
			else
				rightArrow = true;
			// Mermaid node IDs cannot contain spaces; replace with underscores.
			String relationshipId = relationship.replace(" ", "_");
			// Only emit a labelled node declaration when the ID was changed; single-word
			// relationship types are used as their own label implicitly.
			if (!relationshipId.equals(relationship))
				out.printf("      %s[%s]%n", relationshipId, relationship);
			if (rightArrow)
				out.printf("      %s --> %s%n", personId, relationshipId);
			else
				out.printf("      %s --> %s%n", relationshipId, personId);
			for (String person : relationships.get(relationship)) {
				gn = nodes.get(person);
				String name = cleanName(gn.getName());
				if (rightArrow)
					out.printf("      %s --> %s%n", relationshipId, person);
				else
					out.printf("      %s --> %s%n", person, relationshipId);
				out.printf("      %s([%s])%n", person, name);

				if (gn.hasImage()) {
					out.printf("      %s@{ img: \"%s\", label: \"%s\", h: 100, constraint: \"on\" }%n", gn.getId(), gn.getImage(), gn.getName());
				}
				out.printf("      click %s \"%s\" _top%n", person, gn.getUrl());
			}
		}
		out.println(""
				+ "   </pre>\n"
				+ "   <hr/>\n"
				+ "</div>\n"
				+ "<script type=\"module\">\n"
				+ "   import mermaid from '" + HistoryFileProcessor.MERMAID_CDN + "';\n"
				+ "   mermaid.initialize({ startOnLoad: true });\n"
				+ "</script>");
	}

	private String cleanName(String name) {
		return name.replace("(", "").replace(")", "");
	}

}
