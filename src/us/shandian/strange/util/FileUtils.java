package us.shandian.strange.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.Collator;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;

import us.shandian.strange.R;
import us.shandian.strange.type.FileItem;
import static us.shandian.strange.BuildConfig.DEBUG;

public class FileUtils
{
	enum FileType {
		FLODER,
		PACKAGE,
		UNKNOWN
	}
	
	private static final String TAG = FileUtils.class.getSimpleName();
	
	private File mDir;
	
	// Usage statistics
	private int mUsedPercentage = -1;
	private String mUsed = ""; // Human-readable string (df -h)
	private String mFree = "";
	
	public FileUtils(String dir) {
		mDir = new File(dir);
		
		try {
			getUsageStatistics();
		} catch (IOException e) {
			// So what?
		}
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
	
	public int getUsedPercentage() {
		return mUsedPercentage;
	}
	
	public String getUsed() {
		return mUsed;
	}
	
	public String getFree() {
		return mFree;
	}
	
	private void getUsageStatistics() throws IOException {
		// Real path
		String path = mDir.getCanonicalPath();
		if (DEBUG) {
			android.util.Log.d(TAG, "CanonicalPath = " + path);
		}
		
		// Ask shell for those data
		String shell = new CMDProcessor().sh.runWaitFor("busybox df -h").stdout;
		
		// A lot to process
		BufferedReader r = new BufferedReader(new StringReader(shell));
		
		String line = r.readLine();
		
		while (line != null) {
			// Read line by line, be patient
			String[] items = line.split(" ");
			if (items.length == 1) {
				line = line + " " + r.readLine();
				items = line.split(" ");
			}
			int itemNum = 0;
			String tmpUsed = "";
			String tmpFree = "";
			String tmpPercentage = "";
			String tmpPath = "";
			for (String item : items) {
				if (!item.trim().equals("")) {
					if (DEBUG) {
						android.util.Log.d("Strange", "item = " + item + " num = " + itemNum);
					}
					
					switch (itemNum) {
						case 2:
							tmpUsed = item;
							break;
						case 3:
							tmpFree = item;
							break;
						case 4:
							tmpPercentage = item;
							break;
						case 5:
							tmpPath = item;
							break;
					}
					
					itemNum++;
				}
			}
			
			if (!tmpPath.trim().equals("") && path.startsWith(tmpPath)) {
				// Finally I found you
				if (DEBUG) {
					android.util.Log.d(TAG, "percent = " + tmpPercentage);
				}
				mUsed = tmpUsed;
				mFree = tmpFree;
				mUsedPercentage = Integer.parseInt(tmpPercentage.replace("%", ""));
				break;
			}
			
			line = r.readLine();
		}
	}
	
	private static FileType getFileType(FileItem item) {
		// Get the known file type
		if (item.isDir) {
			return FileType.FLODER;
		} else if (item.name.endsWith(".apk")) {
			return FileType.PACKAGE;
		} else {
			return FileType.UNKNOWN;
		}
	}
	
	public static Integer[] getFileActions(FileItem item) {
		// What can we do for this file
		ArrayList<Integer> ret = new ArrayList<Integer>();
		FileType type = getFileType(item);
		if (type == FileType.FLODER) {
			ret.add(R.string.drawer_file_action_enter);
		} else if (type == FileType.PACKAGE) {
			ret.add(R.string.drawer_file_action_install);
		}
		ret.add(R.string.drawer_file_action_property);
		Integer[] array = new Integer[ret.size()];
		return ret.toArray(array);
	}
	
	public static void installPackage(Context context, FileItem pkg) {
		if (getFileType(pkg) != FileType.PACKAGE) return;
		
		// Install an apk
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(new File(pkg.path)), "application/vnd.android.package-archive");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}
