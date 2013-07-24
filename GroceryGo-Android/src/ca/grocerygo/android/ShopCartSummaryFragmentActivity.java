package ca.grocerygo.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import ca.actionbarsherlock.app.SherlockFragmentActivity;
import ca.actionbarsherlock.view.Menu;
import ca.actionbarsherlock.view.MenuInflater;
import ca.actionbarsherlock.view.MenuItem;
import ca.grocerygo.android.utils.GroceryOTGUtils;

public class ShopCartSummaryFragmentActivity extends SherlockFragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.shopcart_summary_activity);
		
		GroceryOTGUtils.NavigationDrawerBundle drawerBundle = GroceryOTGUtils.configNavigationDrawer(this, false, R.string.title_cart);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.shopcart_summary_activity_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.mDrawerLayout != null && this.mDrawerList != null) {
			for (int i = 0; i < menu.size(); i++) {
				MenuItem item = menu.getItem(i);
				item.setVisible(!mDrawerLayout.isDrawerOpen(mDrawerList));
			}
		}
		return super.onPrepareOptionsMenu(menu);
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
	
}

