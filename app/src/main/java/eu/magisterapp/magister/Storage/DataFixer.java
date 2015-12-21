package eu.magisterapp.magister.Storage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.IOException;

import eu.magisterapp.magister.Alerts;
import eu.magisterapp.magister.MagisterApp;
import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.MagisterAPI;
import eu.magisterapp.magisterapi.Utils;

/**
 * Created by max on 13-12-15.
 */
public class DataFixer {

    private MagisterAPI api;
    private MagisterDatabase db;

    private MagisterApp app;

    private Context context;

    public DataFixer(MagisterAPI api, Context context)
    {
        this.api = api;
        db = new MagisterDatabase(context);

        this.context = context;

        app = (MagisterApp) context.getApplicationContext();

    }

    public int getDaysInAdvance()
    {
        return context.getSharedPreferences(MagisterApp.PREFS_NAME, 0).getInt(MagisterApp.PREFS_DAYS_IN_ADVANCE, 21);
    }

    public AfspraakCollection getNextDay() throws IOException
    {
        if (app.hasInternet())
        {
            try
            {
                db.insertAfspraken("", api.getAfspraken(Utils.now(), Utils.deltaDays(getDaysInAdvance())));
            }

            catch (IOException e)
            {
                // het is jammer.
            }
        }

        return getNextDayFromCache();
    }

    public AfspraakCollection getNextDayFromCache() throws IOException
    {
        AfspraakCollection afspraken = db.queryAfspraken("WHERE Einde >= ? ORDER BY Start ASC LIMIT ?", db.now(), "1");
        Afspraak eerste;

        if (afspraken.size() > 0) eerste = afspraken.get(0);
        else return afspraken;

        Log.i("afspraak", eerste.getDocenten() + eerste.getVakken() + eerste.getLokalen() + eerste.Start.toString("MM-dd") + eerste.Einde.toString("MM-dd"));

        DateTime start = eerste.Start; // Begin van 1e afspraak
        DateTime end = start.withTimeAtStartOfDay().plusDays(1); // begin van volgende dag.

        return db.queryAfspraken("WHERE (Start <= @now AND Einde >= @end) " +
                "OR (Start >= @now AND Einde <= @end) " +
                "OR (@now BETWEEN Start AND Einde) " +
                "OR (@end BETWEEN Start AND Einde) " +
                "ORDER BY Start ASC", db.now(), db.ms(end));

    }

}
