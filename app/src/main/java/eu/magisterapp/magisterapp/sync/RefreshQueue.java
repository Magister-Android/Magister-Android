package eu.magisterapp.magisterapp.sync;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.magisterapp.magisterapp.RefreshHandlerInterface;

/**
 * Created by max on 2/5/16.
 */
public class RefreshQueue
{
    private List<Refresh> queue = new ArrayList<>();
    private Set<Integer> codes = new HashSet<>();

    private RefreshHandlerInterface onDoneListener;

    public RefreshQueue(Refresh... refreshCallabels)
    {
        Collections.addAll(queue, refreshCallabels);
    }

    public RefreshQueue then(Refresh... refreshers)
    {
        Collections.addAll(queue, refreshers);

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
            for (Refresh runnable : queue)
            {
                codes.add(runnable.hashCode());

                new Thread(runnable).run();
            }
        }

        else
        {
            throw new RuntimeException("Te veel runnables");
        }

        if (codes.size() == 0 && onDoneListener != null) onDoneListener.onDoneRefreshing();
    }

    public void setFinished(int id)
    {
        codes.remove(id);

        if (codes.isEmpty() && onDoneListener != null)
        {
            // alle shit is af.
            // TODO: fix dat hij al iets zodra de dingen van currentFragmetn af zijn.
            onDoneListener.onDoneRefreshing();
        }
    }
}
