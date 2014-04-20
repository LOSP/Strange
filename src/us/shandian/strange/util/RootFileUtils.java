package us.shandian.strange.util;

import android.content.pm.PackageManager;

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
		String result = mCmd.su.runWaitFor("busybox ls -1Fa " + mDirPath + "/").stdout;
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
			
			// If it is a symlink, we have to try
			// To find out if it is a directory
			if (isSymLink) {
				String out = mCmd.su.runWaitFor("busybox ls -1Fa " + path + "/").stdout;
				if (out != null && out.split("\n").length > 1) {
					isDir = true;
				}
			}
			
			ret.add(new FileItem(path, name, isDir, isSymLink));
		}
		
		FileItemComparator.sortFileItems(ret);
		
		if (!isRoot()) {
			ret.add(0, new FileItem(getParent(), "..", true, false));
		}
		
		return ret;
	}
	
	public void remount() {
		String[] mounts = mCmd.getMounts(mDirPath);
		if (mounts == null) {
			mounts = mCmd.getMounts("/");
		}
		
		String mountPoint = mounts[1];
		
		if (DEBUG) {
			android.util.Log.d(TAG, "mountPoint = " + mountPoint);
		}
		
		mCmd.su.runWaitFor("busybox mount -o remount rw " + mountPoint);
	}

	@Override
	public void delete(FileItem file) {
		mCmd.su.runWaitFor(generateDeleteCmd(file));
	}
	
	@Override
	protected String getFileInfoStr(FileItem item) {
		return mCmd.su.runWaitFor("busybox ls -ledh " + item.path).stdout;
	}
	
	@Override
	protected String getDirSize(FileItem item) {
		return mCmd.su.runWaitFor("busybox du -sk " + item.path).stdout;
	}
	
	public static void forceInstallPackage(PackageManager pm, FileItem item) {
		String packageName = pm.getPackageArchiveInfo(item.path, PackageManager.GET_ACTIVITIES).packageName;
		String dataPath = "/data/app/" + packageName + "-1.apk";
		mCmd.su.runWaitFor("busybox cp " + item.path + " " + dataPath);
		mCmd.su.runWaitFor("busybox chmod 0777 " + dataPath);
		mCmd.su.runWaitFor("busybox chown system:system " + dataPath);
	}
}
