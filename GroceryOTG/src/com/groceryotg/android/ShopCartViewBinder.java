package com.groceryotg.android;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;


public class ShopCartViewBinder implements SimpleCursorAdapter.ViewBinder, ViewBinder {

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

        int viewId = view.getId();

        if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_FLAG_SHOPLIST) 
        		&& viewId == R.id.cart_row_inshoplist) {
        	Integer inShoplist = cursor.getInt(columnIndex);
        	ImageView img = (ImageView) view;
        	
        	if (inShoplist != 0) {
        		img.setImageResource(R.drawable.ic_flag_shoplist_highlight);
        	}
        	else {
        		img.setImageResource(R.drawable.ic_flag_shoplist);
        	}
        	return true;
        }	
        else if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_FLAG_SHOPLIST) 
        		&& viewId == R.id.cart_flag_shoplist) {
        	Integer inShoplist = cursor.getInt(columnIndex);
        	TextView flag = (TextView) view;
        	
        	if (inShoplist != 0) {
        		flag.setText("1");
        	}
        	else {
        		flag.setText("0");
        	}
        	return true;
        }	
        
        return false;
    }
}
