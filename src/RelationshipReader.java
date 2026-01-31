import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public abstract class RelationshipReader {

	private static final int PERSON1 = 0;
	private static final int PERSON2 = 1;
	private static final int RELATIONSHIP = 2;
	private static final int URL = 3;

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
//			node1 = createNode(row[PERSON1],(row.length<4)?"":row[URL]);
			node1 = createNode(row[PERSON1],row[PERSON1]+".html"); // add default URl
			nodes.putIfAbsent(node1.getId(), node1);
			
//			if(!nodes.containsKey(node1.getId())) { // if it is new, add it
//				if(!node1.hasUrl())
//					node1 = createNode(row[PERSON1],row[PERSON1]+".html"); // add default URl
//					
//				nodes.put(node1.getId(), node1);				
//			} else if(node1.hasUrl()) // if it has a URL update it
//				nodes.put(node1.getId(), node1);

			if(row[PERSON2].isBlank()) // if there is only one person
				continue;
			
			node2 = createNode(row[PERSON2],row[PERSON2]+".html");
			nodes.putIfAbsent(node2.getId(), node2);
			
			createRelationship(node1.getId(), node2.getId(), row[RELATIONSHIP]);

		}
	}
	
	protected abstract GraphNode createNode(String name, String url);
	protected abstract void createRelationship(String id1, String id2, String relationship);

	public List<GraphNode> getNodes() {
		// TODO Auto-generated method stub
		return nodes.values().stream().toList();
	}

	public List<String> getRelationships() {
		return relationships;
	}

}
