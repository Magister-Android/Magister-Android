package eu.magisterapp.magister;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    public DateTime van, tot;

    private boolean refreshed = false;

    private DateTime current = DateTime.now();

    private AfspraakCollection afspraken;

    private RecyclerView mRecyclerView;
    private ResourceAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_rooster, container);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rooster_recycler_view);
        mAdapter = new ResourceAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear + 1;
        this.day = dayOfMonth;

        van = current = new DateTime(year, month, day, 0, 0);
        tot = van.plusDays(1);

        selfUpdate(null);
    }

    @Override
    public void onRefreshed(MagisterApp app) {
        // asdf
    }

    @Override
    public void onPostRefresh() {
        // asdf
    }

    @Override
    public Object[] quickUpdate(MagisterApp app) {
        try
        {
            return new Object[] { app.getDataStore().getAfspraken(van, tot) };
    }

        catch (IOException e)
        {
            // asdf
        }

        return new Object[0];
    }

    @Override
    public void onQuickUpdated(Object... result) {
        if (result.length < 1) return;

        AfspraakCollection afspraken = (AfspraakCollection) result[0];

        mAdapter.swap(afspraken);
    }

    public void selfUpdate(SwipeRefreshLayout mainRefresh)
    {

    }

    private class UpdateTask extends AsyncTask<SwipeRefreshLayout, Void, Void>
    {
        private SwipeRefreshLayout refreshLayout;

        @Override
        protected Void doInBackground(SwipeRefreshLayout... params) {

            if (params.length > 0) refreshLayout = params[0];

            return null;
        }
    }
}
