import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * {@link GraphWriter} implementation that fetches a pre-rendered SVG from
 * {@code kroki.io} and writes a self-contained HTML page with the SVG embedded
 * directly — no client-side JavaScript required.
 *
 * <p>Mermaid diagram text is buffered during {@link #writeNames} and
 * {@link #writeRelationships}; in {@link #close()} it is POSTed as plain text
 * to {@code kroki.io/mermaid/svg}, which returns the rendered SVG, and the
 * full HTML page is written to the output stream.
 */
public class SvgWriter implements GraphWriter {
	private final PrintStream out;
	private final StringBuilder diagram = new StringBuilder();

	/**
	 * Constructs the writer and initialises the Mermaid diagram header.
	 *
	 * @param out stream to write the HTML output to (typically a file stream)
	 */
	public SvgWriter(PrintStream out) {
		this.out = out;
		diagram.append("%%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%\n");
		diagram.append("flowchart TD\n");
	}

	/**
	 * Appends node definitions to the buffered diagram.
	 *
	 * @param names list of graph nodes to emit
	 */
	@Override
	public void writeNames(List<GraphNode> names) {
		diagram.append("\n\t%% Name section\n");
		for (GraphNode gn : names)
			diagram.append(gn).append("\n");
	}

	/**
	 * Appends edge definitions to the buffered diagram.
	 *
	 * @param relationships list of Mermaid edge strings
	 */
	@Override
	public void writeRelationships(List<String> relationships) {
		diagram.append("\n\t%% Relationship section\n");
		for (String rel : relationships)
			diagram.append("\t").append(rel).append("\n");
	}

	/**
	 * POSTs the buffered Mermaid diagram to {@code kroki.io/mermaid/svg}, embeds
	 * the returned SVG in an HTML page, and writes it to the output stream.
	 *
	 * @throws RuntimeException if the HTTP request fails
	 */
	@Override
	public void close() {
		String svg;
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://kroki.io/mermaid/svg"))
					.header("Content-Type", "text/plain")
					.POST(HttpRequest.BodyPublishers.ofString(diagram.toString(), StandardCharsets.UTF_8))
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200)
				throw new RuntimeException("kroki.io returned HTTP " + response.statusCode());
			svg = response.body();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while fetching SVG from kroki.io", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to fetch SVG from kroki.io", e);
		}
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("  <head>");
		out.println("    <meta charset=\"utf-8\">");
		out.println("    <title>Martial Arts history map</title>");
		out.println("  </head>");
		out.println("  <body>");
		out.println("    <div style=\"border:1px solid;width:1800px;overflow:auto;\">");
		out.println(svg);
		out.println("    </div>");
		out.println("  </body>");
		out.println("</html>");
	}

}
