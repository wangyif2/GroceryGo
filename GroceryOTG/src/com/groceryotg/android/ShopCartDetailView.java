package com.groceryotg.android;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ShopCartDetailView extends Activity {
    private EditText mCartGroceryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopcart_edit);

        mCartGroceryName = (EditText) findViewById(R.id.cart_grocery_edit_name);
        Button confirmButton = (Button) findViewById(R.id.cart_confirm_button);

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

    private void makeToast() {
        Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
    }
}
