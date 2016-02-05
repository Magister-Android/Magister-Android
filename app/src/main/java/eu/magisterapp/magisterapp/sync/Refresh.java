package eu.magisterapp.magisterapp.sync;

import org.joda.time.DateTime;

import java.io.IOException;

import eu.magisterapp.magisterapi.MagisterAPI;
import eu.magisterapp.magisterapi.Utils;
import eu.magisterapp.magisterapp.ErrorHandlerInterface;
import eu.magisterapp.magisterapp.MagisterApp;
import eu.magisterapp.magisterapp.Storage.DataFixer;

/**
 * Created by max on 2/5/16.
 */
public abstract class Refresh implements Runnable
{
    public int id = hashCode();

    public DateTime van;
    public DateTime tot;

    public MagisterApp mApp;
    public DataFixer mData;
    public MagisterAPI mApi;

    private RefreshQueue mQueue;

    public ErrorHandlerInterface mErrorHandler;

    public Refresh(MagisterApp app)
    {
        mApp = app;
        mData = app.getDataStore();
        mApi = app.getApi();

        van = Utils.now();
        tot = van.plusDays(app.getDaysInAdvance());
    }

    public Refresh setErrorHandler(ErrorHandlerInterface errorHandler)
    {
        mErrorHandler = errorHandler;

        return this;
    }

    void handleError(IOException e)
    {
        if (mErrorHandler != null) mErrorHandler.handleError(e);
    }

    public abstract void fire();

    @Override
    public void run() {
        fire();
        done();
    }

    public void done()
    {
        if (mQueue != null) mQueue.setFinished(id);
    }

    public void setQueue(RefreshQueue queue)
    {
        mQueue = queue;
    }
}
