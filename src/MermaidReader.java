import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class MermaidReader extends RelationshipReader {

	public MermaidReader(String fileName) throws IOException, CsvException {
		super(fileName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createRelationship(String id1, String id2, String relationship) {
		switch(relationship)
		{
		case "family":
			relationships.add(id1+" ==> "+id2);
			break;
		case "sensei":
			relationships.add(id1+" --> "+id2);
			break;
		case "partner":
			relationships.add(id1+" <-.-> "+id2);
			break;
		case "": // no relationship
			break;
		default:
			relationships.add(id1+" -."+relationship+".-> "+id2);
			break;

		}
		// TODO Auto-generated method stub

	}

	@Override
	protected GraphNode createNode(String name, String url) {
		// TODO Auto-generated method stub
		return new MermaidNode(name,url);
	}

}
