package eu.magisterapp.magisterapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import eu.magisterapp.magisterapi.BadResponseException;

/**
 * Created by max on 10-12-15.
 */
public abstract class ErrorableAsyncTask extends AsyncTask<Void, Void, Void> {

    private String message;
    private Context context;

    private boolean error = false;
    protected boolean internetFailure = false;
    protected boolean credentialFailure = false;

    public ErrorableAsyncTask(Context appContext)
    {
        context = appContext;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        if (error)
        {
            Log.e("Message notification", message);
            Snackbar snackbar = Alerts.notify((Activity) context, message);

            if (internetFailure) snackbar.setAction("INTERNET", new View.OnClickListener ()
            {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });

            if (credentialFailure) snackbar.setAction("SETTINGS", new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, eu.magisterapp.magisterapp.Settings.class));
                }
            });

            snackbar.show();
        }

        else
        {
            onSuccess();
        }

        onFinish();
    }

    protected void error(IOException e, String fallbackMessage)
    {
        error = true;

        e.printStackTrace();

        if (e instanceof BadResponseException)
        {
            message = e.getMessage();
        }

        else
        {
            message = fallbackMessage;
        }
    }

    abstract void onSuccess();

    public void onFinish()
    {
        // can be overwritten if wanted. (useful for setRefreshing = false)
    }

}
