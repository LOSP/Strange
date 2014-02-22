package us.shandian.strange.adapter;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.AsyncTask;

import java.util.ArrayList;

import us.shandian.strange.ui.BaseFragment;
import us.shandian.strange.ui.MainActivity;
import static us.shandian.strange.BuildConfig.DEBUG;

public class FragmentTabsAdapter extends FragmentStatePagerAdapter implements TabListener, OnPageChangeListener
{
	private static final int[] COLORS = new int[] {
		android.R.color.holo_green_dark,
		android.R.color.holo_red_dark,
		android.R.color.holo_purple,
		android.R.color.holo_blue_dark,
		android.R.color.holo_orange_dark
	};
	
	private ArrayList<BaseFragment> mFragments = new ArrayList<BaseFragment>();
	private ActionBar mActionBar;
	private ViewPager mPager;
	private FragmentManager mManager;
	
	public FragmentTabsAdapter(FragmentManager manager, ActionBar actionBar, ViewPager pager) {
		super(manager);
		mManager = manager;
		mActionBar = actionBar;
		mPager = pager;
		
		// Initialize
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mPager.setOffscreenPageLimit(Integer.MAX_VALUE);
		mPager.setAdapter(this);
		mPager.setOnPageChangeListener(this);
	}
	
	public int addItem(BaseFragment item) {
		mFragments.add(item);
		Tab tab = mActionBar.newTab();
		tab.setTabListener(this);
		tab.setText(item.getPageTitle());
		mActionBar.addTab(tab);
		notifyDataSetChanged();
		return getCount() - 1;
	}
	
	public void setCurrentItem(int position) {
		Tab tab = mActionBar.getTabAt(position);
		mActionBar.selectTab(tab);
		mActionBar.setTitle(tab.getText());
		mPager.setCurrentItem(position);
	}
	
	public void renameItem(int position) {
		mActionBar.getTabAt(position).setText(mFragments.get(position).getPageTitle());
		notifyDataSetChanged();
	}
	
	public void renameItem(BaseFragment item) {
		renameItem(getItemPos(item));
	}
	
	public void removeItem(int position) {
		if (position >= getCount()) return;
		mActionBar.removeTabAt(position);
		mManager.beginTransaction().remove(mFragments.remove(position)).commit();
		notifyDataSetChanged();
	}
	
	public int getItemPos(BaseFragment item) {
		return mFragments.indexOf(item);
	}

	@Override
	public int getItemPosition(Object object)
	{
		// Force reload
		return POSITION_NONE;
	}
	
	@Override
	public BaseFragment getItem(int position) {
		return position < mFragments.size() ? mFragments.get(position) : null;
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	public int getCurrent() {
		return mPager.getCurrentItem();
	}
	
	@Override
	public void onPageScrolled(int p1, float p2, int p3) {
		// TODO: Implement this method
	}

	@Override
	public void onPageSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
		mActionBar.setTitle(mFragments.get(position).getPageTitle());
		tintStatus(mFragments.get(position));
	}

	@Override
	public void onPageScrollStateChanged(int position) {
		// TODO: Implement this method
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction transaction)
	{
		mActionBar.setTitle(mFragments.get(tab.getPosition()).getPageTitle());
		mPager.setCurrentItem(tab.getPosition());
		tintStatus(mFragments.get(tab.getPosition()));
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction transaction)
	{
		// TODO: Implement this method
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction transaction)
	{
		// TODO: Implement this method
	}
	
	public void tintStatus(BaseFragment fragment) {
		// Only current is allowed
		if (mFragments.get(getCurrent()) != fragment) return;
		MainActivity activity = (MainActivity) fragment.getActivity();
		if (activity == null) return;
		String dir = fragment.getPageTitle();
		// Set Tint Color
		int id;
		try {
			id = dir.charAt(dir.lastIndexOf("/") + 1) % 5;
		} catch (Exception e) {
			id = dir.charAt(0) % 5;
		}
		int tintColor = activity.getResources().getColor(COLORS[id]);
		activity.setTintColor(tintColor);
		fragment.onTint(tintColor);
		
		// Call all of them
		new AsyncTask<Integer, Void, Void>() {
			@Override
			public Void doInBackground(Integer ... params){
				for (BaseFragment f : mFragments) {
					try {
						f.onTint(params[0]);
					} catch (NullPointerException e) {
						// Something might be null when running
						// Just avoid it and let it go
					}
				}
				return null;
			}
		}.execute(tintColor);
		
		if (DEBUG) {
			android.util.Log.d("Strange", "newTitle = " + fragment.getPageTitle());
		}
	}
}
