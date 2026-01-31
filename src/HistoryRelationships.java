import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class HistoryRelationships {
	private static boolean DEBUG=false;

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


