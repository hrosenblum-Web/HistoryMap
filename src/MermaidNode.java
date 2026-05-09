/**
 * A {@link GraphNode} that serializes itself as Mermaid.js flowchart syntax.
 *
 * <p>The {@link #toString()} output includes the node label, an optional
 * clickable hyperlink with a blue border, and an optional portrait image
 * positioned below the label.
 */
public class MermaidNode extends GraphNode {

	/**
	 * Constructs a Mermaid node for the given person name, linking to the local
	 * biography page ({@code <id>.html}).
	 *
	 * @param name display name as it appears in the CSV
	 */
	public MermaidNode(String name) {
		super(name);
	}

	/**
	 * Constructs a Mermaid node for the given person name with an explicit URL.
	 *
	 * @param name display name as it appears in the CSV
	 * @param url  URL to navigate to when the node is clicked
	 */
	public MermaidNode(String name, String url) {
		super(name, url);
	}

	/**
	 * Returns the Mermaid.js syntax for this node, including the label definition,
	 * a {@code click} directive linking to the biography page with a blue stroke style,
	 * and an optional image annotation if a portrait file exists on disk.
	 *
	 * @return Mermaid node definition string
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("\t");
		sb.append(id);
		sb.append("[\"");
		sb.append(name);
		sb.append("\"]");

		// Mermaid click directive opens the URL in a new tab.
		sb.append("\n\tclick ");
		sb.append(id);
		sb.append(" \"");
		sb.append(url);
		sb.append("\" _blank");
		// Blue border visually distinguishes nodes that have a biography page.
		sb.append("\n\tstyle ");
		sb.append(id);
		sb.append(" stroke:blue,stroke-width:4px");

//		String shortName =  id.replace('_', ' ');
		if (hasImage()) {
			// @{ } is Mermaid's node metadata syntax for attaching an image below the label.
			sb.append("\n\t");
			sb.append(id);
			sb.append("@{ img: \"Images/");
			sb.append(id);
			sb.append(".jpg\", label: \"");
			sb.append(name);
			sb.append("\", pos: \"b\", h: 400, constraint: \"on\" }");
		}
		return sb.toString();
	}

}
