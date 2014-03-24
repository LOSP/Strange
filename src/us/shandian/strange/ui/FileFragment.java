package us.shandian.strange.ui;

import android.view.View;
import android.widget.AdapterView;

import java.lang.NullPointerException;
import java.io.IOException;
import java.util.ArrayList;

import us.shandian.strange.R;
import us.shandian.strange.type.FileItem;
import us.shandian.strange.util.FileUtils;
import us.shandian.strange.util.RootFileUtils;
import static us.shandian.strange.BuildConfig.DEBUG;

/* 
    An implementation of BaseFileFragment
    Shows file from storage partitions
*/

public class FileFragment extends BaseFileFragment 
{
	private static final String TAG = FileFragment.class.getSimpleName();
	
	private String mDir;
	
	private FileUtils mFileUtils;
	
	private boolean mGranted = false;
	private boolean mRemounted = false;
	private boolean mAlertShown = false;
	
	public FileFragment() {
		// TODO: replace this with default dir
		this("/sdcard");
	}
	
	public FileFragment(String dir) {
		mDir = dir;
		mTitle = dir;
	}
	
	public void grant() {
		mGranted = true;
		mRemounted = false;
		loadFiles();
	}
	
	@Override
	protected void doLoadFiles() {
		// Load files
		if (!mGranted) {
			mFileUtils = new FileUtils(mDir);
		} else {
			mFileUtils = new RootFileUtils(mDir);
			if (!mRemounted) {
				((RootFileUtils) mFileUtils).remount();
			}
		}
		
		// Do some loading
		try {
			mFiles = mFileUtils.getFileItems();
		} catch (NullPointerException e) {
			// Permission denied
			if (!mAlertShown) {
				getMainActivity().alertPermission();
				mAlertShown = true;
			}
			mFiles = new ArrayList<FileItem>();
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FileItem f = (FileItem) mAdapter.getItem(position);
		if (f.isDir) {
			// Switch into a dir
			goTo(f.path);
		} else {
			// Select
			((MainActivity) getActivity()).selectFile(f, mFileUtils);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		FileItem f = (FileItem) mAdapter.getItem(position);
		((MainActivity) getActivity()).selectFile(f, mFileUtils);
		return true;
	}
	
	private void goTo(String path) {
		if (path.trim().length() == 0) {
			path = "/";
		}

		mDir = path;
		mTitle = path;
		
		MainActivity activity = (MainActivity) getActivity();
		activity.getFragmentTabsAdapter().renameItem(this);
		activity.getActionBar().setTitle(mTitle);
		
		mAlertShown = false;
		
		loadFiles();
	}
	
	@Override
	public void goBack() {
		if (mLoaderFinished) {
			if (!mFileUtils.isRoot()) {
				goTo(mFileUtils.getParent());
			} else {
				getActivity().finish();
			}
		}
	}
}
