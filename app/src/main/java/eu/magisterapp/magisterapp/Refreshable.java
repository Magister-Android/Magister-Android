package eu.magisterapp.magisterapp;

import java.io.IOException;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapp.sync.Refresh;

/**
 * Created by max on 2/5/16.
 */
public interface Refreshable {

    public Refresh[] getRefreshers(MagisterApp app);

    public void readDatabase(DataFixer data) throws IOException;

}
