package eu.magisterapp.magister;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.MagisterAPI;

/**
 * Created by max on 10-12-15.
 */
public abstract class ErrorableAsyncTask extends AsyncTask<Void, Void, Void> {

    private String message;
    private Context context;
    private boolean error = false;

    protected MagisterAPI api;

    public ErrorableAsyncTask(Context context)
    {
        this.context = context;

        api = ((Main) context).api;

        Log.i("Set API", api.getClass().toString());
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (error)
        {
            Log.e("Message notification", message);
            Alerts.notify(context, message).show();
        }

        else
        {
            onSuccess();
        }
    }

    protected void error(IOException e, String fallbackMessage)
    {
        error = true;

        if (e instanceof BadResponseException)
        {
            message = e.getMessage();
        }

        else
        {
            message = fallbackMessage;
        }

        onFinish();
    }

    abstract void onSuccess();

    public void onFinish()
    {
        // can be overwritten if wanted. (useful for setRefreshing = false)
    }

}
