package com.groceryotg.android;

import android.os.Bundle;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ShopCartOverviewFragmentActivity extends SherlockFragmentActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.shopcart_top);
        
        configActionBar();
    }
	
    private void configActionBar() {
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
    	return super.onKeyDown(keycode, e);
    }
}

