import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.opencsv.exceptions.CsvException;

public class HistoryGraph {
	private static boolean DEBUG=false;

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
