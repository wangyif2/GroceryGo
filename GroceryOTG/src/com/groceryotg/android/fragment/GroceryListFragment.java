package com.groceryotg.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.groceryotg.android.*;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.FlyerTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.settings.SettingsManager;
import com.groceryotg.android.utils.GroceryOTGUtils;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

public class GroceryListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String CATEGORY_POSITION = "position";
	
	private Context mContext;
	
	private GroceryListCursorAdapter mAdapter;
	private String mQuery = "";
	
	private ProgressBar mProgressView;
	private TextView mEmptyTextView;
	private Integer categoryId = GroceryListCursorAdapter.GLOBAL_SEARCH_CATEGORY;
	
	private SparseArray<Float> mDistanceMap;
	
	private BroadcastReceiver mRestartReceiver;

	public static GroceryListFragment newInstance(int pos) {
		GroceryListFragment f = new GroceryListFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt(CATEGORY_POSITION, pos);
		f.setArguments(args);
		
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			categoryId = args.getInt(CATEGORY_POSITION);
		}
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
				CartTable.COLUMN_CART_FLAG_SHOPLIST};
		int[] to = new int[]{R.id.grocery_row_id,
				R.id.grocery_row_label,
				R.id.grocery_row_details,
				R.id.grocery_row_price,
				R.id.grocery_row_store_parent_name,
				R.id.grocery_row_store_id,
				R.id.grocery_row_flyer_url,
				R.id.grocery_row_in_shopcart};
		
		// Handles the search filter
		Bundle args = ((Activity) mContext).getIntent().getExtras();
		if (args != null) {
			if (args.containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH)) {
				// Update the query - this is used by the loader when fetching results from database
				mQuery = args.getString(SearchManager.QUERY).trim();
			}
		}

		int layoutId = R.layout.grocery_fragment_list_row;
		mAdapter = new GroceryListCursorAdapter(mContext, layoutId, null, from, to, mQuery, getLoaderManager(), this, this.mDistanceMap);
		mAdapter.setViewBinder(new GroceryViewBinder(mContext));
		
		SlideExpandableListAdapter wrappedAdapter = new SlideExpandableListAdapter(mAdapter, R.id.expandable_toggle_button, R.id.expandable);
		// Make a VERY short animation duration
		wrappedAdapter.setAnimationDuration(0);
		
		setListAdapter(wrappedAdapter);
		
		this.loadDataWithQuery(false, mQuery);
	}
	
	public void loadDataWithQuery(Boolean reload, String query) {
		if (mAdapter == null)
			return;
		
		Bundle b = new Bundle();
		b.putString("query", query);
		if (reload) {
			b.putBoolean("reload", true);
			getLoaderManager().restartLoader(0, b, this);
		} else {
			b.putBoolean("reload", false);
			getLoaderManager().restartLoader(0, b, this);
		}
	}

	public void fillData() {
		// Prepare the asynchronous loader.
		Bundle b = new Bundle();
		b.putString("query", mQuery);
		b.putBoolean("reload", false);
		getLoaderManager().initLoader(0, b, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		final GroceryListFragment frag = this;
		
		mRestartReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(GroceryOTGUtils.BROADCAST_ACTION_RELOAD_GROCERY_LIST)) {
					// Restart the loader, refreshing all views
					Bundle b = new Bundle();
					b.putString("query", mQuery);
					b.putBoolean("reload", false);
					getLoaderManager().restartLoader(0, b, frag);
				}
			}
		};
		IntentFilter mRestartIntentFilter = new IntentFilter(GroceryOTGUtils.BROADCAST_ACTION_RELOAD_GROCERY_LIST);
		mRestartIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(mContext).registerReceiver(mRestartReceiver, mRestartIntentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mRestartReceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		String query = bundle.getString("query").trim();

		List<String> selectionArgs = new ArrayList<String>();
		boolean isAtLeastOneWhere = false;

		String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
				GroceryTable.COLUMN_GROCERY_ID,
				GroceryTable.COLUMN_GROCERY_NAME,
				GroceryTable.COLUMN_GROCERY_PRICE,
				StoreParentTable.COLUMN_STORE_PARENT_NAME,
				FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID,
				FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_URL,
				CartTable.COLUMN_CART_GROCERY_ID,
				CartTable.COLUMN_CART_FLAG_SHOPLIST};
		
		String selection;
		if (categoryId == GroceryListCursorAdapter.GLOBAL_SEARCH_CATEGORY) {
			selection = "";
		} else {
			if (!isAtLeastOneWhere) {
				isAtLeastOneWhere = true;
			}
			selection = GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_CATEGORY + "=?";
			selectionArgs.add(categoryId.toString());
		}

		// If user entered a search query, filter the results based on grocery name
		if (!query.isEmpty()) {
			if (!isAtLeastOneWhere) {
				isAtLeastOneWhere = true;
			} else {
				selection += " AND ";
			}
			selection += GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
			selectionArgs.add("%" + query + "%");
		}
		SparseBooleanArray selectedStores = SettingsManager.getStoreFilter(mContext);
		if (selectedStores != null && selectedStores.size() > 0) {
			// Go through selected stores and add them to query
			String storeSelection = "";
			for (int storeNum = 0; storeNum < selectedStores.size(); storeNum++) {
				if (selectedStores.valueAt(storeNum) == true) {
					if (storeSelection.isEmpty()) {
						if (!isAtLeastOneWhere) {
							isAtLeastOneWhere = true;
						} else {
							selection += " AND ";
						}
						storeSelection = "(";
						storeSelection += StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
					} else {
						storeSelection += " OR " + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
					}
					selectionArgs.add(((Integer) selectedStores.keyAt(storeNum)).toString());
				}
			}
			if (!storeSelection.isEmpty()) {
				storeSelection += ")";
				selection += storeSelection;
			}
		}
		if (CategoryTopFragmentActivity.mPriceRangeMin != null) {
			if (!isAtLeastOneWhere) {
				isAtLeastOneWhere = true;
			} else {
				selection += " AND ";
			}
			selection += GroceryTable.COLUMN_GROCERY_PRICE + " >= ?";
			selectionArgs.add(CategoryTopFragmentActivity.mPriceRangeMin.toString());
		}
		if (CategoryTopFragmentActivity.mPriceRangeMax != null) {
			if (!isAtLeastOneWhere) {
				isAtLeastOneWhere = true;
			} else {
				selection += " AND ";
			}
			selection += GroceryTable.COLUMN_GROCERY_PRICE + " <= ?";
			selectionArgs.add(CategoryTopFragmentActivity.mPriceRangeMax.toString());
		}

		final String[] selectionArgsArr = new String[selectionArgs.size()];
		selectionArgs.toArray(selectionArgsArr);
		return new CursorLoader(mContext, GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, GroceryTable.COLUMN_GROCERY_SCORE);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
		if (mProgressView != null)
			mProgressView.setVisibility(View.GONE);

		if (cursor.getCount() == 0) {
			if (!mQuery.isEmpty()) {
				displayEmptyListMessage(buildNoSearchResultString());
			} else {
				displayEmptyListMessage(buildNoNewContentString());
			}
		}
		
		// Now in the event we are searching, set the number of found items
		Integer cnt = mAdapter.getCount();
		TextView numResults = (TextView) ((Activity) mContext).findViewById(R.id.search_num_results);
		if (numResults != null) {
			numResults.setText(cnt.toString());
		}
	}
	
	private void displayEmptyListMessage(String emptyStringMsg) {
		mEmptyTextView.setText(emptyStringMsg);
		mEmptyTextView.setVisibility(View.VISIBLE);
		ListView listView = (ListView) getListView();
		listView.setEmptyView(mEmptyTextView);
	}
	
	private String buildNoNewContentString() {
		String emptyStringFormat = mContext.getString(R.string.no_new_content);
		return (ServerURL.getLastRefreshed() == null) ? String.format(emptyStringFormat, " Never") : String.format(emptyStringFormat, ServerURL.getLastRefreshed());
	}

	private String buildNoSearchResultString() {
		return mContext.getString(R.string.no_search_results);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mAdapter.swapCursor(null);
	}
}
