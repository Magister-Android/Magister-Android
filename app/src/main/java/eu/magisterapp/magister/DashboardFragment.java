package eu.magisterapp.magister;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Days;

import java.io.IOException;

import eu.magisterapp.magister.Storage.DataFixer;
import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.Utils;


public class DashboardFragment extends TitledFragment
{
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected LinearLayout uurView;
    protected LinearLayout cijferView;

    protected ResourceAdapter uurAdapter;
    protected ResourceAdapter cijferAdapter;

    protected MagisterApp application;
    protected DataFixer data;

    protected View view;
    protected LayoutInflater inflater;


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

        application = (MagisterApp) getActivity().getApplication();

        data = new DataFixer(application.getApi(), getContext());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.dasboard_swipeview);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                Log.i("Refresh", "Refresh gesture made, refreshing");
                refreshDashboard();
            }

        });

        refreshDashboard();

        Log.i("Create", "DashboardFragment.onCreateView");

        return view;

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

    public void refreshDashboard()
    {
        mSwipeRefreshLayout.setRefreshing(true);

        new DashboardFixerTask().execute();
    }

    public class DashboardFixerTask extends AsyncTask<Void, AfspraakCollection, Boolean>
    {
        AfspraakCollection afspraken;
        CijferList cijfers;

        IOException e;

        boolean noInternet;

        @Override
        protected Boolean doInBackground(Void... args)
        {
            try
            {
                // haal eerst snel cache op zodat je niet hoeft te wachten op je shit.
                publishProgress(data.getNextDayFromCache());
            }

            catch (IOException e)
            {
                // jammer als er hier wat mis gaat. is niet erg, wachten ze maar even.
            }

            // probeer hierna wel gwn je rooster op te halen.

            if (! application.hasInternet())
            {
                noInternet = true;
            }

            else
            {
                try
                {
                    afspraken = data.getNextDay();
                    cijfers = application.getApi().getRecentCijfers();

                    return true;
                }

                catch (IOException e)
                {
                    this.e = e;

                    return false;
                }
            }

            try
            {
                afspraken = data.getNextDayFromCache();

                return true;
            }

            catch (IOException e)
            {
                this.e = e;

                return false;
            }
        }

        @Override
        protected void onProgressUpdate(AfspraakCollection... values) {

            if (values.length == 1)

                updateRoosterView(values[0]);

        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (noInternet)
            {
                Alerts.notify(getActivity(), R.string.no_internet_cache).setAction("INTERNET", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).show();
            }

            if (success)
            {
                updateRoosterView(afspraken);
                updateCijferView(cijfers);
            }

            else if (e != null)
            {
                if (e instanceof BadResponseException)
                {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

                else
                {
                    Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_LONG).show();
                }
            }

            mSwipeRefreshLayout.setRefreshing(false);
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

        if (uurAdapter.getItemCount() > 0)
            populateLinearLayout(uurView, uurAdapter);

        if (cijferAdapter.getItemCount() > 0)
            populateLinearLayout(cijferView, cijferAdapter);
    }
}
