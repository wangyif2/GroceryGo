package ca.grocerygo.android.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ca.grocerygo.android.GroceryApplication;
import ca.grocerygo.android.R;
import ca.grocerygo.android.utils.GroceryRefreshTrigger;

/**
 * User: robert
 * Date: 21/06/13
 */
public class GroceryGCMBroadcastReceiver extends BroadcastReceiver {

    public static final String SETTINGS_IS_NEW_DATA_AVA = "isNewDataAvailable";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(GroceryApplication.TAG, context.getString(R.string.gcm_received));

        GroceryRefreshTrigger.enableRefresh(context);

    }

}
