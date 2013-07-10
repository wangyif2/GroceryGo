package com.grocerygo.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.grocerygo.android.*;
import com.grocerygo.android.database.CartTable;
import com.grocerygo.android.database.FlyerTable;
import com.grocerygo.android.database.GroceryTable;
import com.grocerygo.android.database.StoreParentTable;
import com.grocerygo.android.database.contentprovider.GroceryotgProvider;
import com.grocerygo.android.utils.GroceryOTGUtils;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

public class ShopCartOverviewFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private Context mContext;
	
	private GroceryListCursorAdapter mAdapter;
	
	private ProgressBar mProgressView;
	private TextView mEmptyTextView;
	
	private SparseArray<Float> mDistanceMap;
	
	private BroadcastReceiver mRestartReceiver;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.shopcart_fragment_list, container, false);
		
		mProgressView = (ProgressBar) v.findViewById(R.id.refresh_progress);
		mEmptyTextView = (TextView) v.findViewById(R.id.empty_grocery_list);
		
		if (mProgressView != null)
			mProgressView.setVisibility(View.VISIBLE);
		
		if (mEmptyTextView != null)
			mEmptyTextView.setVisibility(View.INVISIBLE);
		
		mRestartReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(GroceryOTGUtils.BROADCAST_ACTION_RELOAD_GROCERY_LIST)) {
					// Restart the loader, refreshing all views
					reloadData(false);
				}
			}
		};
		IntentFilter mRestartIntentFilter = new IntentFilter(GroceryOTGUtils.BROADCAST_ACTION_RELOAD_GROCERY_LIST);
		mRestartIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(mContext).registerReceiver(mRestartReceiver, mRestartIntentFilter);
		
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mDistanceMap = ((GroceryApplication) ((Activity) mContext).getApplication()).getStoreDistanceMap();
		
		String[] from = new String[]{GroceryTable.COLUMN_GROCERY_ID,
				GroceryTable.COLUMN_GROCERY_NAME,
				GroceryTable.COLUMN_GROCERY_NAME,
				GroceryTable.COLUMN_GROCERY_PRICE,
				StoreParentTable.COLUMN_STORE_PARENT_NAME,
				FlyerTable.COLUMN_FLYER_ID,
				FlyerTable.COLUMN_FLYER_URL,
				CartTable.COLUMN_CART_FLAG_SHOPLIST,
				CartTable.COLUMN_ID,
				CartTable.COLUMN_CART_GROCERY_NAME,
				CartTable.COLUMN_CART_GROCERY_NAME};
		int[] to = new int[]{R.id.grocery_row_id,
				R.id.grocery_row_label,
				R.id.grocery_row_details,
				R.id.grocery_row_price,
				R.id.grocery_row_store_parent_name,
				R.id.grocery_row_store_id,
				R.id.grocery_row_flyer_url,
				R.id.grocery_row_in_shopcart,
				R.id.grocery_row_cart_item_id,
				R.id.grocery_row_label,
				R.id.grocery_row_details};
		
		mAdapter = new GroceryListCursorAdapter(mContext, R.layout.grocery_fragment_list_row, null, from, to, this.mDistanceMap);
		mAdapter.setViewBinder(new GroceryViewBinder(mContext));
		
		SlideExpandableListAdapter wrappedAdapter = new SlideExpandableListAdapter(mAdapter, R.id.expandable_toggle_button, R.id.expandable);
		// Make a VERY short animation duration
		wrappedAdapter.setAnimationDuration(0);
		
		setListAdapter(wrappedAdapter);
		
		this.fillData();
	}
	
	private void reloadData(Boolean reload) {
		Bundle b = new Bundle();
		if (reload) {
			b.putBoolean("reload", true);
			getLoaderManager().restartLoader(0, b, this);
		} else {
			b.putBoolean("reload", false);
			getLoaderManager().restartLoader(0, b, this);
		}
	}

	private void fillData() {
		// Prepare the asynchronous loader.
		Bundle b = new Bundle();
		b.putBoolean("reload", false);
		getLoaderManager().initLoader(0, b, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mRestartReceiver);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		List<String> selectionArgs = new ArrayList<String>();
		
		String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
				GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_ID,
				GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME,
				GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_PRICE,
				StoreParentTable.COLUMN_STORE_PARENT_NAME,
				FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID,
				FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_URL,
				CartTable.TABLE_CART + "." + CartTable.COLUMN_ID,
				CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_NAME,
				CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID,
				CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_FLAG_SHOPLIST};
		
		String selection = "";
		
		final String[] selectionArgsArr = new String[selectionArgs.size()];
		selectionArgs.toArray(selectionArgsArr);
		
		String sortOrder = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_NAME;
		return new CursorLoader(mContext, GroceryotgProvider.CONTENT_URI_CART_JOIN_GRO, projection, selection, selectionArgsArr, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
		if (mProgressView != null)
			mProgressView.setVisibility(View.GONE);
		if (cursor.getCount() <= 0) {
			if (mEmptyTextView != null)
				mEmptyTextView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mAdapter.swapCursor(null);
	}
}
