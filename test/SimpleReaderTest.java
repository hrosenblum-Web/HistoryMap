import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleReaderTest {

	@Test
	void seniorBecomesJuniorsTeacher(@TempDir Path tempDir) throws Exception {
		GraphNode.setPath(tempDir.toString() + "\\");
		Files.createDirectory(tempDir.resolve("Images"));

		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("Ueshiba,Tohei,sensei,");
		}

		String base = tempDir.toString() + "\\";
		SimpleReader sr = new SimpleReader(csv.toString(), base);
		sr.load();
		sr.save();

		String tohei = Files.readString(tempDir.resolve("Relationships").resolve("Tohei.html"));
		String ueshiba = Files.readString(tempDir.resolve("Relationships").resolve("Ueshiba.html"));

		assertTrue(tohei.contains("Sensei"), "Tohei's page should list Ueshiba as Sensei");
		assertTrue(ueshiba.contains("Deshi"), "Ueshiba's page should list Tohei as Deshi");
	}

	@Test
	void emptyRelationshipIsIgnored(@TempDir Path tempDir) throws Exception {
		GraphNode.setPath(tempDir.toString() + "\\");
		Files.createDirectory(tempDir.resolve("Images"));

		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("Ueshiba,Tohei,,"); // blank relationship — must not crash
		}

		String base = tempDir.toString() + "\\";
		SimpleReader sr = new SimpleReader(csv.toString(), base);
		assertDoesNotThrow(sr::load); // StringIndexOutOfBoundsException would fail here
	}

	@Test
	void familyLabelFlipsToLaterGeneration(@TempDir Path tempDir) throws Exception {
		GraphNode.setPath(tempDir.toString() + "\\");
		Files.createDirectory(tempDir.resolve("Images"));

		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("Parent,Child,family,");
		}

		String base = tempDir.toString() + "\\";
		SimpleReader sr = new SimpleReader(csv.toString(), base);
		sr.load();
		sr.save();

		String parent = Files.readString(tempDir.resolve("Relationships").resolve("Parent.html"));
		String child  = Files.readString(tempDir.resolve("Relationships").resolve("Child.html"));

		assertTrue(parent.contains("Later generation"), "Parent's page should show 'Later generation'");
		assertTrue(child.contains("Earlier generation"), "Child's page should show 'Earlier generation'");
	}
}
