import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class CreateTemplates implements HistoryFileProcessor{

	public static void main(String[] args) {
		String rootPath = "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
		String relationshipPath = rootPath+"Relationships\\";

		File relationshipFiles = new File(relationshipPath);

		String pageFileName;
		File pageFile;

		String timelinePath = rootPath+"timeline\\";
		File timelineFile;

		PrintStream out;
		//		String name;

		// Find all the external web pages
		Map<String,GraphNode> external = new HashMap<>();
		Map<String,String> nameLookup = new HashMap<>();
		//		File historyFile=new File(rootPath+"History.csv");

		try {
			CSVReader fileReader = new CSVReaderBuilder(new FileReader(rootPath+"History.csv")).build();
			fileReader.readNext();  //ignore header

			for(String[] columns : fileReader.readAll()) {
				GraphNode gn= new GraphNode(columns[SENIOR_PERSON]);
				nameLookup.putIfAbsent(gn.getId(), gn.getName());
				if(!columns[JUNIOR_PERSON].isEmpty()) {
					gn= new GraphNode(columns[JUNIOR_PERSON]);
					nameLookup.putIfAbsent(gn.getId(), gn.getName());
				}
				if(columns.length<4 || columns[SENIOR_URL].isEmpty())
					continue;
				gn= new GraphNode(columns[SENIOR_PERSON], columns[SENIOR_URL]);
				external.put(columns[SENIOR_PERSON], gn);
			}

		} catch (IOException | CsvException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		boolean change=false;

		// Create missing template main page for all relationships
		System.out.println("Create template main pages");
		for(String relationshipFileName:relationshipFiles.list()) {
			if(!relationshipFileName.endsWith(".html"))
				continue;
			pageFile = new File(rootPath+relationshipFileName);
			String id = relationshipFileName.substring(0,relationshipFileName.length()-5);
			String name = nameLookup.get(id);
			String imageFileName = id+".jpg";

			if(!pageFile.exists()) {
				change=true;
				try {
					out=new PrintStream(pageFile);
					out.print("<!DOCTYPE html>\r\n"
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "    <title>"+name+"</title>\r\n"
							+ "    <script>\r\n"
							+ "    function goBack() {\r\n"
							+ "    window.history.back()\r\n"
							+ "    }\r\n"
							+ "    </script>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "    <h2>"+name+" - ART (YEAR) COUNTRY</h2>\r\n"
							+ "    <p><img alt=\"(no photo available)\"\r\n"
							+ "         height=\"250\"\r\n"
							+ "         src=\"Images/"+imageFileName+"\"></p>\r\n"
							+ "    <p>put information here</p>\r\n");

					timelineFile = new File(timelinePath+relationshipFileName);
					if(timelineFile.exists())
						out.print("    <p><a href=\"Timeline/"+relationshipFileName+"\"\r\n"
								+ "       target=\"_self\">"+name+" Timeline</a></p>\r\n");

					if(external.containsKey(name)) {
						String page=external.get(name).getUrl();
						if(!page.contains("wikipedia"))
							out.print("    <p><a href=\""+page+"\"\r\n"
									+ "       target=\"_blank\">"+name+" External link</a></p>\r\n");
						else
							out.print("    <p><a href=\""+page+"\"\r\n"
									+ "       target=\"_blank\">"+name+" Wikipedia entry</a></p>\r\n");
					}

					out.print("    <iframe src=\"Relationships/"+relationshipFileName+"\" width=\"1000\" height=\"500\" title=\"TEST\">This is a test</iframe> \r\n"
							+ "    <p><button onclick=\"goBack()\">Go Back</button></p>\r\n"
							+ "</body>\r\n"
							+ "</html>\r\n");
					out.close();
					System.out.println(relationshipFileName+" created");

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("-------------------------------------");
		for(String name:external.keySet()) {
			pageFileName = external.get(name).getId()+".html";
			pageFile = new File(rootPath+pageFileName);
			String imageFileName = pageFileName.replace(".html", ".jpg");
			if(!pageFile.exists()) {
				change=true;
				try {
					out=new PrintStream(pageFile);
					out.print("<!DOCTYPE html>\r\n"
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "    <title>"+name+"</title>\r\n"
							+ "    <script>\r\n"
							+ "    function goBack() {\r\n"
							+ "    window.history.back()\r\n"
							+ "    }\r\n"
							+ "    </script>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "    <h2>"+name+" - ART (YEAR) COUNTRY</h2>\r\n"
							+ "    <p><img alt=\"(no photo available)\"\r\n"
							+ "         height=\"250\"\r\n"
							+ "         src=\"Images/"+imageFileName+"\"></p>\r\n"
							+ "    <p>put information here</p>\r\n");

					timelineFile = new File(timelinePath+pageFileName);
					if(timelineFile.exists())
						out.print("    <p><a href=\"Timeline/"+pageFileName+"\"\r\n"
								+ "       target=\"_self\">"+name+" Timeline</a></p>\r\n");

					if(external.containsKey(name)) {
						String page=external.get(name).getUrl();
						if(!page.contains("wikipedia"))
							out.print("    <p><a href=\""+page+"\"\r\n"
									+ "       target=\"_blank\">"+name+" External link</a></p>\r\n");
						else
							out.print("    <p><a href=\""+page+"\"\r\n"
									+ "       target=\"_blank\">"+name+" Wikipedia entry</a></p>\r\n");
					}

					out.print("    <iframe src=\"Relationships/"+pageFileName+"\" width=\"1000\" height=\"500\" title=\"TEST\">This is a test</iframe> \r\n"
							+ "    <p><button onclick=\"goBack()\">Go Back</button></p>\r\n"
							+ "</body>\r\n"
							+ "</html>\r\n");
					out.close();
					System.out.println(pageFileName+" created");

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if(!change) 
			System.out.println("No changes");
		else
			change=false;


		File rootFiles = new File(rootPath);
		// Create missing template relationship page for all root files
		System.out.println("\nCreate template relationship pages");
		for(String rootFileName:rootFiles.list()) {
			if(!rootFileName.endsWith(".html"))
				continue;
			//			pageFileName = rootFileName.replaceAll(" ", "_");
			pageFile = new File(relationshipPath+rootFileName); // the root and relationship file names are the same just in different directories
			String name = rootFileName.substring(0,rootFileName.length()-5);

			if(!pageFile.exists()) {
				change=true;
				try {
					out=new PrintStream(pageFile);
					out.print("<!DOCTYPE html>\r\n"
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "    <title>"+name+" Relationships</title>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "    <h2>No known relationships</h2>\r\n"
							+ "</body>\r\n"
							+ "</html>\r\n");
					out.close();
					System.out.println(rootFileName+" created");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(!change)
			System.out.println("No changes");


	}
}
