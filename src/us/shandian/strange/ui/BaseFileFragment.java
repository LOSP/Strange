package us.shandian.strange.ui;

import android.widget.ProgressBar;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;

import java.util.ArrayList;

import us.shandian.strange.R;
import us.shandian.strange.adapter.FileAdapter;
import us.shandian.strange.adapter.FragmentTabsAdapter;
import us.shandian.strange.type.FileItem;
import static us.shandian.strange.BuildConfig.DEBUG;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/*
    This is a base class for all fragments that shows files.
    Any class which extends this shows files from different type of containers
*/

public class BaseFileFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, OnRefreshListener
{
	private static final String TAG = BaseFileFragment.class.getSimpleName();
	
	private ProgressBar mProgress;
	private GridView mGrid;
	
	protected FileAdapter mAdapter;

	protected ArrayList<FileItem> mFiles;

	// Pull To Refresh
	private PullToRefreshLayout mPullToRefresh;

	protected boolean mLoaderFinished = true;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// This is only for loading thread
			FragmentTabsAdapter adapter = ((MainActivity) getActivity()).mAdapter;
			if (adapter.getItemPos(BaseFileFragment.this) == adapter.getCurrent()) {
				onActivate();
			}
			
			mAdapter = new FileAdapter(getActivity(), mFiles);
			mGrid.setAdapter(mAdapter);
			mGrid.setOnItemClickListener(BaseFileFragment.this);
			mGrid.setOnItemLongClickListener(BaseFileFragment.this);
			mProgress.setVisibility(View.GONE);
			mGrid.setVisibility(View.VISIBLE);
			mLoaderFinished = true;

			// Refresh finish
			mPullToRefresh.setRefreshComplete();
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_file, null);
		mProgress = (ProgressBar) view.findViewById(R.id.fragment_file_wait);
		mGrid = (GridView) view.findViewById(R.id.fragment_file_grid);
		mGrid.setDrawingCacheEnabled(true);
		
		// Pull To Refresh
		mPullToRefresh = new PullToRefreshLayout(mGrid.getContext());

		ActionBarPullToRefresh.from(getActivity())
			.insertLayoutInto((ViewGroup) view.findViewById(R.id.fragment_file_grid_container))
			.theseChildrenArePullable(new View[] {mGrid})
			.listener(this)
			.setup(mPullToRefresh);
		
		mPullToRefresh.setBackgroundColor(getResources()
								.getColor(R.color.gray));

		loadFiles();

		return view;
	}
	
	public void loadFiles() {
		mLoaderFinished = false;

		mProgress.setVisibility(View.VISIBLE);
		mGrid.setVisibility(View.GONE);

		// Start loading thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				doLoadFiles();
				
				mHandler.sendEmptyMessage(0);

				// Thread.currentThread().interrupt();
			}
		}).start();
	}
	
	protected void doLoadFiles() {
		// Should be Overridden
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return false;
	}

	@Override
	public void onRefreshStarted(View view) {
		loadFiles();
	}
}
