package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.services.ServerURL;

import java.util.Arrays;
import java.util.List;

/**
 * User: robert
 * Date: 08/03/13
 */
public class GroceryViewBinder implements SimpleCursorAdapter.ViewBinder {

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

        int viewId = view.getId();

        if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_PRICE) 
        		&& viewId == R.id.grocery_row_price) {
            TextView textView = (TextView) view;
            if (cursor.getDouble(columnIndex) != 0) {
                textView.setText("$" + ServerURL.getGetDecimalFormat().format(cursor.getDouble(columnIndex)));
            } else {
                textView.setText("N/A");
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
        
        else if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_GROCERY_ID) 
        		&& viewId == R.id.grocery_row_inshopcart) {
        	Integer cartGroceryId = cursor.getInt(columnIndex);
        	ImageView img = (ImageView) view;
        	
        	if (cartGroceryId != 0) {
        		// Change the view to a highlighted star
        		img.setImageResource(R.drawable.ic_star_highlighted);
        	}
        	else {
        		// Display a non-highlighted star
        		img.setImageResource(R.drawable.ic_star);
        	}
        	return true;
        }	
        
        return false;
    }
}
