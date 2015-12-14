package eu.magisterapp.magister;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import eu.magisterapp.magisterapi.MagisterAPI;

/**
 * Created by max on 13-12-15.
 */
public class MagisterApp extends Application {

    public static final String PREFS_NAME = "magister-storage";

    public static final String PREFS_DAYS_IN_ADVANCE = "daysInAdvance";
    public static final int PREFS_DAYS_IN_ADVANCE_DEFAULT = 7;

    public static final String PREFS_USERNAME = "username";
    public static final String PREFS_PASSWORD = "password";
    public static final String PREFS_SCHOOL = "school";

    private MagisterAPI api;

    public synchronized MagisterAPI getApi()
    {
        if (api == null)
        {
            SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);

            api = new MagisterAPI(
                    prefs.getString(PREFS_SCHOOL, ""),
                    prefs.getString(PREFS_USERNAME, ""),
                    prefs.getString(PREFS_PASSWORD, "")
            );
        }

        return api;
    }

    public void notifyCredentialsUpdated()
    {
        if (api == null) return;

        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);

        getApi().reconnect(
                prefs.getString(PREFS_SCHOOL, ""),
                prefs.getString(PREFS_USERNAME, ""),
                prefs.getString(PREFS_PASSWORD, "")
        );
    }

    public void notifyCredentialsUpdated(String school, String username, String password)
    {
        if (api == null) return;

        getApi().reconnect(school, username, password);
    }

    public void updateCredentials(String school, String username, String password)
    {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);

        prefs.edit()
                .putString(PREFS_SCHOOL, school)
                .putString(PREFS_USERNAME, username)
                .putString(PREFS_PASSWORD, password)
                .apply();
    }

    public void voidCredentails()
    {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);

        prefs.edit()
                .remove(PREFS_SCHOOL)
                .remove(PREFS_USERNAME)
                .remove(PREFS_PASSWORD)
                .apply();

    }

    public boolean isAuthenticated()
    {
        return ! this.getSharedPreferences(PREFS_NAME, 0).getString(PREFS_SCHOOL, "").isEmpty();
    }

    public boolean validateCredentials(String school, String username, String password)
    {
        try
        {
            getApi().reconnect(school, username, password).login();

            return true;
        }

        catch (IOException e)
        {
            return false;
        }
    }

    public boolean hasInternet()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
