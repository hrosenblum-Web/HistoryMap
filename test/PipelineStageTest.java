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

		HistoryGraph.main(new String[]{tempDir.toString() + "\\"});

		Path indexHtml = tempDir.resolve("index.html");
		assertTrue(Files.exists(indexHtml), "index.html should be created");
		String content = Files.readString(indexHtml);
		assertTrue(content.contains("mermaid"), "index.html should load Mermaid");
		assertTrue(content.contains("Ueshiba"), "index.html should reference Ueshiba");
		assertTrue(content.contains("Tohei"), "index.html should reference Tohei");
		assertTrue(content.contains("-->"), "sensei relationship should produce a solid arrow");
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
}
