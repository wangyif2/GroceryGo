package com.groceryotg.android.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groceryotg.android.MapFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.utils.GroceryOTGUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GroceryListCursorAdapter extends SimpleCursorAdapter {
	public static final int GLOBAL_SEARCH_CATEGORY = -1;
	
	private Context mContext;
	
	private SparseArray<Float> mDistanceMap;
	
	public GroceryListCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, SparseArray<Float> distanceMap) {
		super(context, layout, c, from, to, 0);
		
		this.mContext = context;
		
		this.mDistanceMap = distanceMap;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		final View topView = super.getView(position, convertView, parent);
		
		// Add text to the distance text view
		LinearLayout parentLayout = (LinearLayout) topView.findViewById(R.id.grocery_list_row_layout);
		TextView distanceTextView = (TextView) parentLayout.findViewById(R.id.grocery_row_distance);
		Float closestDist = null, newDist;
		
		TextView storesTextView = (TextView) parentLayout.findViewById(R.id.grocery_row_store_id);
		String list = storesTextView.getText().toString();
		if (!list.equals("")) {
			for (String s : list.split(",")) {
				newDist = this.mDistanceMap.get(Integer.parseInt(s));
				if (newDist != null) {
					if (closestDist == null || newDist < closestDist) {
						closestDist = newDist;
					}
				}
			}
		}
		
		if (closestDist != null) {
			// Truncate to a single decimal place
			DecimalFormat oneD = new DecimalFormat("#.#");
			Float truc = Float.valueOf(oneD.format((float) (closestDist/1000.0)));
			distanceTextView.setText(truc.toString() + "km");
		} else {
			distanceTextView.setText(mContext.getString(R.string.grocery_row_distance_missing));
		}
		
		// Replace the default map icon next to the distance text with the store parent's icon
		TextView storeParentTextView = (TextView) parentLayout.findViewById(R.id.grocery_row_store_parent_name);
		Integer iconId = (Integer) storeParentTextView.getTag();
		if (iconId != null) {
			Drawable iconDrawable = mContext.getResources().getDrawable(iconId);
			distanceTextView.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
		}
		
		// Now add listeners for the different buttons
		CheckBox cb_inshoplist = (CheckBox) topView.findViewById(R.id.grocery_row_in_shopcart);
		cb_inshoplist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				CheckBox cb = (CheckBox) view;
				boolean isChecked = cb.isChecked();
				
				// Get the row ID and grocery name from the parent view
				LinearLayout parentLayout = (LinearLayout) topView.findViewById(R.id.grocery_list_row_layout);
				TextView tv_id = (TextView) parentLayout.findViewById(R.id.grocery_row_id);
				TextView tv_cart_id = (TextView) parentLayout.findViewById(R.id.grocery_row_cart_item_id);
				TextView tv_name = (TextView) parentLayout.findViewById(R.id.grocery_row_label);
				
				// Toggle shoplist flag
				int shopListFlag;
				String displayMessage;
				
				if (isChecked == true) {
					shopListFlag = CartTable.FLAG_TRUE;
					displayMessage = mContext.getResources().getString(R.string.cart_shoplist_added);
				}
				else {
					shopListFlag = CartTable.FLAG_FALSE;
					displayMessage = mContext.getResources().getString(R.string.cart_shoplist_removed);
				}
				
				// Determine whether to insert, update, or delete the CartTable entry
				if (isChecked) {
					ContentValues values = new ContentValues();
					values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_id.getText().toString());
					values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
					values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
					values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);
					
					mContext.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
				}
				/*else if (existsInDatabase && watchListFlag==CartTable.FLAG_TRUE) {
					String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "=?";
					String[] selectionArgs = { tv_id.getText().toString() };
					activity.getContentResolver().update(GroceryotgProvider.CONTENT_URI_CART_ITEM, values, whereClause, selectionArgs);
				}*/
				else if (!isChecked) {
					String whereClause;
					String arg = "";
					
					if (tv_id.getText().toString() == "") {
						whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_ID + "=?";
						arg = tv_cart_id.getText().toString();
					} else {
						whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "=?";
						arg = tv_id.getText().toString();
					}
					
					String[] selectionArgs = { arg };
					mContext.getContentResolver().delete(GroceryotgProvider.CONTENT_URI_CART_ITEM, whereClause, selectionArgs);
				}
				
				// Restart the loader, refreshing all views
				GroceryOTGUtils.restartGroceryLoaders(mContext);
			}
		});
		cb_inshoplist.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if (isChecked) {
					button.setBackgroundColor(mContext.getResources().getColor(R.color.holo_blue_very_light));
				} else {
					button.setBackgroundColor(mContext.getResources().getColor(R.color.semi_transparent));
				}
				
			}
		});
		
		// Now add listeners for the expandable view's buttons
		ImageButton exp_mapButton = (ImageButton) topView.findViewById(R.id.expand_button_map);
		exp_mapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Go to the map view, filtering by the stores that contain this item
				LinearLayout parentLayout = (LinearLayout) topView.findViewById(R.id.grocery_list_row_layout);
				TextView text = (TextView) parentLayout.findViewById(R.id.grocery_row_store_id);
				ArrayList<Integer> ids = new ArrayList<Integer>();
				String list = text.getText().toString();
				if (!list.equals("")) {
					for (String s : list.split(",")) {
						ids.add(Integer.parseInt(s));
					}
				}
				
				Bundle extras = new Bundle();
				extras.putIntegerArrayList(MapFragmentActivity.EXTRA_FILTER_STORE, ids);
				
				Intent intent = new Intent(mContext, MapFragmentActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtras(extras);
				mContext.startActivity(intent);
			}
		});
		ImageButton exp_shareButton = (ImageButton) topView.findViewById(R.id.expand_button_share);
		exp_shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Share
				LinearLayout parentLayout = (LinearLayout) topView.findViewById(R.id.grocery_list_row_layout);
				TextView label = (TextView) parentLayout.findViewById(R.id.grocery_row_label);
				TextView price = (TextView) parentLayout.findViewById(R.id.grocery_row_price);
				TextView storeParent = (TextView) parentLayout.findViewById(R.id.grocery_row_store_parent_name);
				
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				String shareText = "";
				shareText += label.getText() + " is on sale";
				if (price.getText() != mContext.getString(R.string.no_price_available)) {
					shareText += " for " + price.getText();
				}
				if (storeParent.getText() != "") {
					shareText += " at " + storeParent.getText();
				}
				
				shareText += "! - via " + mContext.getString(R.string.app_name);
				
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
				shareIntent.setType("text/plain");
				mContext.startActivity(Intent.createChooser(shareIntent, "Share this sale"));
			}
		});
		
		ImageButton exp_flyerviewButton = (ImageButton) topView.findViewById(R.id.expand_button_flyerview);
		exp_flyerviewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// view flyers
				LinearLayout parentLayout = (LinearLayout) topView.findViewById(R.id.grocery_list_row_layout);
				TextView text = (TextView) parentLayout.findViewById(R.id.grocery_row_flyer_url);
				String url = text.getText().toString();
				
				if (url.equals("")) {
					// If there is no link, then don't do anything
					return;
				}
				
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				mContext.startActivity(intent);
			}
		});
		
		return topView;
	}

	public void setViewBinder(GroceryViewBinder groceryViewBinder) {
		super.setViewBinder(groceryViewBinder);
	}
}
