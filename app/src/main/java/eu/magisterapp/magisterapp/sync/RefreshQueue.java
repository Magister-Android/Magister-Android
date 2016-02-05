package eu.magisterapp.magisterapp.sync;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.magisterapp.magisterapp.RefreshHandlerInterface;

/**
 * Created by max on 2/5/16.
 */
public class RefreshQueue extends Handler
{
    private static final String TAG = "sync.RefreshQueue";

    private Map<Integer, Refresh> queue = new HashMap<>();

    private RefreshHandlerInterface onDoneListener;

    public RefreshQueue(Refresh... refreshCallables)
    {
        for (Refresh refresh : refreshCallables)
            queue.put(refresh.hashCode(), refresh);
    }

    public RefreshQueue then(Refresh... refreshers)
    {
        for (Refresh refresh : refreshers)
            queue.put(refresh.hashCode(), refresh);

        return this;
    }

    public RefreshQueue done(RefreshHandlerInterface refreshHandler)
    {
        onDoneListener = refreshHandler;

        return this;
    }

    public void run()
    {
        if (queue.size() <= 5) // max 5 threads
        {
            for (Map.Entry<Integer, Refresh> entry : queue.entrySet())
            {
                Log.i(TAG, "Start new thread");

                new Thread(entry.getValue().setQueue(this)).start();
            }
        }

        else
        {
            throw new RuntimeException("Te veel runnables");
        }

        if (queue.size() == 0 && onDoneListener != null) onDoneListener.onDoneRefreshing();
    }

    public void setFinished(int id)
    {
        Log.i(TAG, "Finish thread");
        queue.remove(id);

        if (queue.isEmpty() && onDoneListener != null)
        {
            // alle shit is af.
            // TODO: fix dat hij al iets zodra de dingen van currentFragment af zijn.
            onDoneListener.onDoneRefreshing();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        setFinished(msg.what);
    }
}
