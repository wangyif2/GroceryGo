package com.groceryotg.android;

import android.app.Activity;
import android.content.*;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ProgressBar;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.gcm.GCMServerUtils;
import com.groceryotg.android.gcm.GCMUtils;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.services.location.LocationServiceReceiver;
import com.groceryotg.android.settings.SettingsManager;
import com.groceryotg.android.utils.GroceryOTGUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SplashScreenActivity extends Activity {
	public static final String BROADCAST_ACTION_UPDATE_PROGRESS = "com.groceryotg.android.intent_action_update_progress_bar";
	public static final String BROADCAST_ACTION_UPDATE_PROGRESS_INCREMENT = "intent_action_update_progres_increment";
	
	private static final String SETTINGS_IS_DB_POPULATED = "isDBPopulated";
	private static final String SETTINGS_IS_LOCATION_FOUND = "isLocationFound";
	// used to know if the back button was pressed in the splash screen activity
	// and avoid opening the next activity
	private boolean mIsBackButtonPressed;
	private static final int SPLASH_DURATION = 10; // 10 milliseconds
	
	private Context mContext;
	
	private String mLocalizationWarningDialogIntentExtra = null;

	private RefreshStatusReceiver mRefreshStatusReceiver;
	private static final int PROGRESS_MAX = 100;
	
	private ProgressBar mProgressBar = null;
	private BroadcastReceiver mProgressReceiver;

    //GCM variables
    GoogleCloudMessaging gcm;
    String regid;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen_activity);
		
		this.mContext = this;
		
		// Load the default preferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Initialize the database tables if they aren't created
		getContentResolver().query(GroceryotgProvider.CONTENT_URI_CAT, null, null, null, null);

		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mRefreshStatusReceiver = new RefreshStatusReceiver();
		IntentFilter mStatusIntentFilter = new IntentFilter(NetworkHandler.REFRESH_COMPLETED_ACTION);
		mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshStatusReceiver, mStatusIntentFilter);
		
		mProgressReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(BROADCAST_ACTION_UPDATE_PROGRESS)) {
					int inc = intent.getExtras().getInt(BROADCAST_ACTION_UPDATE_PROGRESS_INCREMENT);
					if (inc > 0 && mProgressBar != null)
						mProgressBar.incrementProgressBy(inc);
				}
			}
		};
		IntentFilter mProgressIntentFilter = new IntentFilter(BROADCAST_ACTION_UPDATE_PROGRESS);
		mProgressIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(this).registerReceiver(mProgressReceiver, mProgressIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshStatusReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mProgressReceiver);
	}

    private void init() {
		configProgressBar();
        
        configGCM();

//		configLocale();
        configDatabase();
		
		configDefaultSettings();
	}
	
	private void configProgressBar() {
		// creates a progress bar from 0-100
		mProgressBar = (ProgressBar)findViewById(R.id.loading_progress_bar);
		mProgressBar.setProgress(0);
		mProgressBar.setMax(PROGRESS_MAX);
	}

    private void configGCM() {
        final String regId = GCMUtils.getRegistrationId(mContext);

        if (regId.length() == 0) {
            registerGCM();
        }
        else {
            if (!GCMRegistrar.isRegisteredOnServer(mContext)) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        GCMServerUtils.register(mContext, regId);
                        return null;
                    }
                }.execute(null, null, null);
            }
        }
        gcm = GoogleCloudMessaging.getInstance(mContext);
    }

    private void registerGCM() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... String) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regid = gcm.register(GCMUtils.SENDER_ID);
                    msg = "Device registered, registration id=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the message
                    // using the 'from' address in the message.

                    // Save the regid - no need to register again.
                    GCMUtils.setRegistrationId(mContext, regid);

                    GCMServerUtils.register(mContext, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(GroceryApplication.TAG, msg);
            }
        }.execute(null, null, null);
    }


    private void configLocale() {
		SharedPreferences settings = getPreferences(0);
		boolean isLocationFound = settings.getBoolean(SETTINGS_IS_LOCATION_FOUND, false);
		
		if (!isLocationFound) {
			// configure locale settings
			Location lastKnownLocation = GroceryOTGUtils.getLastKnownLocation(mContext);
			
			Geocoder gcd = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = new ArrayList<Address>();
			try {
				addresses = gcd.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
			} catch (Exception e) {
				Log.i("GroceryOTG", "Could not get location");
				e.printStackTrace();
			}
			if (addresses.size() > 0) {
				String locality = addresses.get(0).getLocality();
				String adminArea = addresses.get(0).getAdminArea();
				String countryCode = addresses.get(0).getCountryCode();
				
				SharedPreferences.Editor settingsEditor = settings.edit();
				settingsEditor.putBoolean(SETTINGS_IS_LOCATION_FOUND, true);
				settingsEditor.commit();
				
				// TODO: Have a db of supported areas
				Log.i("GroceryOTG", locality + "." + adminArea + "." + countryCode);
				if (locality.equals("Toronto") && adminArea.equals("Ontario") && countryCode.equals("CA")) {
					// If the location is among supported areas, then populate the db
					configDatabase();
				} else {
					// If is not supported, then warn the user their location is not supported fully
					mLocalizationWarningDialogIntentExtra = CategoryTopFragmentActivity.INTENT_EXTRA_FLAG_LOCATION_NOT_SUPPORTED;
					configHandler();
				}
			} else {
				// If the location service isn't working, then warn the user
				mLocalizationWarningDialogIntentExtra = CategoryTopFragmentActivity.INTENT_EXTRA_FLAG_LOCATION_SERVICE_BAD;
				configHandler();
				// switch when using emulator
				//configDatabase();
			}
		} else {
			configDatabase();
		}
	}

	private void configDatabase() {
		SharedPreferences settings = getPreferences(0);
		boolean isDBPopulated = settings.getBoolean(SETTINGS_IS_DB_POPULATED, false);
		
		if (ServerURL.checkNetworkStatus(getBaseContext()) && !isDBPopulated) {
			populateCategory();
            Log.i(GroceryApplication.TAG, "Cat poped");
            populateStoreParent();
            Log.i(GroceryApplication.TAG, "StorePar poped");
            populateStore();
            Log.i(GroceryApplication.TAG, "Store poped");
            populateFlyer();
            Log.i(GroceryApplication.TAG, "Flyer poped");
            populateGrocery();
            Log.i(GroceryApplication.TAG, "Grocery poped");
        } else {
			configHandler();
		}
	}
	
	private void configDefaultSettings() {
		// Sets up up the settings defaults
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
	
	private void configLocationPoll() {
		if (SettingsManager.getNotificationsEnabled(this)) {
			Intent intent = new Intent(this, LocationServiceReceiver.class);
			intent.setAction(LocationServiceReceiver.LOCATION_SERVICE_RECEIVER_ENABLE);
		}
	}

	private void populateCategory() {
		Intent intent = new Intent(getBaseContext(), NetworkHandler.class);
		intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
		startService(intent);
	}

	private void populateGrocery() {
		Intent intent = new Intent(getBaseContext(), NetworkHandler.class);
		intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
		startService(intent);
	}

	private void populateStoreParent() {
		Intent intent = new Intent(getBaseContext(), NetworkHandler.class);
		intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.STO_PAR);
		startService(intent);
	}

	private void populateStore() {
		Intent intent = new Intent(getBaseContext(), NetworkHandler.class);
		intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.STO);
		startService(intent);
	}

	private void populateFlyer() {
		Intent intent = new Intent(getBaseContext(), NetworkHandler.class);
		intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.FLY);
		startService(intent);
	}

	private void configHandler() {
		mProgressBar.setProgress(PROGRESS_MAX);
		Handler handler = new Handler();
		// wait a bit, then start the home screen
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // make sure we close the splash screen so the user won't come
                // back when it presses back key
                finish();
                if (!mIsBackButtonPressed) {
                    ((GroceryApplication) getApplication()).constructGlobals(mContext);

                    // start the home screen if the back button wasn't pressed
                    // already
                    Intent intent = new Intent(SplashScreenActivity.this, CategoryTopFragmentActivity.class);
                    if (mLocalizationWarningDialogIntentExtra != null) {
                        intent.putExtra(mLocalizationWarningDialogIntentExtra, true);
                    }
                    SplashScreenActivity.this.startActivity(intent);
                }
            }
        }, SPLASH_DURATION);
	}

	@Override
	public void onBackPressed() {
		// set the flag to true so the next activity won't start up
		mIsBackButtonPressed = true;
		super.onBackPressed();
	}

	private class RefreshStatusReceiver extends BroadcastReceiver {
		private RefreshStatusReceiver() {

		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int requestType = intent.getBundleExtra("bundle").getInt(NetworkHandler.REQUEST_TYPE);

			// Network handler services are processed in the order they are called in
			if (requestType == NetworkHandler.GRO) {
				SharedPreferences settings = getPreferences(0);
				SharedPreferences.Editor settingsEditor = settings.edit();
				settingsEditor.putBoolean(SETTINGS_IS_DB_POPULATED, true);
				settingsEditor.commit();
				
				// If this is the first time the app ran, start the location notification service
				configLocationPoll();
				
				configHandler();
			}
		}
	}
}