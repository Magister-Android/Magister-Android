package eu.magisterapp.magister.Storage;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;

import eu.magisterapp.magister.MagisterApp;
import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.Cijfer;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.MagisterAPI;
import eu.magisterapp.magisterapi.Sessie;
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
                db.insertAfspraken(app.getOwner(), api.getAfspraken(Utils.now(), Utils.deltaDays(getDaysInAdvance())));
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
        AfspraakCollection afspraken = db.queryAfspraken("WHERE Einde >= ? AND owner = ? ORDER BY Start ASC LIMIT ?", db.now(), app.getOwner(), "1");
        Afspraak eerste;

        if (afspraken.size() > 0) eerste = afspraken.get(0);
        else return afspraken;

        DateTime start = eerste.Start; // Begin van 1e afspraak
        DateTime end = start.withTimeAtStartOfDay().plusDays(1); // begin van volgende dag.

        return db.queryAfspraken("WHERE ((Start <= @now AND Einde >= @end) " +
                "OR (Start >= @now AND Einde <= @end) " +
                "OR (@now BETWEEN Start AND Einde) " +
                "OR (@end BETWEEN Start AND Einde)) " +
                "AND owner = ? " +
                "ORDER BY Start ASC", db.now(), db.ms(end), app.getOwner());

    }

    public CijferList getCijfers() throws IOException
    {
        if (app.hasInternet())
        {
            // TODO: meerdere accounts: stop sessie van huidige account hierin, ipv mainsessie.
            Sessie sessie = app.getApi().getMainSessie();
            CijferList cijfers = sessie.getCijfers();

            ArrayList<Cijfer.CijferInfo> info = new ArrayList<>();

            for (Cijfer cijfer : cijfers)
            {
                Cijfer.CijferInfo cijferInfo = getCijferInfo(cijfer, sessie);
                info.add(cijferInfo);
                cijfer.setInfo(cijferInfo);
            }

            db.insertCijfers(sessie.id, cijfers);
            db.insertCijferInfo(info);

            return cijfers;
        }

        return getCijfersFromCache();
    }

    public CijferList getCijfersFromCache() throws IOException
    {
        CijferList cijfers = db.queryCijfers("WHERE owner = ?", app.getOwner());

        for (Cijfer cijfer : cijfers)
        {
            // query in een loop.. kan vast wel beter.
            cijfer.setInfo(getCijferInfo(cijfer, null));
        }

        return cijfers;
    }

    private Cijfer.CijferInfo getCijferInfo(Cijfer cijfer, Sessie sessie) throws IOException
    {
        Cijfer.CijferInfo dbInfo = db.getCijferInfo(cijfer);

        if (dbInfo != null) return dbInfo;

        // Geen sessie beschikbaar, en het staat niet in de database. Als het goed is
        // komt dit nooit voor, want zonder sessie en zonder cache zijn er ook geen cijfers.
        if (sessie == null) return null;

        return sessie.getCijferInfo(cijfer);
    }


    public CijferList getRecentCijfers() throws IOException
    {
        if (app.hasInternet())
        {
            // TODO: meerdere accounts: stop sessie van huidige account hierin, ipv mainsessie.
            CijferList cijfers = app.getApi().getRecentCijfers();

            db.insertRecentCijfers(app.getOwner(), cijfers);

            return cijfers;
        }

        return getRecentCijfersFromCache();
    }

    public CijferList getRecentCijfersFromCache() throws IOException
    {
        // TODO: misschien een "seen" flag erop tyfen, zodat je niet zo vaak naar je 1.3 op duits hoeft te kijken.
        return db.queryRecentCijfers("WHERE owner = ?", app.getOwner());
    }

}
