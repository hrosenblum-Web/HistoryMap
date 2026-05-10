import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * {@link GraphWriter} implementation that fetches a pre-rendered SVG from
 * {@code mermaid.ink} and writes a self-contained HTML page with the SVG
 * embedded directly — no client-side JavaScript required.
 *
 * <p>Mermaid diagram text is buffered during {@link #writeNames} and
 * {@link #writeRelationships}; in {@link #close()} it is zlib-compressed,
 * base64url-encoded, and sent to {@code mermaid.ink/svg/pako:} as a GET
 * request. This keeps URLs short enough to avoid HTTP 414 on large graphs.
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
	 * Zlib-compresses the buffered diagram, fetches the rendered SVG from
	 * {@code mermaid.ink/svg/pako:}, and writes the complete HTML page to the
	 * output stream.
	 *
	 * @throws RuntimeException if compression or the HTTP request fails
	 */
	@Override
	public void close() {
		String encoded;
		try {
			encoded = compress(diagram.toString());
		} catch (IOException e) {
			throw new RuntimeException("Failed to compress diagram", e);
		}
		String svg;
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(30))
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://mermaid.ink/svg/pako:" + encoded))
					.timeout(Duration.ofSeconds(60))
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200)
				throw new RuntimeException("mermaid.ink returned HTTP " + response.statusCode());
			svg = response.body();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while fetching SVG from mermaid.ink", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to fetch SVG from mermaid.ink", e);
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

	private static String compress(String text) throws IOException {
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
				dos.write(text.getBytes(StandardCharsets.UTF_8));
			}
			return java.util.Base64.getUrlEncoder().withoutPadding()
					.encodeToString(baos.toByteArray());
		} finally {
			deflater.end();
		}
	}
}
