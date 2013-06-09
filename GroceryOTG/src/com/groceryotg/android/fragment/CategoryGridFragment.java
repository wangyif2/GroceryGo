package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.database.MatrixCursor;
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
import com.commonsware.cwac.merge.MergeAdapter;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class CategoryGridFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private GridView gridview;
    private MergeAdapter adapter;

    private final int INDEX_LOADER_CAT = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_fragment_list, container, false);
        gridview = (GridView) v.findViewById(R.id.gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                GroceryFragmentActivity.mPager.setCurrentItem(position + 1, true);
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
        adapter = new MergeAdapter();

        /*TODO: This is so much hack... need refactor as soon as possible*/
        String[] col = {CategoryTable.COLUMN_ID, CategoryTable.COLUMN_CATEGORY_NAME};
        MatrixCursor mc = new MatrixCursor(col);
        Object[] obj = {"0", "my flyer"};
        mc.addRow(obj);
        SimpleCursorAdapter sa1 = new MyFlyerGridCursorAdapter(getActivity(), R.layout.category_fragment_row, null, from, to);
        sa1.changeCursor(mc);
        adapter.addAdapter(sa1);

        SimpleCursorAdapter sa = new CategoryGridCursorAdapter(getActivity(), R.layout.category_fragment_row, null, from, to);
        adapter.addAdapter(sa);

        gridview.setAdapter(adapter);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {CategoryTable.COLUMN_ID, CategoryTable.COLUMN_CATEGORY_NAME};
        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_CAT, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        SimpleCursorAdapter sa = (SimpleCursorAdapter) adapter.getPieces().get(1);
        if (sa != null)
            sa.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        SimpleCursorAdapter sa = (SimpleCursorAdapter) adapter.getAdapter(1);
        sa.changeCursor(null);
    }
}
