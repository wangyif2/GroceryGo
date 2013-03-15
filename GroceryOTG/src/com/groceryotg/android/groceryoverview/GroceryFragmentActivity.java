package com.groceryotg.android.groceryoverview;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.slidingmenu.lib.SlidingMenu;

import java.util.HashMap;

/**
 * User: robert
 * Date: 16/03/13
 */
public class GroceryFragmentActivity extends SherlockFragmentActivity {
    static HashMap<Integer, String> categories;

    GroceryAdapter mAdapter;
    ViewPager mPager;
    SlidingMenu slidingMenu;

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

        configSlidingMenu();
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

    private void configSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.menu_frame);

        // Populate the SlidingMenu
        String[] slidingMenuItems = new String[]{getString(R.string.slidingmenu_item_cat),
                getString(R.string.slidingmenu_item_cart),
                getString(R.string.slidingmenu_item_map),
                getString(R.string.slidingmenu_item_sync),
                getString(R.string.slidingmenu_item_settings),
                getString(R.string.slidingmenu_item_about)};

        ListView menuView = (ListView) findViewById(R.id.menu_items);
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this,
                R.layout.menu_item, android.R.id.text1, slidingMenuItems);
        menuView.setAdapter(menuAdapter);

        menuView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Switch activity based on what slidingMenu item the user selected
                TextView textView = (TextView) view;
                String selectedItem = textView.getText().toString();

                if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cat))) {
                    // Selected Categories
                    if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cart))) {
                    // Selected Shopping Cart
//                    launchShopCartActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_map))) {
                    // Selected Map
//                    launchMapActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_sync))) {
                    // Selected Sync
//                    refreshCurrentCategory();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_settings))) {
                    // Selected Settings
                    if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_about))) {
                    // Selected About
                    //startActivity(new Intent(CategoryOverView.this, About.class));
                    if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                }
            }
        });
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
