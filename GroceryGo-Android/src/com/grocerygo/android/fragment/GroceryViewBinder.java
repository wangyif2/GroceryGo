package com.grocerygo.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.grocerygo.android.GroceryApplication;
import com.grocerygo.android.R;
import com.grocerygo.android.database.CartTable;
import com.grocerygo.android.database.FlyerTable;
import com.grocerygo.android.database.GroceryTable;
import com.grocerygo.android.database.StoreParentTable;
import com.grocerygo.android.utils.ServerURLs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GroceryViewBinder implements SimpleCursorAdapter.ViewBinder, ViewBinder {
	private Context mContext;
	private Map<String, Integer> mStoreParentIconMap;
	private SparseArray<ArrayList<Integer>> mFlyerStoreMap;
	
	public GroceryViewBinder(Context context) {
		this.mContext = context;
		this.mStoreParentIconMap = ((GroceryApplication) ((Activity) context).getApplication()).getStoreParentIconMap();
		this.mFlyerStoreMap = ((GroceryApplication) ((Activity) context).getApplication()).getFlyerStoreMap();
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

		int viewId = view.getId();

		if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_PRICE) 
				&& viewId == R.id.grocery_row_price) {
			TextView textView = (TextView) view;
			if (cursor.getDouble(columnIndex) != 0) {
				textView.setText("$" + ServerURLs.getGetDecimalFormat().format(cursor.getDouble(columnIndex)));
			} else {
				textView.setText(R.string.no_price_available);
			}
			return true;
		}
		else if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_EXPIRY) 
				&& viewId == R.id.grocery_row_expiry) {
			
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String expiryDate = formatter.format(new Date(Long.valueOf(cursor.getString(columnIndex))));
			
			TextView textView = (TextView) view;
			textView.setText("Ends: " + expiryDate);
			return true;
		}
		else if (columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_ID)
				&& viewId == R.id.grocery_row_id) {
			String itemText = cursor.getString(columnIndex);
			TextView textView = (TextView) view;
			
			if (itemText == null) {
				textView.setText("");
				return true;
			}
			
			textView.setText(itemText);
	
			return true;
		}
		else if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_GROCERY_ID)
				&& viewId == R.id.grocery_row_cart_item_id) {
			String itemText = cursor.getString(columnIndex);
			TextView textView = (TextView) view;
			
			if (itemText == null) {
				textView.setText("");
				return true;
			}
			
			textView.setText(itemText);
	
			return true;
		}
		else if ((columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME)
					|| columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_GROCERY_NAME))
					&& viewId == R.id.grocery_row_label) {
			String itemText = cursor.getString(columnIndex);
			TextView textView = (TextView) view;
			
			if (itemText == null) {
				return true;
			}
			
			itemText = cursor.getString(columnIndex);
			String delim_period = ". ";
			String delim_comma = ", ";
			String regex_period = "\\.\\s";
			String regex_comma = ",\\s";
			if (itemText.indexOf(delim_period) != -1) {
				String[] itemArray = itemText.split(regex_period);
				itemText = itemArray[0];
			} else if (itemText.indexOf(delim_comma) != -1) {
				String[] itemArray = itemText.split(regex_comma);
				itemText = itemArray[0];
			}
			
			textView.setText(itemText);

			return true;
		}
		else if ((columnIndex == cursor.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME)
					|| columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_GROCERY_NAME))
					&& viewId == R.id.grocery_row_details) {
			String itemText = cursor.getString(columnIndex);
			String itemDetails = "";
			TextView textView = (TextView) view;
			
			if (itemText == null) {
				return true;
			}
			
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
			
			textView.setText(itemDetails);

			return true;
		}
		else if (columnIndex == cursor.getColumnIndex(CartTable.COLUMN_CART_FLAG_SHOPLIST) 
				&& viewId == R.id.grocery_row_in_shopcart) {
			Integer inShoplist = cursor.getInt(columnIndex);
			CheckBox cb = (CheckBox) view;
			
			if (inShoplist != 0) {
				cb.setChecked(true);
				cb.setBackgroundColor(mContext.getResources().getColor(R.color.holo_blue_very_light));
			}
			else {
				cb.setChecked(false);
				cb.setBackgroundColor(mContext.getResources().getColor(R.color.semi_transparent));
			}
			
			return true;
		}
		else if (columnIndex == cursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME) 
				&& viewId == R.id.grocery_row_store_parent_name) {
			String storeParentName = cursor.getString(columnIndex);
			TextView textView = (TextView) view;
			
			if (storeParentName == null) {
				textView.setText("");
				textView.setTag(null);
				return true;
			}
			
			textView.setText(storeParentName);
			textView.setTag(mStoreParentIconMap.get(storeParentName));
			
			return true;
		}
		else if (columnIndex == cursor.getColumnIndex(FlyerTable.COLUMN_FLYER_ID) 
				&& viewId == R.id.grocery_row_store_id) {
			String itemText = cursor.getString(columnIndex);
			TextView text = (TextView) view;
			
			if (itemText == null) {
				text.setText("");
				return true;
			}
			
			ArrayList<Integer> list = mFlyerStoreMap.get(Integer.parseInt(itemText));
			
			if (list == null) {
				text.setText("");
				return true;
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