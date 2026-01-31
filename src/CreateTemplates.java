import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

public class CreateTemplates {

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
		Map<String,String> external = new HashMap<>();
//		File historyFile=new File(rootPath+"History.csv");

		try {
			CSVReader fileReader = new CSVReaderBuilder(new FileReader(rootPath+"History.csv")).build();
			fileReader.readNext();  //ignore header

			for(String[] columns : fileReader.readAll()) {
				if(columns.length<4 || columns[3].isEmpty())
					continue;
				external.put(columns[0], columns[3]);
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
			pageFileName = relationshipFileName.replaceAll("_", " ");
			pageFile = new File(rootPath+pageFileName);
			String name = pageFileName.substring(0,pageFileName.length()-5);

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
							+ "         src=\"Images/"+name+".jpg\"></p>\r\n"
							+ "    <p>put information here</p>\r\n");

					timelineFile = new File(timelinePath+name+" Timeline.html");
					if(timelineFile.exists())
						out.print("    <p><a href=\"Timeline/"+name+"%20Timeline.html\"\r\n"
								+ "       target=\"_blank\">"+name+" Timeline</a></p>\r\n");

					if(external.containsKey(name)) {
						String page=external.get(name);
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
					System.out.println(pageFileName+" created");

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("-------------------------------------");
		for(String name:external.keySet()) {
			pageFileName = name+".html";
			pageFile = new File(rootPath+pageFileName);
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
							+ "         src=\"Images/"+name+".jpg\"></p>\r\n"
							+ "    <p>put information here</p>\r\n");

					timelineFile = new File(timelinePath+name+" Timeline.html");
					if(timelineFile.exists())
						out.print("    <p><a href=\"Timeline/"+name+"%20Timeline.html\"\r\n"
								+ "       target=\"_self\">"+name+" Timeline</a></p>\r\n");

					if(external.containsKey(name)) {
						String page=external.get(name);
						if(!page.contains("wikipedia"))
							out.print("    <p><a href=\""+page+"\"\r\n"
									+ "       target=\"_blank\">"+name+" External link</a></p>\r\n");
						else
							out.print("    <p><a href=\""+page+"\"\r\n"
									+ "       target=\"_blank\">"+name+" Wikipedia entry</a></p>\r\n");
					}

					out.print("    <iframe src=\"Relationships/"+pageFileName.replace(" ", "_")+"\" width=\"1000\" height=\"500\" title=\"TEST\">This is a test</iframe> \r\n"
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
			pageFileName = rootFileName.replaceAll(" ", "_");
			pageFile = new File(relationshipPath+pageFileName);
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
					System.out.println(pageFileName+" created");
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
