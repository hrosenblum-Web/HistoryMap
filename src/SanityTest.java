import java.io.File;
import java.util.regex.Pattern;

/**
 * Validates that every filename in the website directory contains only
 * word characters ({@code [A-Za-z0-9_]}) and dots.
 *
 * <p>Filenames with spaces, apostrophes, or other special characters can cause
 * broken links in the generated HTML. Run this before deploying to catch data
 * entry errors early.
 */
public class SanityTest {
	private static final Pattern HAS_INVALID = Pattern.compile(".*[^\\w.].*");
	private static final Pattern STRIP_VALID  = Pattern.compile("[\\w.]");

	/**
	 * Recursively checks all filenames under the given path and prints any
	 * offending paths to stderr. Prints "All good" if no problems are found.
	 *
	 * @param args optional: args[0] is the directory to check (defaults to hardcoded path)
	 */
	public static void main(String[] args) {
		String startDir = args.length > 0 ? args[0] : "C:\\Users\\user\\Desktop\\Demo\\Website\\";
		boolean errorFound = recursiveCheck(new File(startDir));
		if (!errorFound)
			System.out.println("All good");
	}

	/**
	 * Recursively validates filenames in the given directory.
	 * Directories are traversed depth-first; files with non-word characters in
	 * their names are flagged to stderr.
	 *
	 * @param dir directory to check
	 * @return {@code true} if any invalid filename was found
	 */
	public static boolean recursiveCheck(File dir) {
		boolean errorFound = false;
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null) {
			System.err.printf("SanityTest.recursiveCheck: %s is empty directory%n", dir.getAbsolutePath());
			return false;
		}
		for (File file : dirFiles) {
			if (file.isDirectory()) {
				errorFound |= recursiveCheck(file);
			} else {
				String fileName = file.getName();
				if (HAS_INVALID.matcher(fileName).matches()) {
					String badChars = STRIP_VALID.matcher(fileName).replaceAll("");
					System.err.println(file.getAbsolutePath() + " contains invalid characters: [" + badChars + "]");
					errorFound = true;
				}
			}
		}
		return errorFound;
	}

}
