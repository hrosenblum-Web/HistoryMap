import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class HistoryClean {
	private static final boolean DEBUG=false;
	private static final String fileName = "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\History.csv";

	public static void main(String[] args) {
		Set<String> transposition = new HashSet<>();
		String header=null;

		List<String> historyEntries = new ArrayList<>();
		try (CSVReader fileReader = new CSVReaderBuilder(new FileReader(fileName)).build()) {
			header=String.join(",",fileReader.readNext());
			StringBuffer line;
			String column0;

			for(String[] row : fileReader.readAll()) {
				column0 = row[0].trim();
				if(column0.isEmpty())
					continue;
				line = new StringBuffer(4);
				if(column0.contains("\n")) {
					line.append("\"");
					line.append(column0);
					line.append("\"");
				} else {
					line.append(column0);
				}
				for(int i=1;i<row.length;i++) {
					line.append(",");						
					line.append(row[i].trim());
				}
				historyEntries.add(line.toString());

				// check for transposition of first and last name
				if(transposition.add(column0)) {
					int space=column0.indexOf(' ');
					if(space==-1)
						continue;
					String swapped = column0.substring(space).trim() + " "+ column0.substring(0,space).trim();
					if(transposition.contains(swapped))
						System.err.println("Possible transposition "+column0+" and "+swapped);
				}
				column0 = row[1].trim();
				if(transposition.add(column0)) {
					int space=column0.indexOf(' ');
					if(space==-1)
						continue;
					String swapped = column0.substring(space).trim() + " "+ column0.substring(0,space).trim();
					if(transposition.contains(swapped))
						System.err.println("Possible transposition "+column0+" and "+swapped);
				}

			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (CsvException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		historyEntries.sort((s1,s2) -> clean(s1).compareTo(clean(s2)));

		PrintStream out;
		if(DEBUG) {
			out=System.out;
		} else {
			try {
				out = new PrintStream(new File(fileName));
			} catch (FileNotFoundException e) {
				System.out.println("Unable to open file: "+fileName);
				out=System.out;
			}
		}
		out.println(header);
		for(String entry:historyEntries)
			out.println(entry);
		if(!DEBUG) {
			out.close();
			System.out.println(fileName+" saved");
		}


	}

	static String clean(String value) {
		if(value.charAt(0)=='\"')
			return value.substring(1, value.length());
		else
			return value;
	}


}
