import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;

public class HistoryCleanTest {

	@Test
	void unquotedStringUnchanged() {
		assertEquals("John Smith", HistoryClean.clean("John Smith"));
	}

	@Test
	void leadingQuoteStripped() {
		// Multi-line names are wrapped in quotes during normalization;
		// clean() strips the leading quote so they sort alongside unquoted names.
		assertEquals("John Smith", HistoryClean.clean("\"John Smith"));
	}

	@Test
	void seniorPersonTranspositionWarned(@TempDir Path tempDir) throws Exception {
		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("John Smith,Alice,sensei,");
			pw.println("Smith John,Bob,sensei,");
		}

		String stderr = captureStderr(() ->
			HistoryClean.main(new String[]{tempDir.toString() + "\\"}));

		assertTrue(stderr.contains("transposition"),
				"Should warn about 'John Smith' / 'Smith John' transposition");
	}

	@Test
	void juniorPersonTranspositionWarned(@TempDir Path tempDir) throws Exception {
		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("Sensei,John Smith,sensei,");
			pw.println("Sensei2,Smith John,sensei,");
		}

		String stderr = captureStderr(() ->
			HistoryClean.main(new String[]{tempDir.toString() + "\\"}));

		assertTrue(stderr.contains("transposition"),
				"Should warn about junior person 'John Smith' / 'Smith John' transposition");
	}

	@Test
	void nonTransposedNamesProduceNoWarning(@TempDir Path tempDir) throws Exception {
		Path csv = tempDir.resolve("History.csv");
		try (PrintWriter pw = new PrintWriter(csv.toFile())) {
			pw.println("SeniorPerson,JuniorPerson,Relationship,SeniorUrl");
			pw.println("John Smith,Alice Brown,sensei,");
		}

		String stderr = captureStderr(() ->
			HistoryClean.main(new String[]{tempDir.toString() + "\\"}));

		assertFalse(stderr.contains("transposition"),
				"No transposition warning should be emitted for distinct names");
	}

	private static String captureStderr(Runnable action) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		PrintStream original = System.err;
		System.setErr(new PrintStream(buf));
		try {
			action.run();
		} finally {
			System.setErr(original);
		}
		return buf.toString();
	}
}
