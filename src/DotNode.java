
public class DotNode extends GraphNode {

	public DotNode(String name, String url) {
		super(name, url);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("\t");
		sb.append(id);
		sb.append("[label=\"");
		sb.append(name);
		sb.append("\" shape=box]");
		return sb.toString();
	}
}
