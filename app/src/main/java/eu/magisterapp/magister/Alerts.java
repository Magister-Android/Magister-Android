package eu.magisterapp.magister;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;

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

    public static Snackbar notify(Context context, String message)
    {
        View view = ((Main) context).findViewById(R.id.drawer_layout);

        return Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    }


    public static void testShit(Object o)
    {
        try
        {
            Field[] fields = o.getClass().getDeclaredFields();
            for (int i=0; i<fields.length; i++)
            {
                System.out.println(fields[i].getName() + " - " + fields[i].get(o));
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
