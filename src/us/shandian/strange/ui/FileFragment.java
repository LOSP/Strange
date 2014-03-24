package us.shandian.strange.ui;

import android.view.View;
import android.widget.AdapterView;

import java.lang.NullPointerException;

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
	
	public FileFragment() {
		// TODO: replace this with default dir
		this("/sdcard");
	}
	
	public FileFragment(String dir) {
		mDir = dir;
		mTitle = dir;
	}
	
	@Override
	protected void doLoadFiles() {
		// Load files
		mFileUtils = new FileUtils(mDir);

		// If not writable, try root
		if (!(mFileUtils instanceof RootFileUtils) && !mFileUtils.canWrite()) {
			mFileUtils = new RootFileUtils(mDir);
		}
		
		// Do some loading
		try {
			mFiles = mFileUtils.getFileItems();
		} catch (NullPointerException e) {
			// Permission denied, use root
			mFileUtils = new RootFileUtils(mDir);
			mFiles = mFileUtils.getFileItems();
		}
	}
	
	@Override
	protected void initUsageStatistics() {
		// Load usage statistics
		if (mFileUtils.getUsedPercentage() != -1) {
			// Statistics are available
			if (DEBUG) {
				android.util.Log.d(TAG, "Usage statistics available for " + mDir);
				android.util.Log.d(TAG, "Used = " + mFileUtils.getUsed() + " Free = " + mFileUtils.getFree() + " Percentage = " + mFileUtils.getUsedPercentage());
			}
			
			mUsage.setVisibility(View.VISIBLE);
			
			// Calculate percentage to with
			mUsageProgress.setTranslationX(- new Float(getActivity().getWindowManager().getDefaultDisplay().getWidth() * (1 - mFileUtils.getUsedPercentage() / 100.0f)).intValue());
			
			// Display as text
			mUsed.setText(getActivity().getResources().getString(R.string.statistics_used) + " " + mFileUtils.getUsed());
			mFree.setText(mFileUtils.getFree() + " " + getActivity().getResources().getString(R.string.statistics_free));
		} else {
			mUsage.setVisibility(View.GONE);
		}
		
		// Writablity
		// Though usage statistics may not available,
		// We still get the readablity.
		// Even if it cannot be seen,
		// It can still be used for the left menu
		if (mFileUtils.canWrite()) {
			mWritablity.setText(R.string.writablity_rw);
		} else {
			mWritablity.setText(R.string.writablity_ro);
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
	
	@Override
	public void onActivate() {
		if (getActivity() == null) return;
		
		if (DEBUG) {
			android.util.Log.d(TAG, "onActivate");
		}
		
		onActivate((MainActivity) getActivity());
	}
	
	public void onActivate(MainActivity activity) {
		if (mFileUtils instanceof RootFileUtils) {
			activity.setWritablity(mFileUtils.canWrite());
		} else {
			activity.disableRemount();
		}
	}
	
	public void remount() {
		if (DEBUG) {
			android.util.Log.d(TAG, "beginning remount");
		}
		
		// We can only remount with root
		if (!(mFileUtils instanceof RootFileUtils)) {
			return;
		}
		mFileUtils.remount(!mFileUtils.canWrite());
		initUsageStatistics();
	}
}
