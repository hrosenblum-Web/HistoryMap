import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.exceptions.CsvException;

public class SimpleReader extends RelationshipReader {
	private static Map<String,Map<String,List<String>>> relationshipMap = new HashMap<>();
	private static String PATH ;

	public static void setPath(String path) {
		PATH=path+"Relationships\\";
	}

	public SimpleReader(String fileName) throws IOException, CsvException {
		super(fileName);
	}

	@Override
	protected GraphNode createNode(String name, String url) {
		return new GraphNode(name,url);
	}

	@Override
	protected void createRelationship(String id1, String id2, String relationship) {
		Map<String,List<String>> relationships;
		List<String> ids;
		relationship = relationship.toUpperCase().charAt(0)+relationship.substring(1);

		if(relationship.equals("Trained"))
			relationship+=" by";

		if(relationship.equals("Maybe"))
			relationship+=" taught by";

		relationships = relationshipMap.getOrDefault(id2, new HashMap<>());
		ids = relationships.getOrDefault(relationship,new ArrayList<>());
		ids.add(id1);
		relationships.put(relationship, ids);
		relationshipMap.put(id2, relationships);

		if(relationship.equals("Sensei"))
			relationship="Deshi";

		if(relationship.equals("Maybe taught by"))
			relationship="Maybe taught";

		if(relationship.equals("Trained by"))
			relationship="Trained";

		relationships = relationshipMap.getOrDefault(id1, new HashMap<>());
		ids = relationships.getOrDefault(relationship,new ArrayList<>());
		ids.add(id2);
		relationships.put(relationship, ids);
		relationshipMap.put(id1, relationships);
	}

	public void print() {
		Set<String> personIds=relationshipMap.keySet();
		System.out.println("<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "\t<title>Relationship summary</title>\n"
				+ "</head>\n"
				+ "<body>\n");
		for(String personId:personIds) {
			System.out.println("\t<h2>"+personId+"</h2>");
			printData(personId,System.out);
		}
		System.out.println("</body>\n"
				+ "</html>");
	}

	public void save() {
		Set<String> personIds=relationshipMap.keySet();
		for(String personId:personIds) {
			try {
				PrintStream out=new PrintStream(new File(PATH+personId+".html"));
				out.println("<!DOCTYPE html>\n"
						+ "<html>\n"
						+ "<head>\n"
						+ "\t<title>"+personId+" Relationships</title>\n"
						+ "</head>\n"
						+ "<body>\n");
				printData(personId,out);
				out.println("</body>\n"
						+ "</html>");
				out.close();
				System.out.println(PATH+personId+".html created");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void printData(String personId,PrintStream out) {
		Map<String, List<String>> relationships=relationshipMap.get(personId);
		for(String relationship : relationships.keySet()) {
			out.println("\t<h3>"+relationship+"</h3>");
			out.println("\t<ul>");
			for(String person:relationships.get(relationship)) {
				GraphNode gn = nodes.get(person);
				String name = gn.getName().replaceAll("\n", " ");
				out.print("\t\t<li>");
				if(gn.hasUrl()) {
					String url;
					if(gn.getUrl().startsWith("http"))
						url=gn.getUrl();
					else
						url = "../"+gn.getShortUrl();
					out.print("<a href=\""+url+"\" target=\"_parent\">"+name+"</a>");
				} else
					out.print(name);
				out.println("</li>");
			}
			out.println("\t</ul>");
		}
	}

}


