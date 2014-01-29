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

import java.util.ArrayList;

import us.shandian.strange.ui.BaseFragment;

public class FragmentTabsAdapter extends FragmentStatePagerAdapter implements TabListener, OnPageChangeListener
{
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
	
	public void removeItem(int position) {
		if (position >= getCount()) return;
		mActionBar.removeTabAt(position);
		mManager.beginTransaction().remove(mFragments.remove(position)).commit();
		notifyDataSetChanged();
	}
	
	@Override
	public BaseFragment getItem(int position) {
		return position < mFragments.size() ? mFragments.get(position) : null;
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

	@Override
	public void onPageScrolled(int p1, float p2, int p3) {
		// TODO: Implement this method
	}

	@Override
	public void onPageSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
		mActionBar.setTitle(mFragments.get(position).getPageTitle());
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
}
