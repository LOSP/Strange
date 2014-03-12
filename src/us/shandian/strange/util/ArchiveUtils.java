package us.shandian.strange.util;

import java.util.ArrayList;

import us.shandian.strange.type.FileItem;

/*
    Base class of the compressed archive loaders
*/

public abstract class ArchiveUtils
{
	protected String mArchive;
	
	protected ArchiveUtils(String path) {
		mArchive = path;
	}
	
	protected static String getDirPath(String dir) {
		int index = dir.lastIndexOf("/");
		String path;
		if (index > 0) {
			path = dir.substring(0, index);
		} else {
			path = "";
		}
		return path;
	}
	
	public abstract ArrayList<FileItem> loadEntries(String subPath);
	
}
