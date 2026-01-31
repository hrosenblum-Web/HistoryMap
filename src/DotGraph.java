import java.io.IOException;
import java.util.List;

import com.opencsv.exceptions.CsvException;

public class DotGraph {

	public static void main(String[] args) {
		String path ="C:\\Martial arts\\Historical\\Mindmap\\";
		try {
			RelationshipReader rr = new DotReader(path+"input.csv");
			GraphWriter gw = new  DotWriter();
			
			List<GraphNode> names = rr.getNodes();
			gw.writeNames(names);
			
			
			List<String> relationships = rr.getRelationships();
			gw.writeRelationships(relationships);
			
			gw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
