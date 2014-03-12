package us.shandian.strange.ui;

import android.view.View;
import android.widget.AdapterView;

import java.io.IOException;
import java.util.ArrayList;

import us.shandian.strange.type.FileItem;
import us.shandian.strange.util.ArchiveUtils;
import us.shandian.strange.util.ZipUtils;
import static us.shandian.strange.BuildConfig.DEBUG;

/*
    Implementation of BaseFileFragment
    Shows files in archives
*/
public class ArchiveFragment extends BaseFileFragment
{
	private static final String TAG = ArchiveFragment.class.getSimpleName();
	
	private String mArchive;
	private ArchiveUtils mArchiveUtils;
	
	private String mSubPath = "";
	private boolean mFirstLoad = true;
	
	public ArchiveFragment(String archive) {
		mArchive = archive;
		mTitle = mArchive;
	}

	@Override
	protected void doLoadFiles() {
		if (mFirstLoad) {
			try {
				// Create ArchiveUtils by archive type
				// TODO More types of archives
				if (mArchive.endsWith(".zip") || mArchive.endsWith(".apk")) {
					if (DEBUG) {
						android.util.Log.d(TAG, mArchive + " is a zip archive");
					}
					mArchiveUtils = new ZipUtils(mArchive);
				}
			} catch (IOException e) {
				if (DEBUG) {
					e.printStackTrace();
				}
				
				// File cannot open, but let's create an empty list
				// To avoid load loop
				mFiles = new ArrayList<FileItem>();
				
				return;
			}
		}
		
		mFirstLoad = false;
		
		// Load files
		mFiles = mArchiveUtils.loadEntries(mSubPath);
	}
	
	private void goTo(String subPath) {
		mSubPath = subPath;
		loadFiles();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FileItem f = (FileItem) mAdapter.getItem(position);
		if (f.isDir) {
			// Switch into a dir
			if (!f.name.equals("..")) {
				goTo(f.path + "/" + f.name);
			} else {
				goTo(f.path);
			}
		}
	}

	@Override
	public void onActivate() {
		((MainActivity) getActivity()).disableRemount();
	}
}
