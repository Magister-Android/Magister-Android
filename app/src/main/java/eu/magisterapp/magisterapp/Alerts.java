package eu.magisterapp.magisterapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

/**
 * Created by max on 10-12-15.
 */
public class Alerts {

    private static CoordinatorLayout coordinatorLayout;

    public static AlertDialog makeDialogAlert(Context context, String message)
    {
        return new AlertDialog.Builder(context)
                .setTitle("Er is iets mis met je cijfers")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // jemoeder
                    }
                }).create();
    }

    public static Snackbar notify(Activity activity, String message)
    {
        return Snackbar.make(activity.findViewById(R.id.coordinator_layout), message, Snackbar.LENGTH_LONG);
    }

    public static Snackbar notify(Activity activity, int message)
    {
        return Snackbar.make(activity.findViewById(R.id.coordinator_layout), message, Snackbar.LENGTH_LONG);
    }

    public static AlertDialog notifyBig(Context context, String message)
    {
        return new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
