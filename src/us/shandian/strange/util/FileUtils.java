package us.shandian.strange.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.Collator;

import us.shandian.strange.type.FileItem;

public class FileUtils
{
	private File mDir;
	
	public FileUtils(String dir) {
		mDir = new File(dir);
	}
	
	public ArrayList<FileItem> getFileItems() {
		// Get all files and dirs
		final ArrayList<FileItem> ret = new ArrayList<FileItem>();
		File[] files = mDir.listFiles();
		for (File f : files) {
			String path = f.getPath();
			String name = f.getName();
			boolean isDir = f.isDirectory();
			boolean isSymLink;
			try {
				String absolutePath = f.getAbsolutePath();
				String canonicalPath = f.getCanonicalPath();
				String parentAbsolute = mDir.getAbsolutePath();
				String parentCanonical = mDir.getCanonicalPath();
				
				if (!parentAbsolute.equals(parentCanonical)) {
					// Parent floder itself is a symlink
					absolutePath = absolutePath.replaceFirst(parentAbsolute, parentCanonical);
				}
				
				isSymLink = !absolutePath.equals(canonicalPath);
			} catch (IOException e) {
				isSymLink = false;
			}
			ret.add(new FileItem(path, name, isDir, isSymLink));
		}
		
		Collections.sort(ret, new Comparator<FileItem>() {

				@Override
				public int compare(FileItem p1, FileItem p2)
				{
					if (p1.isDir && !p2.isDir) {
						return -1;
					} else if(!p1.isDir && p2.isDir) {
						return 1;
					} else {
						return Collator.getInstance().compare(p1.name, p2.name);
					}
				}

			
		});
		
		if (!isRoot()) {
			ret.add(0, new FileItem(getParent(), "..", true, false));
		}
		
		return ret;
	}
	
	public boolean isRoot() {
		return mDir.getPath().equals("/");
	}
	
	public String getParent() {
		return isRoot() ? mDir.getPath() : mDir.getPath().substring(0, mDir.getPath().lastIndexOf("/"));
	}
}
