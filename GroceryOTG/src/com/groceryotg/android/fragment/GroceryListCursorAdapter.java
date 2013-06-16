package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.groceryotg.android.MapFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

import java.util.ArrayList;

public class GroceryListCursorAdapter extends SimpleCursorAdapter {
	Context mContext;
    Activity mActivity;
    public GroceryListCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
        this.mContext=context;
        this.mActivity=(Activity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);
        
        CheckBox cb_inshoplist = (CheckBox) view.findViewById(R.id.grocery_row_in_shopcart);
        cb_inshoplist.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				CheckBox cb = (CheckBox) view;
				boolean isChecked = cb.isChecked();
				
            	// Get the row ID and grocery name from the parent view
				LinearLayout parentLayout = (LinearLayout) cb.getParent().getParent();
				TextView tv_id = (TextView) parentLayout.findViewById(R.id.grocery_row_id);
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
            	
            	ContentValues values = new ContentValues();
                values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
                values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
                values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);
                
                boolean existsInDatabase = !isChecked;
                
                // Determine whether to insert, update, or delete the CartTable entry
                if (!existsInDatabase && isChecked) {
                	mActivity.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
                }
                /*else if (existsInDatabase && watchListFlag==CartTable.FLAG_TRUE) {
                	String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "=?";
                	String[] selectionArgs = { tv_id.getText().toString() };
                	activity.getContentResolver().update(GroceryotgProvider.CONTENT_URI_CART_ITEM, values, whereClause, selectionArgs);
                }*/
                else if (existsInDatabase && !isChecked) {
                	String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "=?";
                	String[] selectionArgs = { tv_id.getText().toString() };
                	mActivity.getContentResolver().delete(GroceryotgProvider.CONTENT_URI_CART_ITEM, whereClause, selectionArgs);
                }
                	
                Toast t = Toast.makeText(mActivity, displayMessage, Toast.LENGTH_SHORT);
                t.show();
				
			}
    	});
        
        // Now add listeners for the expandable view's buttons
        ImageButton exp_mapButton = (ImageButton) view.findViewById(R.id.expand_button_map);
        exp_mapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Go to the map view, filtering by the stores that contain this item
				TextView text = (TextView) ((LinearLayout) v.getParent().getParent().getParent()).findViewById(R.id.grocery_row_store_id);
				ArrayList<Integer> ids = new ArrayList<Integer>();
				String list = text.getText().toString();
				if (!list.equals("")) {
					for (String s : list.split(",")) {
						ids.add(Integer.parseInt(s));
					}
				}
				
				Bundle extras = new Bundle();
				extras.putIntegerArrayList(MapFragmentActivity.EXTRA_FILTER_STORE, ids);
				
				Intent intent = new Intent(mActivity, MapFragmentActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtras(extras);
				mActivity.startActivity(intent);
			}
        });
        ImageButton exp_shareButton = (ImageButton) view.findViewById(R.id.expand_button_share);
        exp_shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Share
				Log.i("GroceryOTG", "The share button was pressed");
				
				TextView label = (TextView) ((LinearLayout) v.getParent().getParent().getParent()).findViewById(R.id.grocery_row_label);
				TextView price = (TextView) ((LinearLayout) v.getParent().getParent().getParent()).findViewById(R.id.grocery_row_price);
				TextView storeParent = (TextView) ((LinearLayout) v.getParent().getParent().getParent()).findViewById(R.id.grocery_row_store);
				
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				String shareText = "";
				shareText += label.getText() + " is on sale";
				if (price.getText() != mActivity.getString(R.string.no_price_available)) {
					shareText += " for " + price.getText();
        		}
				shareText += " at " + storeParent.getText() + "! - via " + mActivity.getString(R.string.app_name);
				
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
				shareIntent.setType("text/plain");
				mActivity.startActivity(Intent.createChooser(shareIntent, "Share this sale"));
			}
        });
        
        ImageButton exp_flyerviewButton = (ImageButton) view.findViewById(R.id.expand_button_flyerview);
        exp_flyerviewButton.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// view flyers
				TextView text = (TextView) ((LinearLayout) v.getParent().getParent().getParent()).findViewById(R.id.grocery_row_flyer_url);
				String url = text.getText().toString();
				
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				mActivity.startActivity(intent);
			}
        });
        
        return view;
    }

	public void setViewBinder(GroceryViewBinder groceryViewBinder) {
		super.setViewBinder(groceryViewBinder);
	}
    
}
