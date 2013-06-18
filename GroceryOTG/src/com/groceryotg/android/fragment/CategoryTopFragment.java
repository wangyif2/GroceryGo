package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import com.actionbarsherlock.app.SherlockFragment;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.utils.GroceryOTGUtils;

public class CategoryTopFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private Context mContext;
	private GridView gridview;
    private SimpleCursorAdapter mAdapter;

    private final int INDEX_LOADER_CAT = 0;
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	this.mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_fragment_list, container, false);
        gridview = (GridView) v.findViewById(R.id.gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	GroceryOTGUtils.launchGroceryPagerActivity(mContext, position);
            }
        });
        gridview.setEmptyView(v.findViewById(R.id.empty_category_list));

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData();
    }

    private void fillData() {
        String[] from = new String[]{CategoryTable.COLUMN_CATEGORY_NAME};
        int[] to = new int[]{R.id.category_row_label};

        getLoaderManager().initLoader(INDEX_LOADER_CAT, null, this);

        mAdapter = new CategoryTopCursorAdapter(getActivity(), R.layout.category_fragment_list_row, null, from, to);

        gridview.setAdapter(mAdapter);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {CategoryTable.COLUMN_ID, CategoryTable.COLUMN_CATEGORY_NAME};
        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_CAT, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        SimpleCursorAdapter sa = mAdapter;
        if (sa != null)
            sa.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        SimpleCursorAdapter sa = mAdapter;
        sa.changeCursor(null);
    }
}
