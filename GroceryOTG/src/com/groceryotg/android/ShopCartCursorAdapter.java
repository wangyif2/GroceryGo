package com.groceryotg.android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartCursorAdapter extends SimpleCursorAdapter {
	Context context;
    Activity activity;
    
    @SuppressWarnings("deprecation")
	public ShopCartCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context=context;
        this.activity=(Activity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);
        
        CheckBox cb_inshoplist = (CheckBox) view.findViewById(R.id.cart_row_in_shopcart);
        cb_inshoplist.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View view)
			{
				CheckBox cb = (CheckBox) view;
				boolean isChecked = cb.isChecked();
				
            	// Get the row ID and grocery name from the parent view
            	LinearLayout layoutParent = (LinearLayout) cb.getParent().getParent();
            	TextView tv_id = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(0);
            	TextView tv_grocery_id = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(1);
            	TextView tv_name = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(2);
            	
            	// Toggle shoplist flag
            	int shopListFlag;
            	String displayMessage;
            	
            	if (isChecked) {
            		shopListFlag = CartTable.FLAG_TRUE;
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_added);
            	}
            	else {
            		shopListFlag = CartTable.FLAG_FALSE;
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_removed);
            	}
            	
            	ContentValues values = new ContentValues();
                values.put(CartTable.COLUMN_ID, tv_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_grocery_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
                values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
                values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);
                
                // Determine whether to insert, update, or delete the CartTable entry
                if (!isChecked) {
                	String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_ID + "=?";
                	String[] selectionArgs = { tv_id.getText().toString() };
                	activity.getContentResolver().delete(GroceryotgProvider.CONTENT_URI_CART_ITEM, whereClause, selectionArgs);
                }
                else {
                	String whereClause = CartTable.TABLE_CART + "." + CartTable.COLUMN_ID + "=?";
                	String[] selectionArgs = { tv_id.getText().toString() };
                	activity.getContentResolver().update(GroceryotgProvider.CONTENT_URI_CART_ITEM, values, whereClause, selectionArgs);
                }
                
                Toast t = Toast.makeText(activity, displayMessage, Toast.LENGTH_SHORT);
                t.show();
				
			}
        });

        return view;
    }

	public void setViewBinder(ShopCartViewBinder groceryViewBinder) {
		super.setViewBinder(groceryViewBinder);
	}
    
}
