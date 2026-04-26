import java.io.File;

/**
 * SanityTest file
 */
public class SanityTest {
	private static final String startDir = "C:\\Users\\user\\Desktop\\Demo\\Website\\";
	private static final String NON_WORD_PATTERN = "[^\\w.]";
	private static final String SEARCH_PATTERN = ".*"+NON_WORD_PATTERN+".*";
	private static boolean errorFound=false;

	/**
	 * main method
	 * @param args
	 */
	public static void main(String[] args) {
		recursiveCheck(new File(startDir));
		if (!errorFound)
			System.out.println("All good");
	}

	/**
	 * recursiveCheck method
	 * @param dir
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
