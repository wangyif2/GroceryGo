package com.groceryotg.android;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.groceryotg.android.utils.GroceryOTGUtils;
import com.slidingmenu.lib.SlidingMenu;

public class ShopCartOverviewFragmentActivity extends SherlockFragmentActivity {
    private SlidingMenu mSlidingMenu;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.shopcart_top);
        
        configActionBar();
        mSlidingMenu = GroceryOTGUtils.configSlidingMenu(this);
    }
	
    private void configActionBar() {
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

