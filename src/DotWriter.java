import java.util.List;

public class DotWriter implements GraphWriter {

	public DotWriter() {
		super();
		System.out.println("digraph history {");
	}

	@Override
	public void writeNames(List<GraphNode> names) {
		System.out.println("\t// Name section");
		for(GraphNode gn:names)
			System.out.println(gn);
	}

	@Override
	public void writeRelationships(List<String> relationships) {
		System.out.println("\t// Relationship section");
		for(String rel : relationships)
			System.out.println("\t"+rel);
	}

	@Override
	public void close() {
		System.out.println("}");
	}

}
