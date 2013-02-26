package com.groceryotg.android;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ShopCartDetailView extends Activity {
    private EditText mCartGroceryName;

    private Uri cartGroceryItemUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopcart_edit);

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

        ContentValues values = new ContentValues();
        values.put(CartTable.COLUMN_CART_GROCERY_NAME, name);

        if (cartGroceryItemUri == null) {
            cartGroceryItemUri = getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
        } else {
            getContentResolver().update(cartGroceryItemUri, values, null, null);
        }
    }

    private void makeToast() {
        Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
    }
}
