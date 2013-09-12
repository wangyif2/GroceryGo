package ca.grocerygo.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import ca.grocerygo.android.R;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * User: robert
 * Date: 09/09/13
 */
public class ToggleWifiDialogFragment extends SherlockDialogFragment {
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
        String titleString = getString(R.string.toggle_wifi_enable_wifi);
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(titleString)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(R.string.toggle_wifi_action_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(i);

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.toggle_wifi_action_disagree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .setView(((Activity) mContext).getLayoutInflater().inflate(R.layout.toggle_wifi_dialog, null))
                .create();
        //

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

}
