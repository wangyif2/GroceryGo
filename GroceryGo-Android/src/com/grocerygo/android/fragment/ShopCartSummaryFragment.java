package com.grocerygo.android.fragment;

import java.util.ArrayList;
import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import com.grocerygo.android.database.GroceryotgDatabaseHelper;
import com.grocerygo.android.database.StoreParentTable;

public class ShopCartSummaryFragment extends SherlockListFragment { //implements LoaderManager.LoaderCallbacks<Cursor>
	private Context mContext;
	private GroceryotgDatabaseHelper database;
	
	private ShopCartSummaryArrayAdapter mAdapter;
	
	private ProgressBar mProgressView;
	private TextView mEmptyTextView;
	private View mHeaderView;
	
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
		View v = inflater.inflate(R.layout.shopcart_summary_fragment_list, container, false);
		mHeaderView = inflater.inflate(R.layout.shopcart_summary_list_header, container, false);
		
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
		
		if (mHeaderView != null) {
			this.getListView().addHeaderView(mHeaderView);
		}
		
		// Construct a raw query because SimpleCursorAdapter doesn't support 
		// aggregate statements like SUM() in the from column spec
		database = new GroceryotgDatabaseHelper(mContext);
		SQLiteDatabase db = database.getWritableDatabase();
		
		String[] from = new String[]{StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_ID,
									 StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID,
									 StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_NAME,
									 "SUM(" + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_PRICE + ")"};
		String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + " IS NOT NULL";
		String groupByClause = StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID;
		
		StringBuilder sb = new StringBuilder();
		String delim_comma = ",";
		for (String s : from) {
			sb.append(s).append(delim_comma);
		}
		sb.deleteCharAt(sb.length()-1); // delete last delimiter
		
		String sqlQuery = "SELECT " + 
							sb.toString() + 
							" FROM " + 
							CartTable.TABLE_CART
							+ " INNER JOIN " + GroceryTable.TABLE_GROCERY
								+ " ON " + CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID
								+ " = " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_ID
							+ " INNER JOIN " + FlyerTable.TABLE_FLYER
								+ " ON " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_FLYER
								+ "=" + FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID
							+ " INNER JOIN " + StoreParentTable.TABLE_STORE_PARENT
								+ " ON " + FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_STOREPARENT
								+ "=" + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + 
							" WHERE " + whereClause + 
							" GROUP BY " + groupByClause;
		
		String[] selection = new String[]{};
		Cursor cur = db.rawQuery(sqlQuery, selection);
		
		ArrayList<ShopCartSummaryItem> values = new ArrayList<ShopCartSummaryItem>();
		if (cur != null && cur.moveToFirst()) {
			boolean beforeEnd = true;
			while (cur != null && beforeEnd) {
				// Bind the results to the view elements
				ShopCartSummaryItem nextItem = new ShopCartSummaryItem();
				for (int columnIndex = 0; columnIndex < cur.getColumnCount(); columnIndex++) {
					if (columnIndex == cur.getColumnIndex(StoreParentTable.COLUMN_ID)) {
						
					}
					else if (columnIndex == cur.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID)) {
						nextItem.setStoreParentId(cur.getString(columnIndex));
					}
					else if (columnIndex == cur.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME)) {
						nextItem.setStoreParentName(cur.getString(columnIndex));
					}
					else { // aggregation column
						nextItem.setStoreTotal(cur.getString(columnIndex));
					}
				}
				values.add(nextItem);
				beforeEnd = cur.moveToNext();
			}
		}
		
		mAdapter = new ShopCartSummaryArrayAdapter(mContext, R.layout.shopcart_summary_list_row, values);
		
		setListAdapter(mAdapter);
		if (mProgressView != null)
			mProgressView.setVisibility(View.INVISIBLE);
		
		
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
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
}
