package com.groceryotg.android.fragment;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CategoryTable;

import java.util.Locale;

/**
 * User: robert
 * Date: 27/03/13
 */
public class MyFlyerGridCursorAdapter extends CategoryGridCursorAdapter {

    public MyFlyerGridCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
    }


    @Override
    public void bindView(View v, Context context, Cursor c) {
        // Get the next row from the cursor
        String colName = CategoryTable.COLUMN_CATEGORY_NAME;
        String categoryName = c.getString(c.getColumnIndex(colName)).toLowerCase(Locale.CANADA);

        // Set the name of the next category in the grid view
        TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
        if (name_text != null) {
            name_text.setText(categoryName);
        }
        v.setBackgroundResource(mContext.getResources().getIdentifier("icon_myflyer", "drawable", mContext.getPackageName()));
    }
}
