package com.grocerygo.android.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.grocerygo.android.R;
import com.grocerygo.android.database.CategoryTable;

import java.util.Locale;

public class CategoryTopCursorAdapter extends SimpleCursorAdapter {
	protected Context mContext;
	private int mLayout;
	private int[] gridColours;
	private TypedArray gridIcons;

	@SuppressWarnings("deprecation")
	public CategoryTopCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.mContext = context;
		this.mLayout = layout;

		// An array of colours for solid background fill
		String[] allColours = mContext.getResources().getStringArray(R.array.colours);
		gridColours = new int[allColours.length];
		for (int i = 0; i < allColours.length; i++) {
			gridColours[i] = Color.parseColor(allColours[i]);
		}

		// An array of icons (to use instead of colours, when available)
		gridIcons = mContext.getResources().obtainTypedArray(R.array.icons_arr);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		Cursor c = getCursor();
		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(mLayout, parent, false);

		// Get the next row from the cursor
		String colName = CategoryTable.COLUMN_CATEGORY_NAME;
		String categoryName = c.getString(c.getColumnIndex(colName));

		// Set the name of the next category in the grid view
		TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
		if (name_text != null) {
			name_text.setText(categoryName);
		}

		return v;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		// Get the next row from the cursor
		String colName = CategoryTable.COLUMN_CATEGORY_NAME;
		String categoryName = c.getString(c.getColumnIndex(colName)).toLowerCase(Locale.CANADA);

		// TODO: Update category names in database
		if (categoryName.equalsIgnoreCase("Miscellaneous")) {
			categoryName = "misc";
		} else if (categoryName.equalsIgnoreCase("Fruits and Vegetables")) {
			categoryName = "fruit & veg";
		} else if (categoryName.equalsIgnoreCase("Bread and Bakery")) {
			categoryName = "bread";
		} else if (categoryName.equalsIgnoreCase("Beverages")) {
			categoryName = "drinks";
		}


		// Set the name of the next category in the grid view
		TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
		if (name_text != null) {
			name_text.setText(categoryName);
		}

		int position = c.getPosition();

		if (position < gridIcons.length()) {
			v.setBackgroundResource(gridIcons.getResourceId(position % gridIcons.length(), 0));
		} else {
			v.setBackgroundColor(gridColours[position % gridColours.length]);
		}
	}

	// TODO: When should this be called?
	public void cleanUp() {
		// Recycle the obtained type array when done using the adapter
		gridIcons.recycle();
	}
}
