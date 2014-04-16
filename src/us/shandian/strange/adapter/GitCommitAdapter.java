package us.shandian.strange.adapter;

import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.lib.ObjectId;

import us.shandian.strange.R;

public class GitCommitAdapter extends BaseAdapter
{
	private List<RevCommit> mCommits = new ArrayList<RevCommit>();
	private LayoutInflater mInflater;
	
	public GitCommitAdapter(Context context, Iterable<RevCommit> commits) {
		for (RevCommit commit : commits) {
			mCommits.add(commit);
		}
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	

	@Override
	public int getCount() {
		return mCommits.size();
	}

	@Override
	public RevCommit getItem(int position) {
		return mCommits.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= mCommits.size()) {
			return convertView;
		} else {
			RevCommit commit = mCommits.get(position);
			View ret = mInflater.inflate(R.layout.fragment_git_history_item, null);
			
			// Name
			TextView name = (TextView) ret.findViewById(R.id.fragment_git_history_commit_name);
			name.setText(commit.getShortMessage());
			
			// Date
			TextView date = (TextView) ret.findViewById(R.id.fragment_git_history_commit_date);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			date.setText(format.format(Long.valueOf(commit.getCommitTime()) * 1000));
			
			// Commiter
			TextView commiter = (TextView) ret.findViewById(R.id.fragment_git_history_commit_commiter);
			commiter.setText(commit.getCommitterIdent().getName());
			commiter.getPaint().setFakeBoldText(true);
			
			// Id
			TextView id = (TextView) ret.findViewById(R.id.fragment_git_history_commit_id);
			id.setText(ObjectId.toString(commit.getId()));
			
			return ret;
		}
	}
}
