package com.grocerygo.android.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.grocerygo.android.R;
import com.actionbarsherlock.view.MenuItem;

/**
 * User: robert
 * Date: 23/02/13
 */
public class RefreshAnimation {
	private static boolean inProgress = false;
	private static MenuItem refresh = null;

	public static void refreshIcon(Context context, boolean start, MenuItem refreshItem) {
		if (start) {
			inProgress = true;
			refresh = refreshItem;
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

			Animation rotation = AnimationUtils.loadAnimation(context, R.anim.clockwise_refresh);
			rotation.setRepeatCount(Animation.INFINITE);
			iv.startAnimation(rotation);

			refreshItem.setActionView(iv);
		} else if (!start) {
			if (refreshItem != null && inProgress) {
				refreshItem.getActionView().clearAnimation();
				refreshItem.setActionView(null);
				inProgress = false;
			}
			refresh = null;
		}
	}

	public static boolean isInProgress() {
		return inProgress;
	}

	public static void setInProgress(boolean inProgress) {
		RefreshAnimation.inProgress = inProgress;
	}

	public static MenuItem getRefresh() {
		return refresh;
	}
}
