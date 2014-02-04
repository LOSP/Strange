package us.shandian.strange.ui;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.GridView.OnItemClickListener;
import android.os.Bundle;

import java.util.ArrayList;

import us.shandian.strange.R;
import us.shandian.strange.adapter.FileAdapter;
import us.shandian.strange.type.FileItem;
import us.shandian.strange.util.FileUtils;

public class FileFragment extends BaseFragment implements OnItemClickListener
{
	private String mDir;
	
	private GridView mGrid;
	private FileAdapter mAdapter;
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_file, null);
		mGrid = (GridView) view.findViewById(R.id.fragment_file_grid);
		
		// Load files
		mFileUtils = new FileUtils(mDir);
		
		// Set adapter
		mAdapter = new FileAdapter(getActivity(), mFileUtils.getFileItems());
		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(this);
		
		return view;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO: Implement this method
	}
}
