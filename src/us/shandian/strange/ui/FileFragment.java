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
}
