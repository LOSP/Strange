package us.shandian.strange.type;

public class FileItem {
	public String path;
	public String name;
	public boolean isDir = false;
	public boolean isSymLink = false;
	
	public FileItem(String path, String name, boolean isDir, boolean isSymLink) {
		this.path = path;
		this.name = name;
		this.isDir = isDir;
		this.isSymLink = isSymLink;
	}
	
	public static class Info {
		public String permission;
		public String group;
		public String user;
		public String size;
		public String timestamp;
		
		public Info(String permission, String group, String user, String size, String timestamp) {
			this.permission = permission;
			this.group = group;
			this.user = user;
			this.size = size;
			this.timestamp = timestamp;
		}
	}
}
