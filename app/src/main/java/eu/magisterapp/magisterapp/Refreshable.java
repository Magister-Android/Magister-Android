package eu.magisterapp.magisterapp;

import eu.magisterapp.magisterapp.sync.Refresh;

/**
 * Created by max on 2/5/16.
 */
public interface Refreshable {

    public Refresh[] getRefreshers(MagisterApp app);

}
