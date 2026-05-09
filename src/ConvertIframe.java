import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Pipeline stage 5: inlines {@code <iframe>} content into root HTML pages.
 *
 * <p>Scans every {@code .html} file in the configured directory. For any file
 * that contains an {@code <iframe src="...">} tag, the tag is replaced with the
 * full content of the referenced file. This makes each biography page
 * self-contained so the relationship chart renders without a separate iframe
 * request.
 */
public class ConvertIframe {
	private static final boolean DEBUG = false;

	/**
	 * Entry point for the iframe-inlining stage. Processes all {@code .html}
	 * files in the configured directory and reports how many were modified.
	 *
	 * @param args optional: args[0] is the base path (defaults to hardcoded path)
	 */
	public static void main(String[] args) {
		String path = args.length > 0 ? args[0] : "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
		File dir = new File(path);
		int convertCounter = 0;
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null) {
			System.err.printf("ConvertIframe.main: %s directory is empty%n", dir.getAbsolutePath());
			return;
		}
		for (File mainFile : dirFiles) {
			String name = mainFile.getAbsolutePath();
			if (!name.endsWith(".html")) continue;

			try {
				List<String> lines = new ArrayList<>();
				boolean changed = false;

				try (Scanner data = new Scanner(mainFile)) {
					while (data.hasNextLine()) {
						String line = data.nextLine();
						if (line.contains("<iframe")) {
							changed = true;
							// Extract the src attribute value with simple string indexing.
							// This works because CreateTemplates always writes the iframe on
							// a single line with src= as the first attribute.
							if (!line.contains("</iframe>"))
								throw new RuntimeException("Multi-line <iframe> not supported in " + name
										+ " — CreateTemplates must keep the tag on one line");
							int start = line.indexOf("src=\"") + 5;
							int end = line.indexOf("\"", start);
							String location = path + line.substring(start, end);
							try (Scanner relFile = new Scanner(new File(location))) {
								while (relFile.hasNextLine())
									lines.add(relFile.nextLine());
							}
						} else {
							lines.add(line);
						}
					}
				}

				if (!changed)
					continue;
				convertCounter++;
				try (PrintStream ps = new PrintStream(mainFile)) {
					for (String line : lines) {
						ps.println(line);
					}
				}
				if (DEBUG) System.out.println(name + " updated");
			} catch (FileNotFoundException e) {
				throw new RuntimeException("File not found during iframe conversion: " + e.getMessage(), e);
			}
		}
		System.out.println(convertCounter + " html files converted");
	}
}
