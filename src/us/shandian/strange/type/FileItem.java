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
}
