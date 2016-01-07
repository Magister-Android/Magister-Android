package eu.magisterapp.magister;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.Displayable;
import eu.magisterapp.magisterapi.Utils;


public class RoosterFragment extends TitledFragment implements Refreshable, DatePickerDialog.OnDateSetListener, RoosterProvider
{
    private View view;

    private int year;
    private int month;
    private int day;

    private DateTime current = DateTime.now();

    private int oldPosition = 0;

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    private AfspraakCollection afspraken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) return view;

        DateTime now = Utils.now();

        year = now.getYear();
        month = now.getMonthOfYear();
        day = now.getDayOfMonth();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_rooster, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_pick_date);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDatePicker().show();
            }
        });

        mViewPager = (ViewPager) view.findViewById(R.id.rooster_pager);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position) {
                int diff = position - oldPosition;

                Log.d("Jemoeder", String.valueOf(diff));

                current = current.plusDays(diff);
            }
        });
        mPagerAdapter = new RoosterPagerAdapter(this, getChildFragmentManager());

        return view;
    }

    public DatePickerDialog makeDatePicker()
    {
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        current = new DateTime(year, month, day, 0, 0);
    }

    @Override
    public List<? extends Displayable> refresh(MagisterApp app) throws IOException {
        return afspraken = app.getDataStore().getAfspraken(current.minusDays(5), current.plusDays(5));
    }

    @Override
    public List<? extends Displayable> quickRefresh(MagisterApp app) throws IOException {
        return afspraken = app.getDataStore().getAfsprakenFromCache(current.minusDays(5), current.plusDays(5));
    }

    @Override
    public void pushToUI(List<? extends Displayable> displayables) {
        afspraken = (AfspraakCollection) displayables;
    }

    @Override
    public AfspraakCollection getRoosterForPosition(int position) {
        return null;
    }
}
