package eu.magisterapp.magister;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by max on 10-12-15.
 */
public class Alerts {

    public static AlertDialog makeDialogAlert(Context context, String message)
    {
        return new AlertDialog.Builder(context)
                .setTitle("Je hebt kutcijfers")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // jemoeder
                    }
                }).create();
    }

    public static Toast notify(Context context, String message)
    {
        return Toast.makeText(context, message, Toast.LENGTH_SHORT);
    }

}
