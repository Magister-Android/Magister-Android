package eu.magisterapp.magister;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import eu.magisterapp.magisterapi.AfspraakCollection;

/**
 * Created by max on 7-1-16.
 */
public class RoosterPagerAdapter extends FragmentStatePagerAdapter {

    private RoosterFragment container;
    private Map<Integer, AfspraakCollection> indexed = new HashMap<>();

    public RoosterPagerAdapter(FragmentManager fm, RoosterFragment container)
    {
        super(fm);

        this.container = container;
    }

    @Override
    public Fragment getItem(int position) {

        AfspraakCollection afspraken = indexed.get(position);

        if (afspraken == null)
        {
            Log.e("wtf", "null?");
            return null;
        }

        Bundle args = new Bundle();
        args.putSerializable(RoosterDay.ARGUMENTS_KEY, afspraken);

        Fragment frag = new RoosterDay();
        frag.setArguments(args);

        Log.i("asdf", String.valueOf(getCount()));

        return frag;
    }

    @Override
    public int getCount() {
        if (indexed == null) return 0;
        return indexed.size();
    }

    public void setData(Map<Integer, AfspraakCollection> indexedAfspraakCollection)
    {
        indexed = indexedAfspraakCollection;

        notifyDataSetChanged();

        Log.i("asdf change", "changed to " + String.valueOf(indexed.size()));
    }
}
