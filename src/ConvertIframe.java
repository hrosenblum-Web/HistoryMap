import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConvertIframe {
	private static final boolean DEBUG=false;

	public static void main(String[] args) {
		String path ="C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
		File dir = new File(path);
		int convertCounter=0;
		for(File mainFile: dir.listFiles()) {
			String name=mainFile.getAbsolutePath();
			if(!name.endsWith(".html")) continue;

			try {
				// Read the file
				Scanner data = new Scanner(mainFile);
				List<String> lines = new ArrayList<>();
				boolean changed=false;
				while(data.hasNextLine()) {
					String line = data.nextLine();
					if(line.contains("<iframe")) {
						changed=true;
						int start = line.indexOf("src=\"")+5;
						int end = line.indexOf("\"",start);
						String location = path+line.substring(start, end);
						File rel=new File(location);
						Scanner relFile = new Scanner(rel);
						while(relFile.hasNextLine()) 
							lines.add(relFile.nextLine());	
						relFile.close();
						if(!line.contains("</iframe>")) System.err.println("Not inline");
					} else {
						lines.add(line);
					}
				}
				data.close();

				// Write the file
				if(!changed)
					continue;
				convertCounter++;
				PrintStream ps = new PrintStream(mainFile);
				for(String line : lines) {
					ps.println(line);
				}
				ps.close();
				if(DEBUG)System.out.println(name+" updated");
			} catch (FileNotFoundException e) {
				System.err.println("File not found: "+e.getMessage());
			}			
		}
		System.out.println(convertCounter+" html files converted");
	}
}
