package com.groceryotg.android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;


public class ShopCartCursorAdapter extends SimpleCursorAdapter {
	Context context;
    Activity activity;
    public ShopCartCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context=context;
        this.activity=(Activity) context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);
        long id = getItemId(position);
        ImageView icon_inshoplist = (ImageView) view.findViewById(R.id.cart_row_inshoplist);
        icon_inshoplist.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	// the "v" parameter represents the just-clicked button/image
            	ImageView icon = (ImageView) v.findViewById(R.id.cart_row_inshoplist);
            	
            	// These flags represent the state of the shoplist and watchlist icons *before* the user clicked
            	TextView tv_selected_shoplist = (TextView) ((LinearLayout)v.getParent()).getChildAt(0);
            	TextView tv_selected_watchlist = (TextView) ((LinearLayout)v.getParent()).getChildAt(2);
            	int shopListFlagBefore = tv_selected_shoplist.getText().toString().equalsIgnoreCase(((Integer)CartTable.FLAG_TRUE).toString()) ? 1 : 0;
            	int watchListFlagBefore = tv_selected_watchlist.getText().toString().equalsIgnoreCase(((Integer)CartTable.FLAG_TRUE).toString()) ? 1 : 0;
            	
            	// Get the row ID and grocery name from the parent view
            	LinearLayout layoutParent = (LinearLayout) v.getParent().getParent();
            	TextView tv_id = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(0);
            	TextView tv_grocery_id = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(1);
            	TextView tv_name = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(2);
            	
            	// Toggle shoplist flag, and keep watchlist flag the same as currently selected
            	int newImage, shopListFlag, watchListFlag;
            	String displayMessage;
            	
            	if (shopListFlagBefore == CartTable.FLAG_FALSE) {
            		newImage = R.drawable.ic_flag_shoplist_highlight;
            		shopListFlag = CartTable.FLAG_TRUE;
            		tv_selected_shoplist.setText(((Integer)CartTable.FLAG_TRUE).toString());
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_added);
            	}
            	else {
            		newImage = R.drawable.ic_flag_shoplist;
            		shopListFlag = CartTable.FLAG_FALSE;
            		tv_selected_shoplist.setText(((Integer)CartTable.FLAG_FALSE).toString());
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_removed);
            	}
            	watchListFlag = watchListFlagBefore;
            	icon.setImageResource(newImage);
            	
            	ContentValues values = new ContentValues();
                values.put(CartTable.COLUMN_ID, tv_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_grocery_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
                values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
                values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, watchListFlag);
                
                // Determine whether to insert, update, or delete the CartTable entry
                if (shopListFlag==CartTable.FLAG_FALSE && watchListFlag==CartTable.FLAG_FALSE) {
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
        
        ImageView icon_inwatchlist = (ImageView) view.findViewById(R.id.cart_row_inwatchlist);
        icon_inwatchlist.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) 
            {
        		// the "v" parameter represents the just-clicked button/image
            	ImageView icon = (ImageView) v.findViewById(R.id.cart_row_inwatchlist);
            	
            	// These flags represent the state of the shoplist and watchlist icons *before* the user clicked
            	TextView tv_selected_shoplist = (TextView) ((LinearLayout)v.getParent()).getChildAt(0);
            	TextView tv_selected_watchlist = (TextView) ((LinearLayout)v.getParent()).getChildAt(2);
            	int shopListFlagBefore = tv_selected_shoplist.getText().toString().equalsIgnoreCase(((Integer)CartTable.FLAG_TRUE).toString()) ? 1 : 0;
            	int watchListFlagBefore = tv_selected_watchlist.getText().toString().equalsIgnoreCase(((Integer)CartTable.FLAG_TRUE).toString()) ? 1 : 0;
            	
            	// Get the row ID and grocery name from the parent view
            	LinearLayout layoutParent = (LinearLayout) v.getParent().getParent();
            	TextView tv_id = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(0);
            	TextView tv_grocery_id = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(1);
            	TextView tv_name = (TextView)((LinearLayout) layoutParent.getChildAt(0)).getChildAt(2);
            	
            	// Toggle watchlist flag, and keep shoplist flag the same as currently selected
            	int newImage, shopListFlag, watchListFlag;
            	String displayMessage;
            	
            	if (watchListFlagBefore == CartTable.FLAG_FALSE) {
            		newImage = R.drawable.ic_flag_watchlist_highlight;
            		watchListFlag = CartTable.FLAG_TRUE;
            		tv_selected_watchlist.setText(((Integer)CartTable.FLAG_TRUE).toString());
            		displayMessage = context.getResources().getString(R.string.cart_watchlist_added);
            	}
            	else {
            		newImage = R.drawable.ic_flag_watchlist;
            		watchListFlag = CartTable.FLAG_FALSE;
            		tv_selected_watchlist.setText(((Integer)CartTable.FLAG_FALSE).toString());
            		displayMessage = context.getResources().getString(R.string.cart_watchlist_removed);
            	}
            	shopListFlag = shopListFlagBefore;
            	icon.setImageResource(newImage);
            	
            	ContentValues values = new ContentValues();
            	values.put(CartTable.COLUMN_ID, tv_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_grocery_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
                values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
                values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, watchListFlag);
                
                // Determine whether to insert, update, or delete the CartTable entry
                if (shopListFlag==CartTable.FLAG_FALSE && watchListFlag==CartTable.FLAG_FALSE) {
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
