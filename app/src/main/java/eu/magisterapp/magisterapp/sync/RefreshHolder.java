package eu.magisterapp.magisterapp.sync;

import org.joda.time.DateTime;

import java.io.IOException;

import eu.magisterapp.magisterapi.Utils;
import eu.magisterapp.magisterapp.MagisterApp;

/**
 * Created by max on 2/5/16.
 */
public class RefreshHolder {

    private static Refresh cijferRefresh;

    private static Refresh roosterRefresh;

    // Geeft aan of het normale rooster (7 dagen vooruit) moet worden gebruikt,
    // of het rooster met de bounds van het RoosterFragment (custom)
    private static boolean shouldUseRooster = false;

    private static Refresh recentCijferRefresh;

    // Deze wordt door het dashboard gebruikt
    public static Refresh getRoosterRefresh(final MagisterApp app)
    {
        if (roosterRefresh != null && shouldUseRooster)
        {
            shouldUseRooster = false;
            return roosterRefresh;
        }

        final DateTime van = Utils.now();
        final DateTime tot = van.plusDays(app.getDaysInAdvance());

        return getRoosterRefresh(app, van, tot, false);
    }

    // Deze wordt door het rooster gebruikt.
    public static Refresh getRoosterRefresh(final MagisterApp app, final DateTime van, final DateTime tot, boolean useRooster)
    {
        shouldUseRooster = useRooster;

        roosterRefresh = new Refresh(app) {
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

        return roosterRefresh;
    }

    public static Refresh getCijferRefresh(final MagisterApp app)
    {
        if (cijferRefresh == null)

            cijferRefresh = new Refresh(app) {
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

            recentCijferRefresh = new Refresh(app) {
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
}
