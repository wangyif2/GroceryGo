package com.groceryotg.android;

import com.groceryotg.android.database.CategoryTable;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CategoryGridCursorAdapter extends SimpleCursorAdapter {
	private Context mContext;
	private int mLayout;
	private int[] gridColours;
	
	@SuppressWarnings("deprecation")
	public CategoryGridCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        this.mContext = context;
        this.mLayout = layout;
        
        String[] allColours = mContext.getResources().getStringArray(R.array.colours);
        gridColours = new int[allColours.length];
        
        for (int i=0; i<allColours.length; i++) {
        	gridColours[i] = Color.parseColor(allColours[i]);
        }
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
        String categoryName = c.getString(c.getColumnIndex(colName));
        
        // Set the name of the next category in the grid view
        TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
        if (name_text != null) {
            name_text.setText(categoryName);
        }
        
        int position = c.getPosition();
        v.setBackgroundColor(gridColours[position % gridColours.length]);
    }

}
