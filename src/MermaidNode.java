import java.io.File;

public class MermaidNode extends GraphNode {

	private static String PATH ;

	public static void setPath(String path) {
		PATH=path;
	}

	public MermaidNode(String name, String url) {
		super(name, url);
		// TODO Auto-generated constructor stub
	}

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
		
		String shortName =  id.replace('_', ' ');
		File file = new File(PATH+"images\\"+shortName+".jpg");
		if(file.exists()) {
			sb.append("\n\t");
			sb.append(id);
			sb.append("@{ img: \"images/");
			sb.append(shortName);
			sb.append(".jpg\", label: \"");
			sb.append(name);
			sb.append("\", pos: \"b\", h: 400, constraint: \"on\" }");
		}
		return sb.toString();
	}

}
