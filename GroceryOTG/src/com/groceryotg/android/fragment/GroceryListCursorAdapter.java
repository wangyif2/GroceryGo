package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
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


public class GroceryListCursorAdapter extends SimpleCursorAdapter {
	Context context;
    Activity activity;
    public GroceryListCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.context=context;
        this.activity=(Activity) context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);
        long id = getItemId(position);
        ImageView icon_inshoplist = (ImageView) view.findViewById(R.id.grocery_row_inshopcart);
        icon_inshoplist.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	ImageView icon = (ImageView) v.findViewById(R.id.grocery_row_inshopcart);
            	TextView tv_selected_shoplist = (TextView) ((LinearLayout)v.getParent()).getChildAt(0);
            	TextView tv_selected_watchlist = (TextView) ((LinearLayout)v.getParent()).getChildAt(2);
            	
            	// Get parent view
            	TableLayout tableParent = (TableLayout) v.getParent().getParent().getParent();
            	TextView tv_id = (TextView)((LinearLayout)((TableRow) tableParent.getChildAt(0)).getChildAt(0)).getChildAt(0);
            	TextView tv_name = (TextView)((LinearLayout)((TableRow) tableParent.getChildAt(0)).getChildAt(0)).getChildAt(1);
            	
            	// Toggle shoplist flag, and keep watchlist flag the same as currently selected
            	int newImage, shopListFlag, watchListFlag;
            	String displayMessage;
            	
            	if (tv_selected_shoplist.getText().toString().equalsIgnoreCase(((Integer)CartTable.FLAG_FALSE).toString())) {
            		newImage = R.drawable.ic_star_highlighted;
            		shopListFlag = CartTable.FLAG_TRUE;
            		tv_selected_shoplist.setText(((Integer)CartTable.FLAG_TRUE).toString());
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_added);
            	}
            	else {
            		newImage = R.drawable.ic_star;
            		shopListFlag = CartTable.FLAG_FALSE;
            		tv_selected_shoplist.setText(((Integer)CartTable.FLAG_FALSE).toString());
            		displayMessage = context.getResources().getString(R.string.cart_shoplist_removed);
            	}
            	watchListFlag = (tv_selected_watchlist.getText().toString().equalsIgnoreCase(((Integer)CartTable.FLAG_FALSE).toString()) ? CartTable.FLAG_FALSE : CartTable.FLAG_TRUE);
            	icon.setImageResource(newImage);
            	
            	ContentValues values = new ContentValues();
                values.put(CartTable.COLUMN_CART_GROCERY_ID, tv_id.getText().toString());
                values.put(CartTable.COLUMN_CART_GROCERY_NAME, tv_name.getText().toString());
                values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, shopListFlag);
                values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, watchListFlag);
                
                activity.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
                Toast t = Toast.makeText(activity, displayMessage, Toast.LENGTH_SHORT);
                t.show();
                
            	//Log.d("CUSTOMADAPTER", "Clicked on shopcart icon, row_id=" + tv_id.getText().toString());
            }
        });
        
        ImageView icon_inwatchlist = (ImageView) view.findViewById(R.id.grocery_row_inwatchlist);
        icon_inwatchlist.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) 
            {
            	Log.d("CUSTOMADAPTER", "Clicked on watchlist icon");
            }
        });

        return view;
    }

	public void setViewBinder(GroceryViewBinder groceryViewBinder) {
		super.setViewBinder(groceryViewBinder);
	}
    
}
