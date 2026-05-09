import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GraphNodeTest {

	@BeforeAll
	static void setup() {
		// Provide a path so the image-existence check in the constructor doesn't NPE.
		GraphNode.setPath(".\\");
	}

	@Test
	void spacesBecomesUnderscores() {
		assertEquals("John_Smith", new GraphNode("John Smith").getId());
	}

	@Test
	void apostropheBecomesUnderscore() {
		assertEquals("O_Brien", new GraphNode("O'Brien").getId());
	}

	@Test
	void textAfterParenStripped() {
		assertEquals("John_Smith", new GraphNode("John Smith (Junior)").getId());
	}

	@Test
	void textAfterNewlineStripped() {
		assertEquals("John_Smith", new GraphNode("John Smith\nSome subtitle").getId());
	}

	@Test
	void macronTransliterated() {
		assertEquals("Jiro_Kano", new GraphNode("Jirō Kanō").getId());
	}

	@Test
	void acuteAccentTransliterated() {
		assertEquals("Pierre", new GraphNode("Piérre").getId());
	}

	@Test
	void multipleNewDiacriticsTransliterated() {
		// Tests diacritics added in the expanded remap
		assertEquals("Nino", new GraphNode("Niño").getId());
		assertEquals("Uwe", new GraphNode("Üwe").getId());
		assertEquals("Andre", new GraphNode("André").getId());
	}

	@Test
	void defaultUrlIsLocalPage() {
		GraphNode node = new GraphNode("John Smith");
		assertEquals("John_Smith.html", node.getUrl());
		assertFalse(node.hasExternalUrl());
	}

	@Test
	void explicitUrlIsExternal() {
		GraphNode node = new GraphNode("John Smith", "https://en.wikipedia.org/wiki/John_Smith");
		assertEquals("https://en.wikipedia.org/wiki/John_Smith", node.getUrl());
		assertTrue(node.hasExternalUrl());
	}

	@Test
	void emptyExplicitUrlFallsBackToDefault() {
		GraphNode node = new GraphNode("John Smith", "");
		assertEquals("John_Smith.html", node.getUrl());
		assertFalse(node.hasExternalUrl());
	}
}
