public class GraphNode {
	protected final String name;
	protected final String url;
	protected final String id;
	
	public GraphNode(String name, String url) {
		this.name = name;
		this.url = url;
		String id;

		if(name.indexOf('\n')>-1) {
			id = name.substring(0, name.indexOf('\n'));
		} else if(name.indexOf('(')>-1) {
			id = name.substring(0, name.indexOf('('));
		} else {
			id = name;
		}
		this.id = id.trim().replace(' ', '_').replace('\'','_');
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getShortUrl() {
		return id.replace("_", "%20")+".html";
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
