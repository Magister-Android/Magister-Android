package eu.magisterapp.magister;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.joda.time.DateTime;

import java.io.IOException;

import eu.magisterapp.magisterapi.AfspraakCollection;


public class RoosterFragment extends TitledFragment implements DatePickerDialog.OnDateSetListener, OnMainRefreshListener
{
    private View view;

    private static final DateTime magisterBegin = new DateTime(1900, 1, 1, 0, 0);

    private DateTime current = DateTime.now().withTime(0, 0, 0, 0);

    public DateTime van = current;
    public DateTime tot = current.plusDays(1);

    private int year = current.getYear();
    private int month = current.getMonthOfYear();
    private int day = current.getDayOfMonth();

    private boolean refreshed = false;

    private AfspraakCollection afspraken;

    private RecyclerView mRecyclerView;
    private ResourceAdapter mAdapter;
    private FloatingActionButton mFab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_rooster, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rooster_recycler_view);
        mAdapter = new ResourceAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        mFab = (FloatingActionButton) view.findViewById(R.id.fab_pick_date);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), RoosterFragment.this, year, month - 1, day).show();
            }
        });

        // fix een update.
        selfUpdate((SwipeRefreshLayout) getActivity().findViewById(R.id.refresh_layout));

        return view;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        month = monthOfYear + 1;
        day = dayOfMonth;

        Log.i("idk", String.format("%d %d %d", year, month, day));

        van = current = new DateTime(year, month, day, 0, 0);
        tot = van.plusDays(1);

        selfUpdate((SwipeRefreshLayout) getActivity().findViewById(R.id.refresh_layout));
    }

    @Override
    public void onRefreshed(MagisterApp app) {
        try
        {
            afspraken = app.getDataStore().getAfsprakenFromCache(van, tot);
        }

        catch (IOException e)
        {
            // jemoeder
        }
    }

    @Override
    public void onPostRefresh() {
        if (afspraken != null && afspraken.size() > 0 && isVisible()) mAdapter.swap(afspraken);
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
        mainRefresh.setRefreshing(true);

        new UpdateTask(mainRefresh).execute();
    }

    private class UpdateTask extends AsyncTask<Void, Void, AfspraakCollection>
    {
        private SwipeRefreshLayout swipeRefreshLayout;
        private MagisterApp app;

        private Main main;

        // possible error
        private IOException e;

        public UpdateTask(SwipeRefreshLayout swipeRefreshLayout)
        {
            this.swipeRefreshLayout = swipeRefreshLayout;
            app = (MagisterApp) getActivity().getApplication();

            if (getActivity() instanceof Main) main = (Main) getActivity();
        }

        @Override
        protected AfspraakCollection doInBackground(Void... params) {

            try
            {
                if (! app.hasInternet()) return app.getDataStore().getAfsprakenFromCache(van, tot);

                return app.getDataStore().getAfspraken(van, tot);
            }

            catch (IOException e)
            {
                this.e = e;

                try {
                    return app.getDataStore().getAfsprakenFromCache(van, tot);
                } catch (IOException tooBadSoSad) {
                    // :(
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(AfspraakCollection afspraken) {

            if (afspraken == null && e != null && main != null) main.handleError(e);

            if (afspraken != null && mAdapter != null) mAdapter.swap(afspraken);

            swipeRefreshLayout.setRefreshing(false);

        }
    }
}
