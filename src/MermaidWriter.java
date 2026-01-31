import java.io.PrintStream;
import java.util.List;


public class MermaidWriter implements GraphWriter {
	private PrintStream out;

	
	public MermaidWriter(PrintStream out) {
		super();
		this.out=out;
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("  <head>");
		out.println("    <meta charset=\"utf-8\">");
		out.println("    <title>Martial Arts history map</title>");
		out.println("  </head>");
		out.println("  <body>");
		out.println("    <div style=\"border:1px solid;width:1800px;overflow:auto;\">");
		out.println("      <pre class=\"mermaid\">");
		out.println("\t%%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%");
		out.println("\tflowchart TD");
	}

	@Override
	public void writeNames(List<GraphNode> names) {
		out.println("\n\t%% Name section");
		for(GraphNode gn:names)
			out.println(gn);

	}

	@Override
	public void writeRelationships(List<String> relationships) {
		out.println("\n\t%% Relationship section");
		for(String rel : relationships)
			out.println("\t"+rel);
	}

	@Override
	public void close() {
		out.println("      </pre>");
		out.println("    </div>");
		out.println("    <script type=\"module\">");
		out.println("      import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';");
		out.println("      mermaid.initialize({ startOnLoad: true });");
		out.println("    </script>");
		out.println("  </body>");
		out.println("</html>		");
	}

}
