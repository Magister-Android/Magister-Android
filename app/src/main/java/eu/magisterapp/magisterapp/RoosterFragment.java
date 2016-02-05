package eu.magisterapp.magisterapp;

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
import org.joda.time.Days;

import java.io.IOException;

import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakList;
import eu.magisterapp.magisterapi.Utils;
import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapp.sync.Refresh;
import eu.magisterapp.magisterapp.sync.RefreshHolder;


public class RoosterFragment extends TitledFragment implements DatePickerDialog.OnDateSetListener, Refreshable
{
    private static final long MILLIS_PER_DAY = 24 * 3600 * 1000;
    private static final int FETCH_LIMIT = 10;

    private View view;

    private static final DateTime magisterBegin = new DateTime(1900, 1, 1, 0, 0);

    private DateTime current = DateTime.now().withTime(0, 0, 0, 0);

    public DateTime van = current;
    public DateTime tot = current.plusDays(1);

    private int year = current.getYear();
    private int month = current.getMonthOfYear();
    private int day = current.getDayOfMonth();

    private boolean mNeedsUpdate = false;

    private AfspraakList afspraken;

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

        if (mNeedsUpdate) {
            mAdapter.swap(getAfsprakenForDay(van, afspraken));

            mNeedsUpdate = false;
        }

        // fix een update.
        selfUpdate((SwipeRefreshLayout) getActivity().findViewById(R.id.refresh_layout));

        return view;
    }

    private AfspraakList getAfsprakenForDay(DateTime day, AfspraakList afspraken)
    {
        AfspraakList result = new AfspraakList();

        long dayMillis = day.getMillis() / MILLIS_PER_DAY;

        for (Afspraak afspraak : afspraken)
        {
            if (afspraak.getDay().getMillis() / MILLIS_PER_DAY == dayMillis) result.add(afspraak);
        }

        return result;
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
    public Refresh[] getRefreshers(MagisterApp app) {

        DateTime now = Utils.now();

        int difference = Days.daysBetween(now, current).getDays();

        // te veel dagen tussen wat hij normaal ophaalt voor het dashboard, en de dag die hij
        // nu moet ophalen. Maak dus een custom ding, die niet hetzelfde is als het dashboard.
        if (difference > app.getDaysInAdvance() + FETCH_LIMIT || difference < FETCH_LIMIT)
        {
            return new Refresh[] {RefreshHolder.getRoosterRefresh(app, van, tot, false)};
        }

        // Als hij wel in die reeks zit, geef dan een andere Refresh instance. (die het dashboard eenmalig ook gebruikt)
        else
        {
            DateTime refreshVan = now.isBefore(van) ? now : van;
            DateTime refreshTot = now.isAfter(tot) ? now : tot;

            return new Refresh[] { RefreshHolder.getRoosterRefresh(app, refreshVan, refreshTot, true) };
        }

    }

    /**
     * Deze wordt gebruikt voor de swipeRefreshLayout refresh uit main.
     *
     * @param result data die uit een (cache) refresh komt.
     */
    public void onResult(DataFixer.ResultBundle result) {
		if(result.afspraken == null)
			return;

        if (! isVisible())
        {
            afspraken = result.afspraken;
            mNeedsUpdate = true;

            return;
        }

        mAdapter.swap(getAfsprakenForDay(van, result.afspraken));
    }

    public void selfUpdate(SwipeRefreshLayout mainRefresh)
    {
        mainRefresh.setRefreshing(true);

        new UpdateTask(mainRefresh).execute();
    }

    private class UpdateTask extends AsyncTask<Void, Void, AfspraakList>
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
        protected AfspraakList doInBackground(Void... params) {

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
        protected void onPostExecute(AfspraakList afspraken) {

            if (afspraken == null && e != null && main != null) main.handleError(e);

            if (afspraken != null && mAdapter != null) mAdapter.swap(afspraken);

            swipeRefreshLayout.setRefreshing(false);

        }
    }

		public void deleteView()
		{
			view = null;
		}
}
