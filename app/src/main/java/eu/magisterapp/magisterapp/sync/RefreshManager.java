package eu.magisterapp.magisterapp.sync;

import org.joda.time.DateTime;


import eu.magisterapp.magisterapi.MagisterAPI;
import eu.magisterapp.magisterapi.Utils;
import eu.magisterapp.magisterapp.ErrorHandlerInterface;
import eu.magisterapp.magisterapp.MagisterApp;
import eu.magisterapp.magisterapp.Storage.DataFixer;

/**
 * Created by max on 2/5/16.
 */
public class RefreshManager {

    public final DateTime van;
    public final DateTime tot;

    public final MagisterApp app;
    public final DataFixer data;
    public final MagisterAPI api;

    public ErrorHandlerInterface errorHandler;

    public RefreshManager(MagisterApp app)
    {
        this.app = app;
        data = app.getDataStore();
        api = app.getApi();

        van = Utils.now();
        tot = van.plusDays(app.getDaysInAdvance());
    }

    public void setErrorHandler(ErrorHandlerInterface eh)
    {
        errorHandler = eh;
    }

    public RefreshQueue first(RefreshHolder... refreshers)
    {
        return new RefreshQueue(refreshers);
    }
}
