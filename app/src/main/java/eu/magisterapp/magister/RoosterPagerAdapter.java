package eu.magisterapp.magister;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by max on 7-1-16.
 */
public class RoosterPagerAdapter extends FragmentStatePagerAdapter {

    private RoosterProvider mRoosterProvider;

    public RoosterPagerAdapter(RoosterProvider provider, FragmentManager fm)
    {
        super(fm);

        mRoosterProvider = provider;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = new RoosterDay();

        Bundle args = new Bundle();
        args.putSerializable(RoosterDay.ARGUMENTS_KEY, mRoosterProvider.getRoosterForPosition(position));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 10;
    }
}
