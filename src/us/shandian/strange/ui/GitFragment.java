package us.shandian.strange.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.os.Bundle;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;

import us.shandian.strange.R;
import us.shandian.strange.adapter.GitCommitAdapter;
import static us.shandian.strange.BuildConfig.DEBUG;

public class GitFragment extends BaseFragment
{
	private static final String TAG = GitFragment.class.getSimpleName();
	
	
	Repository mRepo;
	Git mGit;
	
	ListView mHistoryList;
	GitCommitAdapter mAdapter;
	
	public GitFragment(String repo) {
		mTitle = repo;
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		
		
		try {
			mRepo = builder.setGitDir(new File(repo + "/.git"))
							.readEnvironment()
							.findGitDir()
							.build();
		} catch (Exception e) {
			if (DEBUG) {
				android.util.Log.d(TAG, "Failed To Open Repository: " + repo);
			}
		}
		
		if (mRepo != null) {
			mGit = new Git(mRepo);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_git, null);
		
		mHistoryList = (ListView) view.findViewById(R.id.fragment_git_history);
		
		if (mGit != null) {
			try {
				Iterable<RevCommit> commits = mGit.log().call();
				
				if (DEBUG) {
					android.util.Log.d(TAG, "commits = " + commits.toString());
				}
				
				mAdapter = new GitCommitAdapter(getActivity(), commits);
			} catch (Exception e) {
				if (DEBUG) {
					android.util.Log.d(TAG, "Can't read repo, error:" + e.getMessage());
				}
			}
			
			if (mAdapter != null) {
				mHistoryList.setAdapter(mAdapter);
			}
		}
		
		return view;
	}
}
