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
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashMap;

import us.shandian.strange.R;
import us.shandian.strange.type.FileItem;
import static us.shandian.strange.BuildConfig.DEBUG;

public class FileAdapter extends BaseAdapter
{
	private static final String TAG = FileAdapter.class.getSimpleName();
	
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
	
	private HashMap<LinearLayout, Drawable> mIcons = new HashMap<LinearLayout, Drawable>();
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Icon loaded, set as background
			LinearLayout layout = mLayouts.get(msg.what);
			TextView colorText = (TextView) layout.findViewById(R.id.fragment_file_grid_item_color_text);
			colorText.post(new IconChanger((Drawable) msg.obj, colorText));
			
			// Save
			mIcons.put(layout, (Drawable) msg.obj);
		}
	};
	
	public FileAdapter(Context context, ArrayList<FileItem> files) {
		mContext = context;
		mFiles = files;
		
		new Thread(new ItemInflater()).start();
		new Thread(new IconLoader()).start();
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
		if (position >= mFiles.size()) {
			return convertView;
		} else {
			while (mLayouts.size() <= position);
			
			LinearLayout layout = mLayouts.get(position);
			
			// Load saved icon
			if (mIcons.containsKey(layout)) {
				if (!(layout.getBackground() instanceof BitmapDrawable)) {
					TextView colorText = (TextView) layout.findViewById(R.id.fragment_file_grid_item_color_text);
					colorText.post(new IconChanger(mIcons.get(layout), colorText));
				}
			}
			
			return layout;
		}
	}

	private class IconLoader implements Runnable {
		
		@Override
		public void run() {
			for (FileItem f : mFiles) {
				if (f.isDir || !f.name.toLowerCase().endsWith(".apk")) continue;
				
				int index = mFiles.indexOf(f);
				
				if (DEBUG) {
					android.util.Log.d(TAG, "f.name = " + f.name);
				}
				
				while (mLayouts.size() <= index);
				
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
				
				mHandler.sendMessage(mHandler.obtainMessage(index, icon));
			}
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
	
	private class ItemInflater implements Runnable {
		@Override
		public void run() {
			// Inflate all the views in background
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
				
				// Interrupt this thread
				Thread.currentThread().interrupt();
			}
		}
	}
}
