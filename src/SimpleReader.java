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
		GraphNode.setPath(path);
		File file = new File(path+"Relationships");
		if(!file.exists()) {
			if(file.mkdirs())
				System.err.println("CREATE "+file.getAbsolutePath());
			else
				System.err.println("Unable to create "+file.getAbsolutePath());

		}
	}

	public SimpleReader(String fileName) throws IOException, CsvException {
		super(fileName);
	}

	@Override
	protected GraphNode createNode(String name) {
		return new GraphNode(name);
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

		if(relationship.equals("Family"))
			relationship="Earlier generation";

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

		if(relationship.equals("Earlier generation"))
			relationship="Later generation";

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
			printTextData(personId,System.out);
		}
		System.out.println("</body>\n"
				+ "</html>");
	}

	public void save() {
		Set<String> personIds=relationshipMap.keySet();
		for(String personId:personIds) {
			try {
				PrintStream out=new PrintStream(new File(PATH+personId+".html"));
				printChartData(personId,out);
				out.close();
				System.out.println(PATH+personId+".html created");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void printTextData(String personId,PrintStream out) {
		out.println("<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "\t<title>"+personId+" Relationships</title>\n"
				+ "</head>\n"
				+ "<body>\n");
		Map<String, List<String>> relationships=relationshipMap.get(personId);
		for(String relationship : relationships.keySet()) {
			out.println("\t<h3>"+relationship+"</h3>");
			out.println("\t<ul>");
			for(String person:relationships.get(relationship)) {
				GraphNode gn = nodes.get(person);
				String name = gn.getName().replaceAll("\n", " ");
				out.print("\t\t<li>");
				if(gn.hasUrl()) {
					out.print("<a href=\""+gn.getUrl()+"\" target=\"_parent\">"+name+"</a>");
				} else
					out.print(name);
				out.println("</li>");
			}
			out.println("\t</ul>");
		}
		out.println("</body>\n"
				+ "</html>");
	}

	private void printChartData(String personId,PrintStream out) {
		out.println(""
				//				+ "<div style=\"border:1px solid;width:1800px;overflow:auto;\">\n"
				+ "<div>\n"
				+ "  <hr/>\n"
				+ "  <h3>Relationship Chart</h3>\n"
				+"   <pre class=\"mermaid\">\n"
				+"      %%{init: {\"flowchart\": {\"htmlLabels\": false}} }%%\n"
				+"      flowchart LR\n");

		Map<String, List<String>> relationships=relationshipMap.get(personId);
		GraphNode gn = nodes.get(personId);
		out.printf("      %s((%s))%n",personId,cleanName(gn.getName()));
		if(gn.hasImage()) {
			out.printf("%s@{ img: \"%s\", label: \"%s\", h: 100, constraint: \"on\" }%n",gn.getId(),gn.getImage(),gn.getName());				}
		boolean rightArrow;
		for(String relationship : relationships.keySet()) {
			out.printf("%n      %%%% %s relationships%n", relationship);
			if(relationship.equalsIgnoreCase("sensei") ||
					relationship.equalsIgnoreCase("Maybe taught by") ||
					relationship.equalsIgnoreCase("Trained by") ||
					relationship.equalsIgnoreCase("Earlier generation"))
				rightArrow=false;
			else 
				rightArrow=true;
			String relationshipId=relationship.replace(" ", "_");
			if(!relationshipId.equals(relationship))
				out.printf("      %s[%s]%n",relationshipId,relationship);
			if(rightArrow)
				out.printf("      %s --> %s%n",personId,relationshipId);
			else
				out.printf("      %s --> %s%n",relationshipId,personId);
			for(String person:relationships.get(relationship)) {
				gn = nodes.get(person);
				String name = cleanName(gn.getName());
				if(rightArrow)
					out.printf("      %s --> %s%n",relationshipId,person);
				else
					out.printf("      %s --> %s%n",person,relationshipId);				
				out.printf("      %s([%s])%n",person,name);

				if(gn.hasUrl()) 
					out.printf("      click %s \"%s\" _top%n",person,gn.getUrl());
				else
					out.print(name);
				
				if(gn.hasImage()) {
					out.printf("%s@{ img: \"%s\", label: \"%s\", h: 100, constraint: \"on\" }%n",gn.getId(),gn.getImage(),gn.getName());				}
			}
		}
		out.println(""
				+ "   </pre>\n"
				+ "   <hr/>\n"
				+ "</div>\n"
				+ "<script type=\"module\">\n"
				+ "   import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.esm.min.mjs';\n"
				+ "   mermaid.initialize({ startOnLoad: true });\n"
				+ "</script>");
	}

	private String cleanName(String name) {
		return name.replace("(", "").replace(")", "");
	}

}


