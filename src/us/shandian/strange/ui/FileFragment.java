package us.shandian.strange.ui;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.GridView.OnItemClickListener;
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

public class FileFragment extends BaseFragment implements OnItemClickListener
{
	private String mDir;
	
	private ProgressBar mProgress;
	private GridView mGrid;
	private FileAdapter mAdapter;
	
	private FileUtils mFileUtils;
	private ArrayList<FileItem> mFiles;
	
	private boolean mLoaderFinished = true;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// This is only for loading thread
			mGrid.setAdapter(mAdapter);
			mGrid.setOnItemClickListener(FileFragment.this);
			mProgress.setVisibility(View.GONE);
			mGrid.setVisibility(View.VISIBLE);
			mLoaderFinished = true;
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
		
		// Load files
		mFileUtils = new FileUtils(mDir);

		// Start loading thread
		new Thread(new Runnable() {
				private boolean looperPrepared = false;

				@Override
				public void run() {
					if (!looperPrepared) {
						Looper.prepare();
						looperPrepared = true;
					}

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
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FileItem f = (FileItem) mAdapter.getItem(position);
		if (f.isDir) {
			// Switch into a dir
			goTo(f.path);
		}
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
