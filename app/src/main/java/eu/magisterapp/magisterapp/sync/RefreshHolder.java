package eu.magisterapp.magisterapp.sync;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;

import eu.magisterapp.magisterapi.Utils;
import eu.magisterapp.magisterapp.MagisterApp;

/**
 * Created by max on 2/5/16.
 */
public class RefreshHolder {

    // TODO: misschien hier een instelling van maken?
    // geen idee hoe ik dit moet uitleggen. Het is een getal dat bepaalt of
    // hij het dashboard rooster verlengd met maximaal FETCH_LIMIT dagen, zodat
    // de huidige dag op het RoosterFragment ook nog in die reeks valt. Dit scheelt
    // 1 request, omdat we dan niet het dashboard rooster, en het RoosterFragment rooster apart moeten ophalen.
    // We controleren hierop, zodat wanneer jurryt naar het rooster van 2010 gaat,
    // niet in een keer zn hele telefoon vol zit met 1500 dagen cache.
    private static final int FETCH_LIMIT = 14;

    private static Refresh cijferRefresh;

    private static Refresh dashboardRefresh;
    private static Refresh dummyDashboardRefresh;

    private static DateTime roosterVan;
    private static DateTime roosterTot;

    private static Refresh recentCijferRefresh;

    public static Refresh getDashboardRoosterRefresh(MagisterApp app)
    {
        dashboardRefresh = getDummyDashboardRefresh(app);

        return dashboardRefresh;
    }

    private static Refresh getDummyDashboardRefresh(final MagisterApp app)
    {
        DateTime now = Utils.now();

        if (dummyDashboardRefresh == null)
            dummyDashboardRefresh = makeRoosterRefresh(app, now, now.plusDays(app.getDaysInAdvance()), true);

        return dummyDashboardRefresh;
    }

    public static Refresh getRoosterFragmentRefresh(MagisterApp app, DateTime van, DateTime tot)
    {
        DateTime now = Utils.now();

        roosterVan = van;
        roosterTot = tot;

        int ahead = app.getDaysInAdvance();

        // Zorg dat dummyDashboardRefresh != null
        getDummyDashboardRefresh(app);

        if (Days.daysBetween(now, van).getDays() < -ahead || Days.daysBetween(now, tot).getDays() > ahead + FETCH_LIMIT) {

            Log.i("RefreshHolder", "andere dan dashboard");

            dashboardRefresh = getDummyDashboardRefresh(app);

            return makeRoosterRefresh(app, van, tot, false);
        }

        else
        {
            Log.i("RefreshHolder", "Zelfde als dashboard");

            DateTime refreshVan = van.isBefore(dummyDashboardRefresh.van) ? van : dummyDashboardRefresh.van;
            DateTime refreshTot = tot.isAfter(dummyDashboardRefresh.tot) ? tot : dummyDashboardRefresh.tot;

            // Maak een refresh die de dashboardweek ophaalt, verkort of verlengd
            // zodat de huidige dag van het rooster er ook bij zit.
            // Hij gebruikt dus de kleinste (nu of van) en de grootste (nu of tot)

            return dashboardRefresh = makeRoosterRefresh(app, refreshVan, refreshTot, false);
        }
    }

    private static Refresh makeRoosterRefresh(final MagisterApp app, final DateTime van, final DateTime tot, boolean dummy)
    {
        return new Refresh(van, tot, dummy ? "Dashboard dummy" : "RoosterFragment") {
            @Override
            public void fire() {
                try
                {
                    app.getDataStore().fetchOnlineAfspraken(van, tot);
                }

                catch (IOException e)
                {
                    handleError(e);
                }
            }
        };
    }

    public static Refresh getCijferRefresh(final MagisterApp app)
    {
        if (cijferRefresh == null)

            cijferRefresh = new Refresh("Cijfers") {
                @Override
                public void fire() {
                    try
                    {
                        app.getDataStore().fetchOnlineCijfers();
                    }

                    catch (IOException e)
                    {
                        handleError(e);
                    }
                }
            };

        return cijferRefresh;
    }

    public static Refresh getRecentCijferRefresh(final MagisterApp app)
    {
        if (recentCijferRefresh == null)

            recentCijferRefresh = new Refresh("Recent cijfers") {
                @Override
                public void fire() {
                    try
                    {
                        app.getDataStore().fetchOnlineRecentCijfers();
                    }

                    catch (IOException e)
                    {
                        handleError(e);
                    }
                }
            };

        return recentCijferRefresh;
    }

    private Refresh refreshInstance;

    public Refresh get()
    {
        return refreshInstance;
    }

    public void set(Refresh refresh)
    {
        refreshInstance = refresh;
    }

}
