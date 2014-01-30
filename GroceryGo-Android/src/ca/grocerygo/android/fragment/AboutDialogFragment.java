package ca.grocerygo.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import ca.grocerygo.android.R;
import ca.grocerygo.android.utils.GroceryGoUtils;
import ca.grocerygo.android.utils.GroceryRefreshTrigger;
import com.actionbarsherlock.app.SherlockDialogFragment;

import java.util.List;

public class AboutDialogFragment extends SherlockDialogFragment {
    Context mContext;

    // Dev_Button: The number of time you have to click on textview to enable refresh
    private static final int DEV_BUTTON_ACTIVATE_COUNT = 8;

    // Dev_Button: the counter counting the current number of click on the textview within 1 second time frame
    private int devButtonClickCount;

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
        String titleString = getString(R.string.about_title) + " v" + getString(R.string.version_name) + " build " + GroceryGoUtils.getVersionCode(mContext);
        View aboutDialogView = getActivity().getLayoutInflater().inflate(R.layout.about_dialog, null);
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
                .setNeutralButton(R.string.about_terms, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open link in browser and dismiss the open dialogue
                        Uri uriUrl = Uri.parse("http://www.grocerygo.ca/terms.html");
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                        dialog.dismiss();
                    }
                })
                .setView(aboutDialogView)
                .create();
        //
        // Implementation of "Dev_Button", where if you click on the textview area of the About dialog DEV_BUTTON_ACTIVATE_COUNT
        // number of times within one second, the refresh button will be activated in the CategoryTopFragmentActivity
        //

        TextView text = (TextView) aboutDialogView.findViewById(R.id.about_dialog_textbox);

        devButtonClickCount = 0;
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (devButtonClickCount > DEV_BUTTON_ACTIVATE_COUNT) {
                    devButtonClickCount = 0;
                    GroceryRefreshTrigger.enableRefresh(mContext);
                    Toast.makeText(mContext, "Refresh button enabled", Toast.LENGTH_SHORT).show();
                }
                devButtonClickCount++;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (devButtonClickCount < 5)
                            devButtonClickCount = 0;
                    }
                };

                thread.start();
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
