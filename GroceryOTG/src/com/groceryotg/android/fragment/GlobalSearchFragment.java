package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.groceryotg.android.R;

public class GlobalSearchFragment extends SherlockListFragment {
	private Context mContext;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.search_fragment_list, container, false);
		return v;
	}

	public void setQuery(String query) {
		TextView searchTitle = (TextView) ((Activity) mContext).findViewById(R.id.search_title);
		searchTitle.setText("\"" + query + "\"");
		
		GroceryListFragment frag = (GroceryListFragment) ((SherlockFragmentActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.search_fragment_list_fragment);
		frag.loadDataWithQuery(false, query);
	}
}
