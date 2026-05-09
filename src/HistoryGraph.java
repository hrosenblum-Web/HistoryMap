import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.opencsv.exceptions.CsvException;

/**
 * Pipeline stage 2: generates the main Mermaid.js relationship graph.
 *
 * <p>Reads the cleaned {@code History.csv}, builds node and edge definitions
 * using {@link MermaidReader}, and writes a self-contained {@code index.html}
 * via {@link MermaidWriter}. The result is a top-down flowchart of the full
 * martial arts lineage.
 */
public class HistoryGraph {
	private static boolean DEBUG = false;

	/**
	 * Entry point for the graph generation stage. Writes {@code index.html} to
	 * the configured path. When {@code DEBUG} is {@code true}, output goes to
	 * stdout instead of the file.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		String path ="C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
		try {
			MermaidNode.setPath(path);
			RelationshipReader rr = new MermaidReader(path+"History.csv");
			PrintStream out;
			if(DEBUG)
				out = System.out;
			else {
				File file = new File(path+"index.html");
				out = new PrintStream(file);
			}
			GraphWriter gw = new  MermaidWriter(out);

			List<GraphNode> names = rr.getNodes();
			gw.writeNames(names);


			List<String> relationships = rr.getRelationships();
			gw.writeRelationships(relationships);

			gw.close();
			if(!DEBUG) {
			out.close();
			System.out.println("file "+path+"index.html created");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
