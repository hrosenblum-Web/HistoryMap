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

/**
 * Pipeline stage 1: normalizes and sorts {@code History.csv}.
 *
 * <p>Reads the CSV, trims whitespace, quotes multi-line names, sorts rows
 * alphabetically by the senior person's name, and writes the result back to
 * the same file. Also warns (to stderr) about any names that appear in both
 * "First Last" and "Last First" order, which likely indicates a data entry
 * error.
 */
public class HistoryClean {
	private static final boolean DEBUG = false;

	/**
	 * Entry point for the clean stage. Reads, sorts, and overwrites
	 * {@code History.csv}. When {@code DEBUG} is {@code true}, output goes to
	 * stdout instead of the file.
	 *
	 * @param args optional: args[0] is the base path (defaults to hardcoded path)
	 */
	public static void main(String[] args) {
		String path = args.length > 0 ? args[0] : HistoryFileProcessor.DEFAULT_PATH;
		String fileName = path + "History.csv";

		Set<String> transposition = new HashSet<>();
		String header = null;
		List<String> historyEntries = new ArrayList<>();

		try (CSVReader fileReader = new CSVReaderBuilder(new FileReader(fileName)).build()) {
			header = String.join(",", fileReader.readNext());
			StringBuilder line;
			String column0;

			for (String[] row : fileReader.readAll()) {
				column0 = row[0].trim();
				if (column0.isEmpty())
					continue;
				line = new StringBuilder(4);
				line.append(csvQuote(column0));
				for (int i = 1; i < row.length; i++) {
					line.append(",");
					line.append(csvQuote(row[i].trim()));
				}
				historyEntries.add(line.toString());

				// Detect likely first/last name transpositions: if both "John Smith" and
				// "Smith John" appear in the data, one is almost certainly a typo.
				// add() returns false when the name was already in the set, meaning
				// we've seen this exact string before and don't need to check again.
				checkTransposition(column0, transposition);

				// Run the same transposition check on the junior person (column 1).
				if (row.length < 2) continue;
				checkTransposition(row[1].trim(), transposition);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read " + fileName, e);
		} catch (CsvException e) {
			throw new RuntimeException("Failed to parse " + fileName, e);
		}

		historyEntries.sort((s1, s2) -> clean(s1).compareTo(clean(s2)));

		if (DEBUG) {
			System.out.println(header);
			for (String entry : historyEntries)
				System.out.println(entry);
		} else {
			try (PrintStream out = new PrintStream(new File(fileName))) {
				out.println(header);
				for (String entry : historyEntries)
					out.println(entry);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Failed to write " + fileName, e);
			}
			System.out.println(fileName + " saved");
		}
	}

	/**
	 * Adds {@code name} to {@code seen} and, if it is new and contains a space,
	 * checks whether the first/last name swap is already in the set. Prints a
	 * warning to stderr if a likely transposition is found.
	 *
	 * <p>Extracted so the same logic applies to both the senior and junior columns
	 * without the old {@code continue} escaping past the junior-column check.
	 */
	private static void checkTransposition(String name, java.util.Set<String> seen) {
		if (!seen.add(name)) return; // already checked this name
		int space = name.indexOf(' ');
		if (space == -1) return;
		String swapped = name.substring(space + 1) + " " + name.substring(0, space);
		if (seen.contains(swapped))
			System.err.println("Possible transposition " + name + " and " + swapped);
	}

	/**
	 * Strips a leading quote character from a CSV value so that quoted multi-line
	 * names sort correctly alongside unquoted names.
	 *
	 * @param value raw CSV field value
	 * @return value without the leading {@code "} character, if present
	 */
	static String clean(String value) {
		if (value.isEmpty()) return value;
		if (value.charAt(0) == '\"')
			return value.substring(1);
		else
			return value;
	}

	private static String csvQuote(String value) {
		if (value.contains("\"") || value.contains(",") || value.contains("\n"))
			return "\"" + value.replace("\"", "\"\"") + "\"";
		return value;
	}

}
