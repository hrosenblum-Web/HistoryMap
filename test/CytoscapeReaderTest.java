import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public class CytoscapeReaderTest {

	private CytoscapeReader readerFor(Path dir, String... rows) throws Exception {
		Path csv = dir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			for (String row : rows) pw.println(row);
		}
		GraphNode.setPath(dir.toString() + "\\");
		CytoscapeReader cr = new CytoscapeReader(csv.toString());
		cr.load();
		return cr;
	}

	@Test
	void senseiEdgeHasTypeSensei(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Ueshiba,Tohei,sensei,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("type:\"sensei\""), "sensei should produce type=sensei");
	}

	@Test
	void familyEdgeHasTypeFamily(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Parent,Child,family,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("type:\"family\""), "family should produce type=family");
	}

	@Test
	void partnerEdgeHasTypePartner(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Alice,Bob,partner,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("type:\"partner\""), "partner should produce type=partner");
	}

	@Test
	void customRelationshipPreservesLabel(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "A,B,trained by,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("type:\"trained by\""), "custom type should preserve the label");
	}

	@Test
	void relationshipTypeMatchingIsCaseInsensitive(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Ueshiba,Tohei,Sensei,").getRelationships();
		assertEquals(1, rels.size());
		assertTrue(rels.get(0).contains("type:\"sensei\""), "Sensei (capital S) should be lowercased to sensei");
	}

	@Test
	void emptyRelationshipProducesNoEdge(@TempDir Path tempDir) throws Exception {
		assertTrue(readerFor(tempDir, "Ueshiba,Tohei,,").getRelationships().isEmpty(),
				"blank relationship should add no edge");
	}

	@Test
	void edgeContainsSourceAndTarget(@TempDir Path tempDir) throws Exception {
		List<String> rels = readerFor(tempDir, "Ueshiba,Tohei,sensei,").getRelationships();
		String edge = rels.get(0);
		assertTrue(edge.contains("source:\"Ueshiba\""), "edge should contain source id");
		assertTrue(edge.contains("target:\"Tohei\""), "edge should contain target id");
	}
}
