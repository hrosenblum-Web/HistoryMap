import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public class MermaidReaderTest {

	private MermaidReader readerFor(Path dir, String... rows) throws Exception {
		Path csv = dir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			for (String row : rows) pw.println(row);
		}
		GraphNode.setPath(dir.toString() + "\\");
		MermaidReader mr = new MermaidReader(csv.toString());
		mr.load();
		return mr;
	}

	@Test
	void senseiProducesSolidArrow(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Ueshiba,Tohei,sensei,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("-->"), "sensei should use solid arrow (-->)");
	}

	@Test
	void familyProducesThickArrow(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Parent,Child,family,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("==>"), "family should use thick arrow (==>)");
	}

	@Test
	void partnerProducesBidirectionalArrow(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Alice,Bob,partner,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("<-.->"), "partner should use bidirectional dashed arrow (<-.->)");
	}

	@Test
	void customRelationshipProducesDashedLabelledArrow(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "A,B,trained by,").getRelationships();
		assertEquals(1, rels.size());
		String rel = rels.get(0);
		assertTrue(rel.contains("-.") && rel.contains(".->"), "custom type should use dashed arrow");
		assertTrue(rel.contains("trained by"), "custom type should embed the label");
	}

	@Test
	void emptyRelationshipProducesNoEdge(@TempDir Path tempDir) throws Exception {
		assertTrue(readerFor(tempDir, "Ueshiba,Tohei,,").getRelationships().isEmpty(),
				"blank relationship should add no edge");
	}
}
