import java.util.Map;

public class GraphNode {
	protected final String name;
	protected final String url;
	protected final String id;
	
	private final Map<Character,Character> remap = Map.of(
			' ','_',
			'\'','_',
			'ō','o',
			'ū','u'
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
	}

	public GraphNode(String name, String url) {
		this.name = name;
		this.id = convertToId(name);
		this.url = url;
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

	public boolean hasUrl() {
		return !url.equals("");
	}


//	public boolean hasMoreData(int i) {
//		return (name.length()>id.length() || hasUrl());
//	}

}
