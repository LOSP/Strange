package us.shandian.strange.ui;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;

import java.util.ArrayList;
import java.lang.NullPointerException;

import us.shandian.strange.R;
import us.shandian.strange.adapter.FileAdapter;
import us.shandian.strange.type.FileItem;
import us.shandian.strange.util.FileUtils;
import us.shandian.strange.util.RootFileUtils;
import static us.shandian.strange.BuildConfig.DEBUG;

public class FileFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener
{
	private static final String TAG = FileFragment.class.getSimpleName();
	
	private String mDir;
	
	private ProgressBar mProgress;
	private GridView mGrid;
	private FileAdapter mAdapter;
	
	// Usage statistics
	private RelativeLayout mUsage;
	private TextView mUsageProgress;
	private TextView mUsed;
	private TextView mFree;
	
	private FileUtils mFileUtils;
	private ArrayList<FileItem> mFiles;
	
	private boolean mLoaderFinished = true;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// This is only for loading thread
			mGrid.setAdapter(mAdapter);
			mGrid.setOnItemClickListener(FileFragment.this);
			mGrid.setOnItemLongClickListener(FileFragment.this);
			mProgress.setVisibility(View.GONE);
			mGrid.setVisibility(View.VISIBLE);
			mLoaderFinished = true;
			
			// Show usage status too
			initUsageStatistics();
		}
	};
	
	public FileFragment() {
		// TODO: replace this with default dir
		this("/sdcard");
	}
	
	public FileFragment(String dir) {
		mDir = dir;
		mTitle = dir;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_file, null);
		mProgress = (ProgressBar) view.findViewById(R.id.fragment_file_wait);
		mGrid = (GridView) view.findViewById(R.id.fragment_file_grid);
		mUsage = (RelativeLayout) view.findViewById(R.id.fragment_file_usage);
		mUsageProgress = (TextView) view.findViewById(R.id.fragment_file_usage_progress);
		mUsed = (TextView) view.findViewById(R.id.fragment_file_usage_used);
		mFree = (TextView) view.findViewById(R.id.fragment_file_usage_free);
		
		loadFiles();
		
		// Tint if I'm first
		MainActivity activity = (MainActivity) getActivity();
		if (activity.mAdapter.getItemPos(this) == 0) {
			activity.mAdapter.tintStatus(this);
		}
		
		return view;
	}
	
	private void loadFiles() {
		mLoaderFinished = false;
		
		mProgress.setVisibility(View.VISIBLE);
		mGrid.setVisibility(View.GONE);

		// Start loading thread
		new Thread(new Runnable() {
				private boolean looperPrepared = false;

				@Override
				public void run() {
					if (!looperPrepared) {
						Looper.prepare();
						looperPrepared = true;
					}

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

					try {
						mAdapter = new FileAdapter(getActivity(), mFiles);
					} catch (NullPointerException e) {
						run();

						// The following should not be called
						// But for safety, let's return
						return;
					}
					mHandler.sendEmptyMessage(0);

					Looper.loop();
				}
			}).start();
	}
	
	private void initUsageStatistics() {
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
			((MainActivity) getActivity()).selectFile(f);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		FileItem f = (FileItem) mAdapter.getItem(position);
		((MainActivity) getActivity()).selectFile(f);
		return true;
	}
	
	private void goTo(String path) {
		if (path.trim().length() == 0) {
			path = "/";
		}

		mDir = path;
		mTitle = path;

		((MainActivity) getActivity()).mAdapter.tintStatus(this);
		
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
