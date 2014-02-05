package us.shandian.strange.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.text.Collator;

import us.shandian.strange.type.FileItem;

public class RootFileUtils extends FileUtils
{
	// This class plays the role of FileUtils, but has root permission
	private String mDirPath;
	
	public RootFileUtils(String dir) {
		super(dir);
		mDirPath = dir;
		if (mDirPath.endsWith("/")) {
			mDirPath = mDirPath.substring(0, mDirPath.length() - 1);
		}
	}
	
	@Override
	public ArrayList<FileItem> getFileItems() {
		ArrayList<FileItem> ret = new ArrayList<FileItem>();
		String result = new CMDProcessor().su.runWaitFor("busybox ls -1F " + mDirPath + "/").stdout;
		String[] fileNames = result == null ? new String[0] : result.split("\n");
		for (String f : fileNames) {
			String name;
			if (f.endsWith("@") || f.endsWith("*") || f.endsWith("/")) {
				name = f.substring(0, f.length() - 1);
			} else {
				name = f;
			}
			String path = mDirPath + "/" + name;
			boolean isDir = f.endsWith("/") ? true : false;
			boolean isSymLink = f.endsWith("@") ? true : false;
			ret.add(new FileItem(path, name, isDir, isSymLink));
		}
		
		Collections.sort(ret, new Comparator<FileItem>() {

				@Override
				public int compare(FileItem p1, FileItem p2)
				{
					if (p1.isDir && !p2.isDir) {
						return -1;
					} else if(!p1.isDir && p2.isDir) {
						return 0;
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
}
