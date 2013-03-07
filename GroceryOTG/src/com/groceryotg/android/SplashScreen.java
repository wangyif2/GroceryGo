package com.groceryotg.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity
{
	// used to know if the back button was pressed in the splash screen activity
	// and avoid opening the next activity
	private boolean				mIsBackButtonPressed;
	private static final int	SPLASH_DURATION	= 2000; // 2 seconds

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash_screen);

		Handler handler = new Handler();

		// run a thread after 2 seconds to start the home screen
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				// make sure we close the splash screen so the user won't come
				// back when it presses back key
				finish();
				if (!mIsBackButtonPressed)
				{
					// start the home screen if the back button wasn't pressed
					// already
					Intent intent = new Intent(SplashScreen.this, CategoryOverView.class);
					SplashScreen.this.startActivity(intent);
				}
			}
		}, SPLASH_DURATION);
	}

	@Override
	public void onBackPressed()
	{
		// set the flag to true so the next activity won't start up
		mIsBackButtonPressed = true;
		super.onBackPressed();
	}
}