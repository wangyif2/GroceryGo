package ca.grocerygo.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import ca.grocerygo.android.R;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * User: robert
 * Date: 02/09/13
 */
public class ToggleLocationDialogFragment extends SherlockDialogFragment {
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
        String titleString = getString(R.string.toggle_location_enable_location);
        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(titleString)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(R.string.toggle_location_action_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.toggle_location_action_disagree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .setView(((Activity) mContext).getLayoutInflater().inflate(R.layout.toggle_location_dialog, null))
                .create();
        //

        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
