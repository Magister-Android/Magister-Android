package eu.magisterapp.magisterapp;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by max on 2/5/16.
 */
public interface RefreshHandlerInterface extends SwipeRefreshLayout.OnRefreshListener {

    public void onDoneRefreshing();

}
