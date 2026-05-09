import java.io.File;

/**
 * Validates that every filename in the website directory contains only
 * word characters ({@code [A-Za-z0-9_]}) and dots.
 *
 * <p>Filenames with spaces, apostrophes, or other special characters can cause
 * broken links in the generated HTML. Run this before deploying to catch data
 * entry errors early.
 */
public class SanityTest {
	private static final String startDir = "C:\\Users\\user\\Desktop\\Demo\\Website\\";
	private static final String NON_WORD_PATTERN = "[^\\w.]";
	private static final String SEARCH_PATTERN = ".*" + NON_WORD_PATTERN + ".*";
	private static boolean errorFound = false;

	/**
	 * Recursively checks all filenames under {@code startDir} and prints any
	 * offending paths to stderr. Prints "All good" if no problems are found.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		recursiveCheck(new File(startDir));
		if (!errorFound)
			System.out.println("All good");
	}

	/**
	 * Recursively validates filenames in the given directory.
	 * Directories are traversed depth-first; files with non-word characters in
	 * their names are flagged to stderr and set {@code errorFound}.
	 *
	 * @param dir directory to check
	 */
	public static void recursiveCheck(File dir) {
		File[] dirFiles = dir.listFiles();
		if(dirFiles == null) {
			System.err.printf("SanityTest.recursiveCheck: %s is empty directory%n",dir.getAbsolutePath());
			return;
		}
		for(File file : dirFiles) {
			if(file.isDirectory()) {
				recursiveCheck(file);
			} else {
				String fileName = file.getName();
				if(fileName.matches(SEARCH_PATTERN)) {
					String[] sp = fileName.split(NON_WORD_PATTERN);
					System.err.println(file.getAbsolutePath()+" is not in required format => "+String.join("?", sp));
					errorFound=true;
				}
			}
		}	
	}

}
