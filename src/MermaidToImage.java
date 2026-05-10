import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * Standalone example that converts a Mermaid diagram string to a PNG image
 * by calling the public {@code mermaid.ink} rendering API.
 *
 * <p>This class is not part of the main build pipeline. It demonstrates how to
 * base64-encode a Mermaid definition, send it to {@code https://mermaid.ink/img/},
 * and save the response as a local file ({@code diagram.png}).
 */
public class MermaidToImage {

    /**
     * Encodes a hardcoded Mermaid diagram as Base64, fetches a PNG from
     * {@code mermaid.ink}, and saves it to {@code diagram.png} in the working
     * directory.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // 1. Define the Mermaid diagram string
        String mermaidCode = "graph TD\n" +
                             "    A[Start] --> B{Error?}\n" +
                             "    B -- Yes --> C[Fix]\n" +
                             "    B -- No --> D[Enjoy]";

        // 2. Encode to Base64 (required by mermaid.ink)
        String base64Code = Base64.getUrlEncoder().encodeToString(mermaidCode.getBytes());
        String url = "https://mermaid.ink/img/" + base64Code;

        // 3. Send Request and Save File
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream body = response.body()) {
                if (response.statusCode() == 200) {
                    Path path = Paths.get("diagram.png");
                    Files.copy(body, path, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Image saved to: " + path.toAbsolutePath());
                } else {
                    System.err.println("Failed to get image. Status: " + response.statusCode());
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch diagram image", e);
        }
    }
}
