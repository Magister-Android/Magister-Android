package eu.magisterapp.magister;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.magisterapp.magister.Storage.DataFixer;
import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.Displayable;
import eu.magisterapp.magisterapi.Utils;


public class RoosterFragment extends TitledFragment implements DatePickerDialog.OnDateSetListener, OnMainRefreshListener
{
    private View view;

    private static final DateTime magisterBegin = new DateTime(1900, 1, 1, 0, 0);

    private int year;
    private int month;
    private int day;

    private boolean refreshed = false;

    private DateTime current = DateTime.now();
    public DateTime van = current.minusDays(5);
    public DateTime tot = current.plusDays(5);

    private ViewPager mViewPager;
    private RoosterPagerAdapter mPagerAdapter;

    private AfspraakCollection afspraken;
    public Map<Integer, AfspraakCollection> parsedAfspraken;

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
        mViewPager.setAdapter(mPagerAdapter = new RoosterPagerAdapter(getChildFragmentManager(), this));

        if (refreshed && afspraken != null && afspraken.size() > 0) onPostRefresh();

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

        this.current = new DateTime(year, month, day, 0, 0);

        selfUpdate(null);
    }

    @Override
    public void onRefreshed(MagisterApp app) {
        try
        {
            afspraken = app.getDataStore().getAfsprakenFromCache(van, tot);

            refreshed = true;
        }

        catch (IOException e)
        {
            // Low level kut exception..
            e.printStackTrace();

            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Object[] quickUpdate(MagisterApp app) {
        return new Object[0];
    }

    @Override
    public void onQuickUpdated(Object... result) {

    }

    @Override
    public void onPostRefresh() {

        // update swagview met nieuwe afspraken.

        Log.i("Swag", "update swag");

        mPagerAdapter.setData(parsedAfspraken = parseAfspraakResponse(afspraken));

        mViewPager.setCurrentItem(getPositionFromDate(current), false);

        refreshed = false;
    }

    private int getPositionFromDate(DateTime date)
    {
        return Days.daysBetween(magisterBegin, date).getDays();
    }

    private Map<Integer, AfspraakCollection> parseAfspraakResponse(AfspraakCollection unparsed)
    {
        Map<Integer, AfspraakCollection> result = new HashMap<>();

        Iterator<AfspraakCollection> iterator = unparsed.dayIterator();

        while (iterator.hasNext())
        {
            AfspraakCollection day = iterator.next();

            result.put(getPositionFromDate(day.getFirstDayTime()), day);
        }

        return result;
    }

    public void selfUpdate(SwipeRefreshLayout refreshLayout)
    {
        new RoosterUpdater().execute(refreshLayout);
    }

    private class RoosterUpdater extends AsyncTask<SwipeRefreshLayout, Void, Boolean>
    {
        AfspraakCollection afspraken;
        DataFixer data = main.getMagisterApplication().getDataStore();

        SwipeRefreshLayout mainlayout;

        @Override
        protected Boolean doInBackground(SwipeRefreshLayout... params) {

            if (params[0] != null) mainlayout = params[0];

            try
            {
                data.fetchOnlineAfspraken(van, tot);
            }

            catch (IOException e)
            {
                e.printStackTrace();

                main.handleError(e);

                return false;
            }

            try
            {
                afspraken = data.getAfsprakenFromCache(van, tot);
                parsedAfspraken = parseAfspraakResponse(afspraken);
            }

            catch (IOException e)
            {
                e.printStackTrace();

                // low level exception, weet niet eens of dit wel in een ander thread mag. yolo
                Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_LONG).show();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success)
            {
                RoosterFragment.this.afspraken = afspraken;

                refreshed = true;

                onPostRefresh();
            }

            if (mainlayout != null) mainlayout.setRefreshing(false);
        }
    }
}
