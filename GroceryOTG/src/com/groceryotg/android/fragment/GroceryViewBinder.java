package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.groceryotg.android.R;
import com.groceryotg.android.R.drawable;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.FlyerTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.utils.GroceryOTGUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GroceryViewBinder implements SimpleCursorAdapter.ViewBinder, ViewBinder {
	public GroceryViewBinder() {
	}
	
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

        int viewId = view.getId();

        if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_PRICE) 
        		&& viewId == R.id.grocery_row_price) {
            TextView textView = (TextView) view;
            if (cursor.getDouble(columnIndex) != 0) {
                textView.setText("$" + ServerURL.getGetDecimalFormat().format(cursor.getDouble(columnIndex)));
            } else {
                textView.setText(R.string.no_price_available);
            }
            return true;
        } 
        
        else if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME)
                && viewId == R.id.grocery_row_label) {
            String itemText = cursor.getString(columnIndex);
            String delim_period = ". ";
            String delim_comma = ", ";
            String regex_period = "\\.\\s";
            String regex_comma = ",\\s";
            if (itemText.indexOf(delim_period) != -1) {
                String[] itemArray = itemText.split(regex_period);
                itemText = itemArray[0];
            } 
            else if (itemText.indexOf(delim_comma) != -1) {
                String[] itemArray = itemText.split(regex_comma);
                itemText = itemArray[0];
            }

            TextView textView = (TextView) view;
            textView.setText(itemText);

            return true;
        } 
        else if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME)
                && viewId == R.id.grocery_row_details) {
            String itemText = cursor.getString(columnIndex);
            String itemDetails = "";
            String delim_period = ". ";
            String delim_comma = ", ";
            String regex_period = "\\.\\s";
            String regex_comma = ",\\s";
            
            if (itemText.indexOf(delim_period) != -1) {
                String[] itemArray = itemText.split(regex_period);
                List<String> itemList = Arrays.asList(itemArray);
                List<String> itemSublist = itemList.subList(1, itemList.size());
                itemText = itemArray[0];

                if (itemSublist.size() < 1) {
                	itemDetails = "";
                }
                else {
	                StringBuilder sb = new StringBuilder();
	                for (String s : itemSublist) {
	                    sb.append(s).append(delim_period);
	                }
	                sb.deleteCharAt(sb.length() - 1); // delete last delimiter
	                sb.deleteCharAt(sb.length() - 1);
	                itemDetails = sb.toString();
                }
            } 
            else if (itemText.indexOf(delim_comma) != -1) {
                String[] itemArray = itemText.split(regex_comma);
                List<String> itemList = Arrays.asList(itemArray);
                List<String> itemSublist = itemList.subList(1, itemList.size());
                itemText = itemArray[0];

                if (itemSublist.size() < 1) {
                	itemDetails = "";
                }
                else {
	                StringBuilder sb = new StringBuilder();
	                for (String s : itemSublist) {
	                    sb.append(s).append(delim_comma);
	                }
	                
	            	sb.deleteCharAt(sb.length() - 1); // delete last delimiter
	            	sb.deleteCharAt(sb.length() - 1);
	            	itemDetails = sb.toString();
                }
            }

            TextView textView = (TextView) view;
            textView.setText(itemDetails);

            return true;
        }
        else if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_FLAG_SHOPLIST) 
        		&& viewId == R.id.grocery_row_in_shopcart) {
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
        else if (columnIndex == cursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME) 
        		&& viewId == R.id.grocery_row_store_parent_name) {
        	String storeParentName = cursor.getString(columnIndex);
			TextView textView = (TextView) view;
			
			try {
				Class<drawable> res = R.drawable.class;
				Field field = res.getField("ic_store_" + storeParentName.toLowerCase(Locale.CANADA));
				textView.setText(storeParentName);
				textView.setTag(field.getInt(null));
			} catch (Exception e) {
				Log.e("GroceryOTG", "Could not get drawable id for row.", e);
			}
        	return true;
        }
        else if (columnIndex == cursor.getColumnIndex(FlyerTable.COLUMN_FLYER_ID) 
        		&& viewId == R.id.grocery_row_store_id) {
        	Integer id = cursor.getInt(columnIndex);
        	TextView text = (TextView) view;
        	Cursor flyerIDs = GroceryOTGUtils.getStoreFlyerIDs(view.getContext());
        	ArrayList<Integer> list = new ArrayList<Integer>();
        	
        	flyerIDs.moveToFirst();
        	while (!flyerIDs.isAfterLast()) {
        		if (flyerIDs.getInt(flyerIDs.getColumnIndex(StoreTable.COLUMN_STORE_FLYER)) == id) {
        			list.add(flyerIDs.getInt(flyerIDs.getColumnIndex(StoreTable.COLUMN_STORE_ID)));
        		}
        		flyerIDs.moveToNext();
        	}
        	
        	// Pack the list for store IDs into a string
        	StringBuilder sb = new StringBuilder();
        	for (Integer i : list) {
        		sb.append("," + Integer.toString(i));
        	}
        	if (list.size() > 0)
        		sb.replace(0, 1, "");
        	
        	text.setText(sb.toString());
        	
        	return true;
        }
        
        return false;
    }
}
