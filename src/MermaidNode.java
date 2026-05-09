import java.io.File;

/**
 * A {@link GraphNode} that serializes itself as Mermaid.js flowchart syntax.
 *
 * <p>The {@link #toString()} output includes the node label, an optional
 * clickable hyperlink with a blue border, and an optional portrait image
 * positioned below the label.
 */
public class MermaidNode extends GraphNode {

//	private static String PATH ;
//
//	public static void setPath(String path) {
//		PATH=path;
//	}
//
	/**
	 * Constructs a Mermaid node for the given person name.
	 *
	 * @param name display name as it appears in the CSV
	 */
	public MermaidNode(String name) {
		super(name);
	}

	/**
	 * Returns the Mermaid.js syntax for this node, including the label definition,
	 * an optional {@code click} directive with a blue stroke style if a URL exists,
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
		if(hasUrl()) {
			sb.append("\n\tclick ");
			sb.append(id);
			sb.append(" \"");
			sb.append(url);
			sb.append("\" _blank");
			sb.append("\n\tstyle ");
			sb.append(id);
			sb.append(" stroke:blue,stroke-width:4px");
		}
		
//		String shortName =  id.replace('_', ' ');
		File file = new File(PATH+"Images\\"+id+".jpg");
		if(file.exists()) {
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
