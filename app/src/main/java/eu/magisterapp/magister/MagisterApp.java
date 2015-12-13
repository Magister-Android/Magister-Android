package eu.magisterapp.magister;

import android.app.Application;
import android.content.SharedPreferences;

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

    public MagisterAPI getApi()
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

        notifyCredentialsUpdated(school, username, password);
    }

    public boolean isAuthenticated()
    {
        return ! this.getSharedPreferences(PREFS_NAME, 0).getString(PREFS_SCHOOL, "").isEmpty();
    }


}
