package eu.magisterapp.magisterapp;

import android.os.Bundle;


import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Days;

import java.io.IOException;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.Utils;


public class DashboardFragment extends TitledFragment implements OnMainRefreshListener
{
    protected LinearLayout uurView;
    protected LinearLayout cijferView;

    protected ResourceAdapter uurAdapter;
    protected ResourceAdapter cijferAdapter;

    protected CijferList cijfers;
    protected AfspraakCollection afspraken;

    protected MagisterApp application;
    protected DataFixer data;

    protected View view;
    protected LayoutInflater inflater;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ScrollView scrollview;

    private boolean refreshed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (view != null) return view;

        this.inflater = inflater;

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        uurView = (LinearLayout) view.findViewById(R.id.volgende_uur_container);
        uurAdapter = new ResourceAdapter();

        cijferView = (LinearLayout) view.findViewById(R.id.laatste_cijfers_container);
        cijferAdapter = new ResourceAdapter();

        swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.refresh_layout);

        Log.i("Create", "DashboardFragment.onCreateView");

        if (refreshed) onPostRefresh();

        // Fix scrollview
        scrollview = (ScrollView) view.findViewById(R.id.dashboard_scrollview);
        scrollview.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                // Disable de main swiperefreshlayout als je niet helemaal omhoog bent gescrolt.
                swipeRefreshLayout.setEnabled(scrollview.getScrollY() == 0);
            }
        });

        return view;
    }

    @Override
    public void onRefreshed(MagisterApp app) {

        DataFixer data = app.getDataStore();

        try
        {
            afspraken = data.getNextDayFromCache();
            cijfers = data.getRecentCijfersFromCache();

            refreshed = true;
        }

        catch (IOException e)
        {
            e.printStackTrace();

            // Dit komt alleen voor bij deserialization errors, of data corruption.
            // alleen low-level shit, kan er vanuit gaan dat dit nooit gebeurt.
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPostRefresh() {
        updateRoosterView(afspraken);
        updateCijferView(cijfers);

        refreshed = false;
    }

    @Override
    public Object[] quickUpdate(MagisterApp app) {
        try
        {
            DataFixer data = app.getDataStore();

            return new Object[] {data.getNextDayFromCache(), data.getRecentCijfersFromCache()};
        }

        catch (IOException e)
        {
            // jemoeder
        }

        return new Object[0];
    }

    @Override
    public void onQuickUpdated(Object... result) {
        if (result.length != 2) return;

        updateRoosterView((AfspraakCollection) result[0]);
        updateCijferView((CijferList) result[1]);
    }

    protected void populateLinearLayout(LinearLayout layout, RecyclerView.Adapter adapter)
    {
        layout.removeAllViews();

        int count = adapter.getItemCount();

        for (int i = 0; i < count; i++)
        {
            RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(layout, adapter.getItemViewType(i));
            adapter.onBindViewHolder(holder, i);

            layout.addView(holder.itemView);
        }
    }

    private void updateRoosterView(AfspraakCollection afspraken)
    {
        if (afspraken.size() > 0)
        {
            // verander het kopje "R.string.volgende_uur" naar iets anders
            TextView header = (TextView) view.findViewById(R.id.volgende_uur_text);

            Afspraak eerste = afspraken.get(0);

            int daydiff = Days.daysBetween(Utils.now().withTimeAtStartOfDay(), eerste.Start.withTimeAtStartOfDay()).getDays();

            switch (daydiff)
            {
                case 0:
                    // eerste uur is op zelfde dag
                    header.setText(R.string.volgende_uur);
                    break;

                case 1:
                    // morgen
                    String tomorrow = DateUtils.getRelativeTimeSpanString(
                            eerste.Start.getMillis(),
                            System.currentTimeMillis(),
                            DateUtils.DAY_IN_MILLIS
                    ).toString();

                    // Uppercase first letter
                    tomorrow = tomorrow.substring(0, 1).toUpperCase() + tomorrow.substring(1);

                    header.setText(tomorrow);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    // eerste uur valt nog in deze week
                    header.setText(eerste.Start.toString("EEEE"));
                    break;

                default:
                    // meer dan 1 week
                    header.setText(eerste.Start.toString("d MMMM")); // dit fixt automagisch in de goede taal.. wow
                    break;
            }
        }

        else
        {
            // Maak hier een custom view, die zegt dat je geen afspraken hebt, en flikker dat in de linearlayout.
            View view = inflater.inflate(R.layout.resource_row_empty, null);

            TextView tv = (TextView) view.findViewById(R.id.empty_text);
            tv.setText(R.string.empty_afspraken);

            uurView.removeAllViews();
            uurView.addView(view);

            return;
        }

        uurAdapter.swap(afspraken);
        populateLinearLayout(uurView, uurAdapter);
    }

    private void updateCijferView(CijferList cijfers)
    {
        if (cijfers.size() == 0)
        {
            // Maak hier een custom "empty" view, en zet dat in die linearLayout.
            View view = inflater.inflate(R.layout.resource_row_empty, null);

            TextView tv = (TextView) view.findViewById(R.id.empty_text);
            tv.setText(R.string.empty_cijfers_recent);

            cijferView.removeAllViews();
            cijferView.addView(view);
        }

        else
        {
            cijferAdapter.swap(cijfers);
            populateLinearLayout(cijferView, cijferAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (scrollview == null) return;

        swipeRefreshLayout.setEnabled(scrollview.getScrollY() == 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Enable swipeRefreshlayout weer, anders werkt hij niet in andere fragments.
        swipeRefreshLayout.setEnabled(true);
    }
}