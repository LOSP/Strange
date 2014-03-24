package us.shandian.strange.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.lang.StringBuilder;
import java.lang.reflect.Field;

import us.shandian.strange.R;
import us.shandian.strange.adapter.FragmentTabsAdapter;
import us.shandian.strange.type.FileItem;
import us.shandian.strange.util.CMDProcessor;
import us.shandian.strange.util.FileUtils;

public class MainActivity extends FragmentActivity implements OnItemClickListener
{
	private static final int DRAWER_MAIN_NEW = 0;
	private static final int DRAWER_MAIN_CLOSE = 1;
	
	private DrawerLayout mDrawer;
	private ListView mDrawerList;
	private TextView mVersionName;
	private ViewPager mPager;
	
	// Left Drawer
	private LinearLayout mLeft;
	
	// Drawer for file
	private TextView mFileName;
	private ListView mFileActions;
	
	// StatusBar Tint
	private TextView mTint;
	
	// Selected file
	private FileItem mSelected;
	private FileUtils mSelectedUtils;
	private Integer[] mSelectedActions;
	
	// Tasks
	private ProgressDialog mProgress;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					// Dismiss the progress dialog and reload
					mProgress.dismiss();
					allReload();
					break;
				case 1:
					// Only dismiss
					mProgress.dismiss();
					break;
				case 2:
					// AlertDialog needs to be built in main thread
					((AlertDialog.Builder) msg.obj).create().show();
					break;
			}
		}
	};
	
	private ActionBarDrawerToggle mToggle;
	public FragmentTabsAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Output busybox
		CMDProcessor.exportBusybox(this);
		
		// The "Please wait" dialog
		mProgress = new ProgressDialog(this);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setMessage(getResources().getString(R.string.msg_wait));
		mProgress.setCancelable(false);
		
		// Initialize the drawer
		mLeft = (LinearLayout) findViewById(R.id.activity_main_drawer_left);
		mDrawer = (DrawerLayout) findViewById(R.id.activity_main_drawer);
		mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, 0, 0) {
			
			// Only allow toggle of left drawer
			
			@Override
			public void onDrawerSlide(View view, float percent) {
				if (view == mLeft) {
					super.onDrawerSlide(view, percent);
				}
			}
			
			@Override
			public void onDrawerOpened(View view) {
				if (view == mLeft) {
					super.onDrawerOpened(view);
				}
			}
			
			@Override
			public void onDrawerClosed(View view) {
				if (view == mLeft) {
					super.onDrawerClosed(view);
				}
			}
			
		};
		
		mDrawer.setDrawerListener(mToggle);
		mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		mDrawer.setDrawerShadow(R.drawable.panel_shadow, Gravity.END);
		
		mDrawerList = (ListView) findViewById(R.id.activity_main_drawer_list);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main_drawer_list_item, getResources().getStringArray(R.array.drawer_items)));
		mDrawerList.setOnItemClickListener(this);
		
		// Display my version name
		mVersionName = (TextView) findViewById(R.id.activity_main_drawer_version_name);
		String version = "0.0.0";
		try {
			version = getPackageManager().getPackageInfo(getApplicationInfo().packageName, 0).versionName;
		} catch (Exception e) {
			// So what?
		}
		mVersionName.setText("v " + version);
		
		// File Drawer
		mFileName = (TextView) findViewById(R.id.activity_main_drawer_file_name);
		mFileActions = (ListView) findViewById(R.id.activity_main_drawer_file_actions);
		mFileActions.setOnItemClickListener(this);
		
		// Show "Up" button (Replaced by DrawerLayout)
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		// Hide the icon
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Initialize tabs
		mPager = (ViewPager) findViewById(R.id.activity_main_fragment_container);
		mAdapter = new FragmentTabsAdapter(getSupportFragmentManager(), getActionBar(), mPager);
		
		// Tint SystemBar
		initTint();
		
		// TODO: Replace this with restoring state of last time's
		mAdapter.addItem(new FileFragment("/sdcard"));
		mAdapter.addItem(new FileFragment("/"));
	}
	
	private void initTint() {
		findViewById(R.id.activity_main_container).setPadding(0, getStatusBarHeight() + getActionBarHeight() * 2, 0, 0);
		
		// Tint color
		mTint = (TextView) findViewById(R.id.activity_main_statusbar_tint);
		mTint.setHeight(getStatusBarHeight());
		
		setTintColor(getResources().getColor(android.R.color.holo_green_dark));
		
		// Split and stacked actionbar
		getActionBar().setSplitBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.gray)));
		getActionBar().setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.gray)));
	}
	
	private void setTintColor(int color) {
		// Changes the color of ActionBar and StatusBar
		getActionBar().setBackgroundDrawable(new ColorDrawable(color));
		mTint.setBackgroundColor(color);
	}
	
	protected FragmentTabsAdapter getFragmentTabsAdapter() {
		return mAdapter;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_menu, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mPager.setCurrentItem(getActionBar().getSelectedTab().getPosition());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mPager.setCurrentItem(getActionBar().getSelectedTab().getPosition());
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.activity_main_menu_right:
				if (mDrawer.isDrawerOpen(Gravity.END)) {
					mDrawer.closeDrawer(Gravity.END);
				} else {
					mDrawer.openDrawer(Gravity.END);
				}
				break;
			case android.R.id.home:
				return mToggle.onOptionsItemSelected(item);
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		mAdapter.getItem(mPager.getCurrentItem()).goBack();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (parent == mDrawerList) {
			// Handle event from drawer list
			switch (position) {
				case DRAWER_MAIN_NEW:
					// TODO Replace this with customize default new tab path
					mAdapter.addItem(new FileFragment("/"));
					mAdapter.setCurrentItem(mAdapter.getCount() - 1);
					break;
				case DRAWER_MAIN_CLOSE:
					// Close current tab
					if (mAdapter.getCount() == 1) {
						// Tabs must exist
						// TODO Replace this with customize default new tab path
						mAdapter.addItem(new FileFragment("/"));
					}
					mAdapter.removeItem(mAdapter.getCurrent());
					break;
					
			}
		} else if (parent == mFileActions) {
			switch (mSelectedActions[position]) {
				case R.string.drawer_file_action_install:
					FileUtils.installPackage(this, mSelected);
					break;
				case R.string.drawer_file_action_delete:
					mDrawer.closeDrawer(Gravity.END);
					
					mProgress.show();
					// Run commands in FileUtils to delete
					new Thread(new Runnable() {
						@Override
						public void run() {
							mSelectedUtils.delete(mSelected);
							mHandler.sendEmptyMessage(0);
						}
					}).start();
					break;
				case R.string.drawer_file_action_view_archive:
					// Create fragment to view the archive
					mDrawer.closeDrawer(Gravity.END);
					mAdapter.addItem(new ArchiveFragment(mSelected.path));
					mAdapter.setCurrentItem(mAdapter.getCount() - 1);
					break;
				case R.string.drawer_file_action_property:
					mDrawer.closeDrawer(Gravity.END);
					
					mProgress.show();
					new Thread(new Runnable() {
						@Override
						public void run() {
							showProperties();
							mHandler.sendEmptyMessage(1);
						}
					}).start();
					break;
			}
		}
	}
	
	public void selectFile(FileItem file, FileUtils utils) {
		// Select a file
		mSelected = file;
		mSelectedUtils = utils;
		mFileName.setText(mSelected.name);
		
		// Get those actions
		mSelectedActions = FileUtils.getFileActions(mSelected);
		ArrayList<String> actionNames = new ArrayList<String>();
		for (Integer action : mSelectedActions) {
			actionNames.add(getResources().getString(action));
		}
		
		// Set to list
		mFileActions.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main_drawer_list_item, actionNames));
		
		// Popup
		mDrawer.openDrawer(Gravity.END);
	}
	
	private void allReload() {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			BaseFragment f = mAdapter.getItem(i);
			if (f instanceof FileFragment) {
				((FileFragment) f).loadFiles();
			}
		}
	}
	
	private void showProperties() {
		AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
		StringBuilder sBuilder = new StringBuilder();
		Resources res = getResources();
		
		// Get the properties
		FileItem.Info info = mSelectedUtils.getFileInfo(mSelected);
		
		// Build string
		sBuilder.append(res.getString(R.string.prop_path))
				.append(" ").append(mSelected.path)
				.append("\n")
				.append(res.getString(R.string.prop_permission))
				.append(" ").append(info.permission)
				.append("\n")
				.append(res.getString(R.string.prop_group))
				.append(" ").append(info.group)
				.append("\n")
				.append(res.getString(R.string.prop_user))
				.append(" ").append(info.user)
				.append("\n")
				.append(res.getString(R.string.prop_size))
				.append(" ").append(info.size)
				.append("\n")
				.append(res.getString(R.string.prop_timestamp))
				.append(" ").append(info.timestamp);
		
		// Build up the dialog
		dBuilder.setMessage(sBuilder.toString());
		dBuilder.setTitle(res.getString(R.string.drawer_file_action_property));
		dBuilder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
		// Call main thread to show the dialog
		mHandler.sendMessage(mHandler.obtainMessage(2, dBuilder));
	}
	
	// Get the height of statusbar
	public int getStatusBarHeight() {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}
	
	// Get the height of ActionBar
	public int getActionBarHeight() {
		TypedValue tv = new TypedValue();
		int actionBarHeight = 0;
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
		}
		
		return actionBarHeight;
	}
}
