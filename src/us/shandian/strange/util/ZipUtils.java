package us.shandian.strange.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.IOException;

import us.shandian.strange.type.FileItem;
import static us.shandian.strange.BuildConfig.DEBUG;

/*
    Implementation of ArchiveUtils, loading zip file
    Using Java's interface
*/

public class ZipUtils extends ArchiveUtils
{
	private static final String TAG = ZipUtils.class.getSimpleName();
	
	private ZipFile mZip;
	private ArrayList<FileItem> mEntries = new ArrayList<FileItem>();
	
	public static boolean canLoad(String name) {
		return name.endsWith(".zip") || name.endsWith(".jar") ||
				name.endsWith(".apk");
	}
	
	public ZipUtils(String path) throws IOException {
		super(path);
		load();
	}
    
	private void load() throws IOException {
		// Load all the files first
		mZip = new ZipFile(mArchive);
		Enumeration<? extends ZipEntry> entries = mZip.entries();
		ArrayList<String> dirs = new ArrayList<String>();
		
		// Analyze entries
		while (entries.hasMoreElements()) {
			ZipEntry zip = entries.nextElement();
			String name = zip.getName();
			
			String path = "";
			String[] items = name.split("/");
			
			for (int i = 0; i < items.length - 1; i++) {
				path += items[i];
				
				// If this dir isn't saved, save it
				if (!dirs.contains(path)) {
					dirs.add(path);
				}
				
				if (i < items.length - 2) {
					path += "/";
				}
			}
			
			name = name.substring(path.length(), name.length());
			
			// DO NOT START WITH THIS THING
			if (name.startsWith("/")) {
				name = name.substring(1, name.length());
			}
			
			if (!name.trim().endsWith("/")) {
				mEntries.add(new FileItem(path, name, false, false));
			}
		}
		
		// Then let's record directories
		for (String dir : dirs) {
			String path = getDirPath(dir);
			String name = dir.substring(path.length(), dir.length());
			
			// DO NOT START WITH THIS THING
			if (name.startsWith("/")) {
				name = name.substring(1, name.length());
			}
			
			mEntries.add(new FileItem(path, name, true, false));
		}
	}
	
	@Override
	public ArrayList<FileItem> loadEntries(String subPath) {
		if (DEBUG) {
			android.util.Log.d(TAG, "subPath = " + subPath);
		}
		
		// DO NOT START WITH THIS
		if (subPath.startsWith("/")) {
			subPath = subPath.substring(1, subPath.length());
		}
		
		ArrayList<FileItem> entries = new ArrayList<FileItem>();
		
		// Look into all entries, find files under sub path
		for (FileItem item : mEntries) {
			if (DEBUG) {
				android.util.Log.d(TAG, "item.name = " + item.name + " item.path = " + item.path);
			}
			
			if (item.path.equals(subPath)) {
				// If this item matches the sub path,add it
				entries.add(item);
			}
		}
		
		FileItemComparator.sortFileItems(entries);
		
		// Add parent
		if (subPath != "") {
			entries.add(0, new FileItem(getDirPath(subPath), "..", true, false));
		}
		
		return entries;
	}
}
