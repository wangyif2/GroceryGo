package com.groceryotg.android.groceryoverview;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

import java.util.HashMap;

/**
 * User: robert
 * Date: 16/03/13
 */
public class GroceryFragmentActivity extends SherlockFragmentActivity {
    static HashMap<Integer, String> categories;

    GroceryAdapter mAdapter;

    ViewPager mPager;

    private Uri groceryUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_pager);

        Bundle extras = getIntent().getExtras();

        categories = getCategoryInfo();


        mAdapter = new GroceryAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
    }

    private HashMap<Integer, String> getCategoryInfo() {
        HashMap<Integer, String> categories = new HashMap<Integer, String>();
        Cursor c = getContentResolver().query(GroceryotgProvider.CONTENT_URI_CAT, null, null, null, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            categories.put(
                    c.getInt(c.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_ID)),
                    c.getString(c.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_NAME)));
            c.moveToNext();
        }
        return categories;
    }

    public static class GroceryAdapter extends FragmentStatePagerAdapter {

        public GroceryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "categories";
            } else
                return categories.get(position);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return new CategoryGridFragment();
            } else {
                return GroceryListFragment.newInstance(i);
            }
        }

        @Override
        public int getCount() {
            return categories.size();
        }
    }
}
