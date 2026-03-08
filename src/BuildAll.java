
public class BuildAll {

	public static void main(String[] args) {
		int length=80;
		HistoryClean.main(args);
		System.out.println("=".repeat(length));
		HistoryGraph.main(args);
		System.out.println("=".repeat(length));
		HistoryRelationships.main(args);
		System.out.println("=".repeat(length));
		CreateTemplates.main(args);
		System.out.println("=".repeat(length));
		ConvertIframe.main(args);
	}
}
