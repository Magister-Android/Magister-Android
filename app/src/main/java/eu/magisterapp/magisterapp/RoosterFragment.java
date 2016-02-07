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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNeedsUpdate) {
            mAdapter.swap(afspraken);

            mNeedsUpdate = false;
        }
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


    }

    @Override
    public Refresh[] getRefreshers(MagisterApp app) {

        return new Refresh[] { RefreshHolder.getRoosterFragmentRefresh(app, van, tot) };

    }

    @Override
    public void readDatabase(DataFixer data) throws IOException {
        final AfspraakList afspraken = data.getAfsprakenFromCache(van, tot);

        if (mRecyclerView == null)
        {
            this.afspraken = afspraken;
            mNeedsUpdate = true;

            return;
        }

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.swap(afspraken);
            }
        });
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

	public void deleteView()
		{
			view = null;
		}
}
