package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;

public class ShopCartViewBinder implements SimpleCursorAdapter.ViewBinder, ViewBinder {

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

		int viewId = view.getId();

		if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_FLAG_SHOPLIST) 
				&& viewId == R.id.cart_row_in_shopcart) {
			Integer inShoplist = cursor.getInt(columnIndex);
			CheckBox cb = (CheckBox) view;
			
			if (inShoplist != 0) {
				cb.setChecked(true);
			}
			else {
				cb.setChecked(false);
			}
			return true;
		}	
		
		return false;
	}
}
