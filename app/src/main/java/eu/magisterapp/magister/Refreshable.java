package eu.magisterapp.magister;

import java.io.IOException;
import java.util.List;

import eu.magisterapp.magisterapi.Displayable;

/**
 * Created by max on 7-1-16.
 */
public interface Refreshable {

    public List<? extends Displayable> refresh(MagisterApp app) throws IOException;

    public List<? extends Displayable> quickRefresh(MagisterApp app) throws IOException;

    public void pushToUI(List<? extends Displayable> displayables);

}
