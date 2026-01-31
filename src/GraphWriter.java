import java.util.List;

public interface GraphWriter {

	public void writeNames(List<GraphNode> names);

	public void writeRelationships(List<String> relationships);

	public void close();

}