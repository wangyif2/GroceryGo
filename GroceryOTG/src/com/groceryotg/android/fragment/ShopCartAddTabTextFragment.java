package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartAddTabTextFragment extends SherlockFragment {
	private Context mContext;
	
    private EditText mCartGroceryName;

    private Uri cartGroceryItemUri;

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	this.mContext = activity;
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.shopcart_add_tab_text, container, false);
    	
        mCartGroceryName = (EditText) v.findViewById(R.id.cart_grocery_edit_name);
        Button confirmButton = (Button) v.findViewById(R.id.cart_confirm_button);

        Bundle extras = ((Activity) mContext).getIntent().getExtras();

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
                    //setResult(Activity.RESULT_OK);
                    saveState();
                    ((Activity) mContext).finish();
                }
            }
        });
        
        return v;
    }
    
    private void fillData(Uri cartGroceryItemUri) {
        String[] projection = {CartTable.COLUMN_CART_GROCERY_NAME};
        Cursor cursor = mContext.getContentResolver().query(cartGroceryItemUri, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            mCartGroceryName.setText(cursor.getString(cursor.getColumnIndexOrThrow(CartTable.COLUMN_CART_GROCERY_NAME)));
            cursor.close();
        }
    }

    private void makeToast() {
        Toast.makeText(mContext, "Please enter a name", Toast.LENGTH_LONG).show();
    }
    
    private void saveState() {
        String name = mCartGroceryName.getText().toString();

        if (!name.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(CartTable.COLUMN_CART_GROCERY_NAME, name);
            values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, CartTable.FLAG_TRUE);
            values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);

            if (cartGroceryItemUri == null) {
                cartGroceryItemUri = mContext.getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);
            } else {
            	mContext.getContentResolver().update(cartGroceryItemUri, values, null, null);
            }
        }
    }
}
