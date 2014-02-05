package us.shandian.strange.adapter;

import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;

import java.util.ArrayList;

import us.shandian.strange.R;
import us.shandian.strange.type.FileItem;

public class FileAdapter extends BaseAdapter
{
	
	private static final int[] COLORS = new int[] {
		android.R.color.holo_blue_light,
		android.R.color.holo_green_dark,
		android.R.color.holo_orange_light,
		android.R.color.holo_red_light,
		android.R.color.holo_red_dark,
		android.R.color.holo_blue_bright,
		android.R.color.holo_purple,
		android.R.color.holo_blue_dark,
		android.R.color.holo_orange_dark,
		android.R.color.holo_green_light
	};
	
	private Context mContext;
	private ArrayList<FileItem> mFiles;
	
	private ArrayList<LinearLayout> mLayouts = new ArrayList<LinearLayout>();
	
	public FileAdapter(Context context, ArrayList<FileItem> files) {
		mContext = context;
		mFiles = files;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (FileItem f : mFiles) {
			// Inflate all the views
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_file_grid_item, null);
			
			// Find views and set texts
			TextView colorText = (TextView) layout.findViewById(R.id.fragment_file_grid_item_color_text);
			TextView fileName = (TextView) layout.findViewById(R.id.fragment_file_grid_item_file_name);
			
			colorText.setText(f.name.substring(0, 1));
			if (f.name.length() > 6) {
				fileName.setText(f.name.substring(0, 5) + "..");
			} else {
				fileName.setText(f.name);
			}
			
			char first = f.name.charAt(0);
			colorText.setBackgroundResource(COLORS[first % 10]);
			
			if (f.isDir) {
				colorText.getPaint().setFakeBoldText(true);
			}
			
			if (f.isSymLink) {
				colorText.getPaint().setFakeBoldText(true);
				colorText.getPaint().setUnderlineText(true);
			}
			
			// Add to list
			mLayouts.add(layout);
		}
	}
	
	@Override
	public int getCount() {
		return mFiles.size();
	}

	@Override
	public Object getItem(int position) {
		return mFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return position < mLayouts.size() ? mLayouts.get(position) : convertView;
	}

}
