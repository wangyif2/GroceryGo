package com.groceryotg.android;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartDetailActivity extends SherlockActivity {
    private EditText mCartGroceryName;

    private Uri cartGroceryItemUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopcart_fragment_edit_dialog);

        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mCartGroceryName = (EditText) findViewById(R.id.cart_grocery_edit_name);
        Button confirmButton = (Button) findViewById(R.id.cart_confirm_button);

        Bundle extras = getIntent().getExtras();

        cartGroceryItemUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CART_ITEM);

        if (extras != null) {
            cartGroceryItemUri = extras.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CART_ITEM);
            fillData(cartGroceryItemUri);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mCartGroceryName.getText().toString())) {
                    makeToast();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	// This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.
            	
            	// Specify the parent activity
            	Intent parentActivityIntent = new Intent(this, ShopCartOverviewFragmentActivity.class);
            	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
            								Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(parentActivityIntent);
            	finish();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillData(Uri cartGroceryItemUri) {
        String[] projection = {CartTable.COLUMN_CART_GROCERY_NAME};
        Cursor cursor = getContentResolver().query(cartGroceryItemUri, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            mCartGroceryName.setText(cursor.getString(cursor.getColumnIndexOrThrow(CartTable.COLUMN_CART_GROCERY_NAME)));
            cursor.close();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CART_ITEM, cartGroceryItemUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    private void saveState() {
        String name = mCartGroceryName.getText().toString();

        if (!name.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(CartTable.COLUMN_CART_GROCERY_NAME, name);
            values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, CartTable.FLAG_TRUE);
            values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);

            if (cartGroceryItemUri == null) {
                cartGroceryItemUri = getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
            } else {
                getContentResolver().update(cartGroceryItemUri, values, null, null);
            }
        }
    }

    private void makeToast() {
        Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
    }
}
