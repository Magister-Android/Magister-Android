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

    public String tag;

    private RefreshQueue mQueue;

    public ErrorHandlerInterface mErrorHandler;

    public final DateTime van;
    public final DateTime tot;

    public Refresh()
    {
        van = null;
        tot = null;
    }

    public Refresh(String tag)
    {
        van = null;
        tot = null;
        this.tag = tag;
    }

    public Refresh(final DateTime van, final DateTime tot)
    {
        this.van = van;
        this.tot = tot;
    }

    public Refresh(final DateTime van, final DateTime tot, String tag)
    {
        this.van = van;
        this.tot = tot;
        this.tag = tag;
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
        if (mQueue != null) mQueue.sendEmptyMessage(id);
    }

    public Refresh setQueue(RefreshQueue queue)
    {
        mQueue = queue;

        return this;
    }
}
