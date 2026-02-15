import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public abstract class RelationshipReader implements HistoryFileProcessor {

	private CSVReader fileReader;
	protected Map<String,GraphNode> nodes = new HashMap<>();
	protected List<String> relationships = new ArrayList<>();


	public RelationshipReader(String fileName) throws IOException, CsvException {

		GraphNode node1,node2;
		fileReader = new CSVReaderBuilder(new FileReader(fileName)).build();
		fileReader.readNext();  //ignore header

		for(String[] row : fileReader.readAll()) {
			if(row.length<3)
				continue;
			node1 = createNode(row[SENIOR_PERSON]); // add default URl
			nodes.putIfAbsent(node1.getId(), node1);
			
			if(row[JUNIOR_PERSON].isBlank()) // if there is only one person
				continue;
			
			node2 = createNode(row[JUNIOR_PERSON]);
			nodes.putIfAbsent(node2.getId(), node2);
			
			createRelationship(node1.getId(), node2.getId(), row[RELATIONSHIP]);

		}
	}
	
//	protected abstract GraphNode createNode(String name, String url);
	protected abstract GraphNode createNode(String name);
	protected abstract void createRelationship(String id1, String id2, String relationship);

	public List<GraphNode> getNodes() {
		// TODO Auto-generated method stub
		return nodes.values().stream().toList();
	}

	public List<String> getRelationships() {
		return relationships;
	}

}
