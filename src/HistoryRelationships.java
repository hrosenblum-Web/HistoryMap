import java.io.IOException;

import com.opencsv.exceptions.CsvException;

/**
 * Pipeline stage 3: generates per-person HTML relationship pages.
 *
 * <p>Reads the cleaned {@code History.csv} using {@link SimpleReader}, which
 * builds a bidirectional relationship map, then writes one HTML file per person
 * into the {@code Relationships/} directory. Each file contains a Mermaid
 * mini-graph and a linked list of that person's connections.
 */
public class HistoryRelationships {
	private static boolean DEBUG = false;

	/**
	 * Entry point for the relationship page generation stage. When {@code DEBUG}
	 * is {@code true}, prints all relationships to stdout instead of saving files.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		String path ="C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
		SimpleReader.setPath(path);
		SimpleReader sr;
		try {
			sr = new SimpleReader(path+"History.csv");
			if(DEBUG)
				sr.print();
			else
				sr.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}


