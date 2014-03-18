package us.shandian.strange.util;

import java.util.ArrayList;

import us.shandian.strange.type.FileItem;
import static us.shandian.strange.BuildConfig.DEBUG;

public class RootFileUtils extends FileUtils
{
	// This class plays the role of FileUtils, but has root permission
	private static final String TAG = RootFileUtils.class.getSimpleName();
	
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
		String result = new CMDProcessor().su.runWaitFor("busybox ls -1Fa " + mDirPath + "/").stdout;
		String[] fileNames = result == null ? new String[0] : result.split("\n");
		for (String f : fileNames) {
			if (f.equals("./") || f.equals("../")) continue;
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
		
		FileItemComparator.sortFileItems(ret);
		
		if (!isRoot()) {
			ret.add(0, new FileItem(getParent(), "..", true, false));
		}
		
		return ret;
	}

	@Override
	public void delete(FileItem file) {
		new CMDProcessor().su.runWaitFor(generateDeleteCmd(file));
	}

	@Override
	protected String getMountTable() {
		return new CMDProcessor().su.runWaitFor("busybox mount").stdout;
	}

	@Override
	public void remount(boolean rw) {
		if (DEBUG) {
			android.util.Log.d(TAG, "remount cmd : " + "busybox mount -o remount" + (rw ? " rw" : ",ro") + " " + mRemountPoint);
		}
		new CMDProcessor().su.runWaitFor("busybox mount -o remount" + (rw ? " rw" : ",ro") + " " + mRemountPoint);
		reloadWritablity();
	}
	
	@Override
	protected String getFileInfoStr(FileItem item) {
		return new CMDProcessor().su.runWaitFor("busybox ls -ledh " + item.path).stdout;
	}
	
	@Override
	protected String getDirSize(FileItem item) {
		return new CMDProcessor().su.runWaitFor("busybox du -sk " + item.path).stdout;
	}
}
