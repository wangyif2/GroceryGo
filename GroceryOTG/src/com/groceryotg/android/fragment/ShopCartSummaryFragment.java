package com.groceryotg.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.groceryotg.android.R;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartSummaryFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private Context mContext;
	private ProgressBar mProgressView;
	private TextView mEmptyTextView;
	private SimpleCursorAdapter mAdapter;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] from = new String[]{StoreParentTable.COLUMN_STORE_PARENT_NAME,
					"SUM(" + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_PRICE + ")",
					StoreParentTable.COLUMN_STORE_PARENT_ID};
		int[] to = new int[]{R.id.shopcart_summary_storeparent,
				 R.id.shopcart_summary_total,
				 R.id.shopcart_summary_storeparent_id};
		
		getLoaderManager().initLoader(0, null, this);
		mAdapter = new SimpleCursorAdapter(mContext, R.layout.shopcart_summary_list_row, null, from, to,1);
		//mAdapter.setViewBinder(new ShopCartSummaryViewBinder());
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.grocery_fragment_list, container, false);
		
		mProgressView = (ProgressBar) v.findViewById(R.id.refresh_progress);
		mEmptyTextView = (TextView) v.findViewById(R.id.empty_grocery_list);
		
		if (mProgressView != null)
			mProgressView.setVisibility(View.VISIBLE);
		
		if (mEmptyTextView != null)
			mEmptyTextView.setVisibility(View.INVISIBLE);
		
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {StoreParentTable.COLUMN_STORE_PARENT_NAME,
        						"SUM(" + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_PRICE + ")",
        					   StoreParentTable.COLUMN_STORE_PARENT_ID};
        
        List<String> selectionArgs = new ArrayList<String>();
        String selection = "";
        
        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);
        
        String sortOrder = StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_NAME;
        
        return new CursorLoader(mContext, GroceryotgProvider.CONTENT_URI_CART_GROUPBY_STOREPARENT, projection, selection, selectionArgsArr, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
