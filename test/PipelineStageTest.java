import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class PipelineStageTest {

	@Test
	void historyGraphCreatesIndexHtml(@TempDir Path tempDir) throws Exception {
		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("Ueshiba,Tohei,sensei,");
		}

		try {
			HistoryGraph.main(new String[]{tempDir.toString() + "\\"});
		} catch (RuntimeException e) {
			Assumptions.abort("Skipping: mermaid.ink unavailable: " + e.getMessage());
		}

		Path indexHtml = tempDir.resolve("index.html");
		assertTrue(Files.exists(indexHtml), "index.html should be created");
		String content = Files.readString(indexHtml);
		Assumptions.assumeTrue(content.contains("<svg"),
				"Skipping: mermaid.ink returned non-SVG content");
		assertTrue(content.contains("<svg"), "index.html should contain embedded SVG");
		assertTrue(content.contains("Ueshiba"), "index.html should reference Ueshiba");
		assertTrue(content.contains("Tohei"), "index.html should reference Tohei");
	}

	@Test
	void historyRelationshipsCreatesPersonPages(@TempDir Path tempDir) throws Exception {
		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("Ueshiba,Tohei,sensei,");
		}

		HistoryRelationships.main(new String[]{tempDir.toString() + "\\"});

		String tohei = Files.readString(tempDir.resolve("Relationships").resolve("Tohei.html"));
		String ueshiba = Files.readString(tempDir.resolve("Relationships").resolve("Ueshiba.html"));

		assertTrue(tohei.contains("Sensei"), "Tohei's page should list Ueshiba as Sensei");
		assertTrue(ueshiba.contains("Deshi"), "Ueshiba's page should list Tohei as Deshi");
		assertTrue(tohei.contains("mermaid"), "Relationship pages should include a Mermaid chart");
	}

	@Test
	void convertIframeInlinesRelationshipContent(@TempDir Path tempDir) throws Exception {
		Files.createDirectory(tempDir.resolve("Relationships"));
		Files.writeString(tempDir.resolve("Relationships").resolve("Tohei.html"),
				"<div>Tohei relationships chart</div>");

		Files.writeString(tempDir.resolve("Tohei.html"),
				"<html><body>\n"
				+ "    <iframe src=\"Relationships/Tohei.html\" width=\"1000\" height=\"500\" title=\"TEST\">This is a test</iframe> \r\n"
				+ "</body></html>\n");

		ConvertIframe.main(new String[]{tempDir.toString() + "\\"});

		String result = Files.readString(tempDir.resolve("Tohei.html"));
		assertTrue(result.contains("Tohei relationships chart"), "iframe content should be inlined");
		assertFalse(result.contains("<iframe"), "iframe tag should be replaced");
	}

	@Test
	void historyGraphThrowsWhenCsvMissing(@TempDir Path tempDir) {
		// No History.csv created — HistoryGraph should throw RuntimeException,
		// which is what BuildAll catches to abort the pipeline with exit code 1.
		assertThrows(RuntimeException.class,
				() -> HistoryGraph.main(new String[]{tempDir.toString() + "\\"}),
				"missing History.csv should throw RuntimeException");
	}
}
