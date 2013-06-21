package com.groceryotg.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.groceryotg.android.fragment.ShopCartAddTabCodeFragment;
import com.groceryotg.android.fragment.ShopCartAddTabTextFragment;
import com.groceryotg.android.fragment.ShopCartAddTabVoiceFragment;
import com.groceryotg.android.utils.GroceryOTGUtils;

public class ShopCartAddFragmentActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBar mActionBar;
	
	private final String SAVE_INSTANCE_TAB_TITLE = "save_instance_tab_title";
	private int mActionBarTab;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i("GroceryOTG", "Creating ShopCartAddFragmentActivity");
		
		setContentView(R.layout.shopcart_add_activity);
		
		GroceryOTGUtils.NavigationDrawerBundle drawerBundle = GroceryOTGUtils.configNavigationDrawer(this, false, R.string.title_cart_detail);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();
		
		// set up action bar tabs
		mActionBar = getSupportActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mActionBar.addTab(mActionBar.newTab().setText(R.string.title_cart_add_tab_text).setTabListener(this));
		mActionBar.addTab(mActionBar.newTab().setText(R.string.title_cart_add_tab_voice).setTabListener(this));
		mActionBar.addTab(mActionBar.newTab().setText(R.string.title_cart_add_tab_code).setTabListener(this));
		
		mActionBarTab = 0;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(SAVE_INSTANCE_TAB_TITLE, mActionBarTab);
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    mActionBarTab = savedInstanceState.getInt(SAVE_INSTANCE_TAB_TITLE);
	    mActionBar.setSelectedNavigationItem(mActionBarTab);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.shopcart_add_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (mDrawerLayout.isDrawerOpen(mDrawerList))
					mDrawerLayout.closeDrawer(mDrawerList);
				else {
					// Specify the parent activity
					Intent parentActivityIntent = new Intent(this, ShopCartOverviewFragmentActivity.class);
					parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
												Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(parentActivityIntent);
					this.finish();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		String title = (String) tab.getText();
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.content).getWindowToken(), 0);
		
		if (title == getString(R.string.title_cart_add_tab_text)) {
			ft.replace(R.id.content, new ShopCartAddTabTextFragment());
			mActionBarTab = 0;
		} else if (title == getString(R.string.title_cart_add_tab_voice)) {
			ft.replace(R.id.content, new ShopCartAddTabVoiceFragment());
			mActionBarTab = 1;
		} else if (title == getString(R.string.title_cart_add_tab_code)) {
			ft.replace(R.id.content, new ShopCartAddTabCodeFragment());
			mActionBarTab = 2;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
	
	// Need to add this here since the ZXing library creates a new activity from activity, not fragment
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			String text = scanResult.getContents();
			ShopCartAddTabCodeFragment frag = (ShopCartAddTabCodeFragment) getSupportFragmentManager().findFragmentById(R.id.content);
			frag.setCode(text);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}

