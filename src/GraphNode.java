import java.io.File;
import java.util.Map;

public class GraphNode {
	protected final String name;
	protected final String url;
	protected final String id;
	protected final boolean hasImage;
	
	private static String PATH;
	
	public static void setPath(String path) {
		PATH=path;
	}
	
	private final Map<Character,Character> remap = Map.of(
			' ','_',
			'\'','_',
			'ō','o',
			'ū','u',
			'é','e'
			);
	
	private String convertToId(String userName) {
		int index=userName.indexOf('\n');
		if(index>-1) {
			userName = userName.substring(0,index);
		}
		index=userName.indexOf('(');
		if(index>-1) {
			userName = userName.substring(0,index);
		}
		userName=userName.trim();
		
		StringBuffer idBuffer = new StringBuffer();
		char c;
		for(int i=0;i<userName.length();i++) {
			c=userName.charAt(i);
			idBuffer.append(remap.getOrDefault(c,c));
		}
		return idBuffer.toString();	
	}
	
	public GraphNode(String name) {
		this.name = name;
		this.id = convertToId(name);
		this.url = id+".html";
		File file = new File(PATH+"Images\\"+id+".jpg");
		hasImage = file.exists();
	}

	public GraphNode(String name, String url) {
		this.name = name;
		this.id = convertToId(name);
		this.url = url;
		File file = new File(PATH+"Images\\"+id+".jpg");
		hasImage = file.exists();
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public String getImage() {
		return "Images/"+id+".jpg";
	}
	
	public boolean hasUrl() {
		return !url.equals("");
	}

	public boolean hasImage() {
		return hasImage;
	}

//	public boolean hasMoreData(int i) {
//		return (name.length()>id.length() || hasUrl());
//	}

}
