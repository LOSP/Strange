package us.shandian.strange.ui;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.content.res.Configuration;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;

import us.shandian.strange.R;
import us.shandian.strange.adapter.FragmentTabsAdapter;
import us.shandian.strange.util.CMDProcessor;

public class MainActivity extends FragmentActivity
{
	private DrawerLayout mDrawer;
	private ListView mDrawerList;
	private TextView mVersionName;
	private ViewPager mPager;
	
	// Drawer for file
	private TextView mFileName;
	private ListView mFileActions;
	
	private ActionBarDrawerToggle mToggle;
	private FragmentTabsAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Output busybox
		CMDProcessor.exportBusybox(this);
		
		// Initialize the drawer
		mDrawer = (DrawerLayout) findViewById(R.id.activity_main_drawer);
		mToggle = new ActionBarDrawerToggle(this, mDrawer, R.drawable.ic_drawer, 0, 0);
		
		mDrawer.setDrawerListener(mToggle);
		mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		mDrawer.setDrawerShadow(R.drawable.panel_shadow, Gravity.END);
		
		mDrawerList = (ListView) findViewById(R.id.activity_main_drawer_list);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main_drawer_list_item, getResources().getStringArray(R.array.drawer_items)));
		
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
		
		// Show "Up" button (Replaced by DrawerLayout)
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		// Hide the icon
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Initialize tabs
		mPager = (ViewPager) findViewById(R.id.activity_main_fragment_container);
		mAdapter = new FragmentTabsAdapter(getSupportFragmentManager(), getActionBar(), mPager);
		
		// TODO: Remove tests
		mAdapter.addItem(new FileFragment("/sdcard"));
		mAdapter.addItem(new FileFragment("/sdcard/Download"));
		mAdapter.addItem(new FileFragment("/"));
		mAdapter.addItem(new FileFragment("/data"));
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
}
