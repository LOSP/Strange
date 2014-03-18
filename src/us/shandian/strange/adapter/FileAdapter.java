package us.shandian.strange.adapter;

import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

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
		android.R.color.holo_orange_dark,
		android.R.color.holo_green_light
	};
	
	private Context mContext;
	private ArrayList<FileItem> mFiles;
	
	private ArrayList<LinearLayout> mLayouts = new ArrayList<LinearLayout>();
	private ArrayList<Boolean> mLoadeds = new ArrayList<Boolean>();
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Icon loaded, set as background
			LinearLayout layout = mLayouts.get(msg.what);
			TextView colorText = (TextView) layout.findViewById(R.id.fragment_file_grid_item_color_text);
			colorText.post(new IconChanger((Drawable) msg.obj, colorText));
		}
	};
	
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
			fileName.setText(f.name);
			
			char first = f.name.charAt(0);
			colorText.setBackgroundResource(COLORS[first % COLORS.length]);
			
			if (f.isDir) {
				colorText.getPaint().setFakeBoldText(true);
			}
			
			if (f.isSymLink) {
				colorText.getPaint().setFakeBoldText(true);
				colorText.getPaint().setUnderlineText(true);
			}
			
			// Add to list
			mLayouts.add(layout);
			
			mLoadeds.add(false);
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
		if (position >= mLayouts.size()) {
			return convertView;
		} else {
			if (!mLoadeds.get(position)) {
				// Only load once
				mLoadeds.set(position, true);
				if (mFiles.get(position).name.toLowerCase().endsWith(".apk")) {
					new Thread(new IconLoader(position)).start();
				}
			}
			return mLayouts.get(position);
		}
	}

	private class IconLoader implements Runnable {
		private int position;
		
		public IconLoader(int position) {
			this.position = position;
		}
		
		@Override
		public void run() {
			FileItem f = mFiles.get(position);
			ApplicationInfo info;
			try {
				info = mContext.getPackageManager().getPackageArchiveInfo(f.path, PackageManager.GET_ACTIVITIES)
								.applicationInfo;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			info.sourceDir = f.path;
			info.publicSourceDir = f.path;
			Drawable icon = info.loadIcon(mContext.getPackageManager());
			mHandler.sendMessage(mHandler.obtainMessage(position, icon));
		}
	}
	
	private class IconChanger implements Runnable {
		private Drawable icon;
		private TextView view;
		
		public IconChanger(Drawable icon, TextView view) {
			this.icon = icon;
			this.view = view;
		}
		
		@Override
		public void run() {
			view.setText("");
			view.setBackgroundDrawable(icon);
		}
	}
}
