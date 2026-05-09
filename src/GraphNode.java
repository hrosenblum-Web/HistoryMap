import java.io.File;
import java.util.Map;

/**
 * Represents a person in the martial arts lineage graph.
 *
 * <p>Each node stores the person's display name, a URL-safe identifier derived
 * from the name, a link to their profile page, and whether a portrait image
 * exists on disk. The static {@code PATH} must be set once (via
 * {@link #setPath(String)}) before constructing any nodes so that image
 * presence can be checked.
 */
public class GraphNode {
	protected final String name;
	protected final String url;
	protected final String id;
	protected final boolean hasImage;

	/** Base filesystem path used to resolve the {@code Images/} directory. */
	protected static String PATH;

	/**
	 * Sets the base filesystem path for all nodes.
	 * Must be called before constructing any {@code GraphNode} instances.
	 *
	 * @param path absolute path ending with a backslash, e.g. {@code "C:\Website\"}
	 * @throws IllegalArgumentException if {@code path} is {@code null}
	 */
	public static void setPath(String path) {
		if (path == null) throw new IllegalArgumentException("PATH must not be null");
		PATH = path;
	}

	/**
	 * Characters replaced during name-to-ID conversion.
	 * Covers common diacritics found in Japanese, Korean, and European martial arts names.
	 */
	private final Map<Character, Character> remap = Map.ofEntries(
			Map.entry(' ',  '_'),
			Map.entry('\'', '_'),
			Map.entry('ō',  'o'), Map.entry('Ō',  'O'),
			Map.entry('ū',  'u'), Map.entry('Ū',  'U'),
			Map.entry('ā',  'a'), Map.entry('Ā',  'A'),
			Map.entry('ī',  'i'), Map.entry('Ī',  'I'),
			Map.entry('é',  'e'), Map.entry('è',  'e'), Map.entry('ê',  'e'),
			Map.entry('É',  'E'), Map.entry('È',  'E'), Map.entry('Ê',  'E'),
			Map.entry('á',  'a'), Map.entry('à',  'a'), Map.entry('â',  'a'), Map.entry('ä',  'a'),
			Map.entry('Á',  'A'), Map.entry('À',  'A'), Map.entry('Â',  'A'), Map.entry('Ä',  'A'),
			Map.entry('í',  'i'), Map.entry('Í',  'I'),
			Map.entry('ó',  'o'), Map.entry('ö',  'o'), Map.entry('Ó',  'O'), Map.entry('Ö',  'O'),
			Map.entry('ú',  'u'), Map.entry('ü',  'u'), Map.entry('Ú',  'U'), Map.entry('Ü',  'U'),
			Map.entry('ñ',  'n'), Map.entry('Ñ',  'N')
	);

	/**
	 * Converts a display name to a filesystem- and Mermaid-safe identifier.
	 * Strips text after a newline or opening parenthesis, trims whitespace,
	 * then applies character substitutions defined in {@link #remap}.
	 *
	 * @param userName raw display name from the CSV
	 * @return sanitized identifier string
	 */
	private String convertToId(String userName) {
		int index = userName.indexOf('\n');
		if (index > -1) {
			userName = userName.substring(0, index);
		}
		index = userName.indexOf('(');
		if (index > -1) {
			userName = userName.substring(0, index);
		}
		userName = userName.trim();

		StringBuilder idBuffer = new StringBuilder();
		char c;
		for (int i = 0; i < userName.length(); i++) {
			c = userName.charAt(i);
			idBuffer.append(remap.getOrDefault(c, c));
		}
		return idBuffer.toString();
	}

	/**
	 * Constructs a node whose URL defaults to {@code <id>.html} (the local biography page).
	 *
	 * @param name display name as it appears in the CSV
	 */
	public GraphNode(String name) {
		this.name = name;
		this.id = convertToId(name);
		this.url = id + ".html";
		if (PATH == null)
			throw new IllegalStateException("GraphNode.setPath() must be called before constructing nodes");
		hasImage = new File(PATH + "Images\\" + id + ".jpg").exists();
	}

	/**
	 * Constructs a node with an explicit external URL (e.g. a Wikipedia link).
	 *
	 * @param name display name as it appears in the CSV
	 * @param url  external URL overriding the default {@code <id>.html}
	 */
	public GraphNode(String name, String url) {
		this.name = name;
		this.id = convertToId(name);
		this.url = url.isEmpty() ? id + ".html" : url;
		if (PATH == null)
			throw new IllegalStateException("GraphNode.setPath() must be called before constructing nodes");
		hasImage = new File(PATH + "Images\\" + id + ".jpg").exists();
	}

	/**
	 * Returns the person's full display name.
	 *
	 * @return display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the URL for this person's profile page.
	 * Always non-empty: defaults to {@code <id>.html} if no external URL was provided.
	 *
	 * @return profile URL or external link
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the filesystem- and Mermaid-safe identifier derived from the name.
	 *
	 * @return sanitized ID string
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the relative path to the person's portrait image.
	 *
	 * @return path of the form {@code Images/<id>.jpg}
	 */
	public String getImage() {
		return "Images/" + id + ".jpg";
	}

	/**
	 * Returns {@code true} if this node has an external URL (i.e. not just the
	 * default local biography page {@code <id>.html}).
	 *
	 * @return whether an external URL was explicitly provided
	 */
	public boolean hasExternalUrl() {
		return !url.equals(id + ".html");
	}

	/**
	 * Returns {@code true} if a portrait image file exists on disk for this node.
	 *
	 * @return whether the image file exists
	 */
	public boolean hasImage() {
		return hasImage;
	}

}
