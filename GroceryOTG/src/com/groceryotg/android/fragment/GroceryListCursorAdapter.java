package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
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
	Context context;
    Activity activity;
    public GroceryListCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
        this.context=context;
        this.activity=(Activity) context;
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
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_added);
            	}
            	else {
            		shopListFlag = CartTable.FLAG_FALSE;
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_removed);
            	}
            	
            	ContentValues values = new ContentValues();
                values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
                values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
                values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);
                
                boolean existsInDatabase = !isChecked;
                
                // Determine whether to insert, update, or delete the CartTable entry
                if (!existsInDatabase && isChecked) {
                	activity.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
                }
                /*else if (existsInDatabase && watchListFlag==CartTable.FLAG_TRUE) {
                	String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "=?";
                	String[] selectionArgs = { tv_id.getText().toString() };
                	activity.getContentResolver().update(GroceryotgProvider.CONTENT_URI_CART_ITEM, values, whereClause, selectionArgs);
                }*/
                else if (existsInDatabase && !isChecked) {
                	String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_ID + "=?";
                	String[] selectionArgs = { tv_id.getText().toString() };
                	activity.getContentResolver().delete(GroceryotgProvider.CONTENT_URI_CART_ITEM, whereClause, selectionArgs);
                }
                	
                Toast t = Toast.makeText(activity, displayMessage, Toast.LENGTH_SHORT);
                t.show();
				
			}
    	});
        
        ImageView icon_store = (ImageView) view.findViewById(R.id.grocery_row_store);
        icon_store.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	TextView text = (TextView) ((LinearLayout)v.getParent()).getChildAt(0);
            	ArrayList<Integer> ids = new ArrayList<Integer>();
            	String list = text.getText().toString();
            	if (!list.equals("")) {
	        		for (String s : list.split(",")) {
	        			ids.add(Integer.parseInt(s));
	        		}
        		}
        		
            	Bundle extras = new Bundle();
            	extras.putIntegerArrayList(MapFragmentActivity.EXTRA_FILTER_STORE, ids);
            	
        		Intent intent = new Intent(activity, MapFragmentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(extras);
                activity.startActivity(intent);
        	}
        });

        return view;
    }

	public void setViewBinder(GroceryViewBinder groceryViewBinder) {
		super.setViewBinder(groceryViewBinder);
	}
    
}
