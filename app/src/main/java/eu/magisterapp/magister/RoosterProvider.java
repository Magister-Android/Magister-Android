package eu.magisterapp.magister;

import eu.magisterapp.magisterapi.AfspraakCollection;

/**
 * Created by max on 7-1-16.
 */
public interface RoosterProvider {

    public AfspraakCollection getRoosterForPosition(int position);

}
