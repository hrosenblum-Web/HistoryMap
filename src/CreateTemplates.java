import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

/**
 * Pipeline stage 4: creates stub HTML pages for people who do not yet have one.
 *
 * <p>Iterates over the {@code Relationships/} directory to find all known
 * person IDs, then creates two kinds of missing pages:
 * <ul>
 *   <li>A root-level biography stub ({@code <id>.html}) with a portrait placeholder,
 *       optional timeline link, optional external/Wikipedia link, and an embedded
 *       iframe pointing to the person's relationship page.</li>
 *   <li>A {@code Relationships/<id>.html} stub with a "No known relationships"
 *       message, for people who appear in root pages but lack a relationship file.</li>
 * </ul>
 * Existing files are never overwritten.
 */
public class CreateTemplates implements HistoryFileProcessor {

	/**
	 * Entry point for the template creation stage. Reads {@code History.csv} to
	 * build name and external-URL lookup tables, then creates any missing stub pages.
	 *
	 * @param args optional: args[0] is the base path (defaults to hardcoded path)
	 */
	public static void main(String[] args) {
		String rootPath = args.length > 0 ? args[0] : "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
		String relationshipPath = rootPath + "Relationships\\";
		String timelinePath = rootPath + "timeline\\";

		GraphNode.setPath(rootPath);

		File relationshipFiles = new File(relationshipPath);

		// Find all the external web pages and build a name→id lookup from the CSV.
		Map<String, GraphNode> external = new HashMap<>();
		Map<String, String> nameLookup = new HashMap<>();

		try {
			CSVReader fileReader = new CSVReaderBuilder(new FileReader(rootPath + "History.csv")).build();
			fileReader.readNext(); // ignore header

			for (String[] columns : fileReader.readAll()) {
				GraphNode gn = new GraphNode(columns[SENIOR_PERSON]);
				nameLookup.putIfAbsent(gn.getId(), gn.getName());
				if (!columns[JUNIOR_PERSON].isEmpty()) {
					gn = new GraphNode(columns[JUNIOR_PERSON]);
					nameLookup.putIfAbsent(gn.getId(), gn.getName());
				}
				if (columns.length < 4 || columns[SENIOR_URL].isEmpty())
					continue;
				gn = new GraphNode(columns[SENIOR_PERSON], columns[SENIOR_URL]);
				external.put(columns[SENIOR_PERSON], gn);
			}
		} catch (IOException | CsvException e) {
			throw new RuntimeException("Failed to read History.csv", e);
		}

		boolean change = false;

		// Create missing root biography pages for everyone in the Relationships/ dir.
		System.out.println("Create template main pages");
		String[] dirStrings = relationshipFiles.list();
		if (dirStrings == null) {
			System.err.printf("CreateTemplates.main: %s directory is empty%n", relationshipFiles.getAbsolutePath());
			return;
		}
		for (String relationshipFileName : dirStrings) {
			if (!relationshipFileName.endsWith(".html"))
				continue;
			String id = relationshipFileName.substring(0, relationshipFileName.length() - 5);
			String name = nameLookup.get(id);
			if (name == null) {
				System.err.println("Skipping " + relationshipFileName + ": no matching entry in History.csv");
				continue;
			}
			File pageFile = new File(rootPath + relationshipFileName);
			if (writeMainStubPage(pageFile, name, id, external, timelinePath)) {
				change = true;
				System.out.println(relationshipFileName + " created");
			}
		}

		// Also create root biography pages for people with external URLs who may not
		// have appeared in the Relationships/ dir yet.
		System.out.println("-------------------------------------");
		for (String name : external.keySet()) {
			String id = external.get(name).getId();
			File pageFile = new File(rootPath + id + ".html");
			if (writeMainStubPage(pageFile, name, id, external, timelinePath)) {
				change = true;
				System.out.println(id + ".html created");
			}
		}

		if (!change)
			System.out.println("No changes");
		else
			change = false;

		// Create missing Relationships/ stubs for anyone who has a root page but no
		// relationship file.
		System.out.println("\nCreate template relationship pages");
		String[] dirRootStrings = relationshipFiles.list();
		if (dirRootStrings == null) {
			System.err.printf("CreateTemplates.main: %s directory is empty%n", relationshipFiles.getAbsolutePath());
			return;
		}
		for (String rootFileName : dirRootStrings) {
			if (!rootFileName.endsWith(".html"))
				continue;
			String name = rootFileName.substring(0, rootFileName.length() - 5);
			File pageFile = new File(relationshipPath + rootFileName);
			if (!pageFile.exists()) {
				change = true;
				try (PrintStream out = new PrintStream(pageFile)) {
					out.print("<!DOCTYPE html>\r\n"
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "    <title>" + name + " Relationships</title>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "    <h2>No known relationships</h2>\r\n"
							+ "</body>\r\n"
							+ "</html>\r\n");
					System.out.println(rootFileName + " created");
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Cannot create relationship stub " + pageFile, e);
				}
			}
		}
		if (!change)
			System.out.println("No changes");
	}

	/**
	 * Writes a biography stub page for a person if one does not already exist.
	 * The stub includes a portrait placeholder, optional timeline and external links,
	 * and an iframe embedding the person's relationship chart.
	 *
	 * @param pageFile      destination file to create
	 * @param name          display name used in the page title and heading
	 * @param id            sanitized person ID used to derive filenames
	 * @param external      map of display names to nodes with external URLs
	 * @param timelinePath  absolute path to the timeline directory
	 * @return {@code true} if the file was created, {@code false} if it already existed
	 */
	private static boolean writeMainStubPage(File pageFile, String name, String id,
	                                          Map<String, GraphNode> external,
	                                          String timelinePath) {
		if (pageFile.exists())
			return false;
		String imageFileName = id + ".jpg";
		String htmlFileName = id + ".html";
		try (PrintStream out = new PrintStream(pageFile)) {
			out.print("<!DOCTYPE html>\r\n"
					+ "<html>\r\n"
					+ "<head>\r\n"
					+ "    <title>" + name + "</title>\r\n"
					+ "    <script>\r\n"
					+ "    function goBack() {\r\n"
					+ "    window.history.back()\r\n"
					+ "    }\r\n"
					+ "    </script>\r\n"
					+ "</head>\r\n"
					+ "<body>\r\n"
					+ "    <h2>" + name + " - ART (YEAR) COUNTRY</h2>\r\n"
					+ "    <p><img alt=\"(no photo available)\"\r\n"
					+ "         height=\"250\"\r\n"
					+ "         src=\"Images/" + imageFileName + "\"></p>\r\n"
					+ "    <p>put information here</p>\r\n");

			File timelineFile = new File(timelinePath + htmlFileName);
			if (timelineFile.exists())
				out.print("    <p><a href=\"Timeline/" + htmlFileName + "\"\r\n"
						+ "       target=\"_self\">" + name + " Timeline</a></p>\r\n");

			if (external.containsKey(name)) {
				String page = external.get(name).getUrl();
				if (!page.contains("wikipedia"))
					out.print("    <p><a href=\"" + page + "\"\r\n"
							+ "       target=\"_blank\">" + name + " External link</a></p>\r\n");
				else
					out.print("    <p><a href=\"" + page + "\"\r\n"
							+ "       target=\"_blank\">" + name + " Wikipedia entry</a></p>\r\n");
			}

			out.print("    <iframe src=\"Relationships/" + htmlFileName + "\" width=\"1000\" height=\"500\" title=\"TEST\">This is a test</iframe> \r\n"
					+ "    <p><button onclick=\"goBack()\">Go Back</button></p>\r\n"
					+ "</body>\r\n"
					+ "</html>\r\n");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot create stub page " + pageFile, e);
		}
		return true;
	}
}
