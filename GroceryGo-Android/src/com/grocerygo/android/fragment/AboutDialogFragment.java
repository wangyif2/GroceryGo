package com.grocerygo.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.grocerygo.android.R;

import java.util.List;

public class AboutDialogFragment extends SherlockDialogFragment {
    Context mContext;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String titleString = getString(R.string.about_title) + " v" + getString(R.string.version_name) + " build " + getString(R.string.version_code);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(titleString)
                .setPositiveButton(R.string.navdrawer_item_about_feedback, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.app_email)});
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.navdrawer_item_feedback_subject));

                        // Get a list of apps that are capable of handling this email intent
                        List<ResolveInfo> pkgAppsList = mContext.getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);

                        for (int i = 0; i < pkgAppsList.size(); i++) {
                            ResolveInfo info = pkgAppsList.get(i);
                            String packageName = info.activityInfo.packageName;
                            String className = info.activityInfo.name;

                            if (packageName.equals("com.google.android.gm")) {
                                // Set the intent to launch that specific app
                                emailIntent.setClassName(packageName, className);
                            }
                        }

                        // Start the app
                        startActivity(emailIntent);

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .setView(((Activity) mContext).getLayoutInflater().inflate(R.layout.about_dialog, null))
                .create();

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
