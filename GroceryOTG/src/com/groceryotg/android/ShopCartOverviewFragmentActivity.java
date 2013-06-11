package com.groceryotg.android;

import android.os.Bundle;
import android.view.KeyEvent;

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
        mSlidingMenu = GroceryOTGUtils.createSlidingMenu(this);
        GroceryOTGUtils.registerSlidingMenu(mSlidingMenu, this);
    }
	
    private void configActionBar() {
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
    	switch (keycode) {
    	case KeyEvent.KEYCODE_BACK:
    		if (mSlidingMenu.isMenuShowing()) {
    			mSlidingMenu.showContent();
    			return true;
    		}
    	}
    	return super.onKeyDown(keycode, e);
    }
}

