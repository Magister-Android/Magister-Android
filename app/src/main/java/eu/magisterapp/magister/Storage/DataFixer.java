package eu.magisterapp.magister.Storage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.joda.time.DateTime;

import java.io.IOException;

import eu.magisterapp.magister.MagisterApp;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.MagisterAPI;
import eu.magisterapp.magisterapi.Utils;

/**
 * Created by max on 13-12-15.
 */
public class DataFixer {

    private MagisterAPI api;
    private MagisterDatabase db;

    private Context context;

    public DataFixer(MagisterAPI api, Context context)
    {
        this.api = api;
        db = new MagisterDatabase(context);

        this.context = context;
    }

    private boolean hasInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public int getDaysInAdvance()
    {
        return context.getSharedPreferences(MagisterApp.PREFS_NAME, 0).getInt(MagisterApp.PREFS_DAYS_IN_ADVANCE, 7);
    }

    public AfspraakCollection getAfspraken(int days) throws IOException
    {
        DateTime van = Utils.now();
        DateTime tot = Utils.deltaDays(days);

        if (! hasInternet())
        {
            // zernike
            return getLocalAfspraken(van, tot);
        }

        else
        {
            try
            {
                return getOnlineAfspraken(van, tot);
            }

            catch (IOException e)
            {
                e.printStackTrace();

                return getLocalAfspraken(van, tot);
            }
        }
    }

    private AfspraakCollection getLocalAfspraken(DateTime van, DateTime tot) throws IOException
    {
        return db.queryAfspraken(
                        " WHERE " + MagisterDatabase.Afspraken.START + " > ? AND "
                        + MagisterDatabase.Afspraken.EINDE + " < ?"
                ,
                new String[] {
                        String.valueOf(van.getMillis()),
                        String.valueOf(tot.getMillis())
                });
    }

    private AfspraakCollection getOnlineAfspraken(DateTime van, DateTime tot) throws IOException
    {
        return api.getAfspraken(van, tot);
    }
}
