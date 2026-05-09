import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
}
