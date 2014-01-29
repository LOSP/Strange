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

public class FileFragment extends BaseFragment implements OnItemClickListener
{
	private String mDir;
	
	private GridView mGrid;
	private FileAdapter mAdapter;
	
	public FileFragment(String dir) {
		mDir = dir;
		mTitle = dir;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_file, null);
		mGrid = (GridView) view.findViewById(R.id.fragment_file_grid);
		
		// TODO: Implement file browser, not test code
		ArrayList<FileItem> fileList = new ArrayList<FileItem>();
		FileItem testFile = new FileItem("..", "..", true);
		fileList.add(testFile);
		
		// Set adapter
		mAdapter = new FileAdapter(getActivity(), fileList);
		mGrid.setAdapter(mAdapter);
		mGrid.setOnItemClickListener(this);
		
		return view;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO: Implement this method
	}
}
