package us.shandian.strange.ui;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment
{
	protected String mTitle = "Page";
	
	public String getPageTitle() {
		return mTitle;
	}
	
	public void goBack() {
		// Do nothing by default
	}
}
