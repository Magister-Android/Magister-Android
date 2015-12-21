package eu.magisterapp.magister;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import eu.magisterapp.magister.Storage.DataFixer;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.Displayable;
import eu.magisterapp.magisterapi.MagisterAPI;


public class DashboardFragment extends TitledFragment
{
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected LinearLayout uurView;
    protected LinearLayout cijferView;

    protected ResourceAdapter uurAdapter;
    protected ResourceAdapter cijferAdapter;

    protected MagisterApp application;
    protected DataFixer data;

    private MagisterAPI api;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        uurView = (LinearLayout) view.findViewById(R.id.volgende_uur_container);
        uurAdapter = new ResourceAdapter();

        cijferView = (LinearLayout) view.findViewById(R.id.laatste_cijfers_container);
        cijferAdapter = new ResourceAdapter();

        application = (MagisterApp) getActivity().getApplication();

        data = new DataFixer(application.getApi(), getContext());
        api = application.getApi();

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.dasboard_swipeview);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh()
            {
                Log.i("Refresh", "Refresh gesture made, refreshing");
                refreshDashboard();
            }

        });

        refreshDashboard();

        return view;

    }

    protected void populateLinearLayout(LinearLayout layout, RecyclerView.Adapter adapter)
    {
        layout.removeAllViews();

        int count = adapter.getItemCount();

        for (int i = 0; i < count; i++)
        {
            RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(layout, i);
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

            Log.i("DashBoardFixer", "Hij is klaar.");

            if (noInternet)
            {
                Log.i("DashBoardFixer", "Geen internet");

                Alerts.notify(getActivity(), R.string.no_internet_cache).setAction("INTERNET", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).show();
            }

            if (success)
            {
                Log.i("DashBoardFixer", "Success");
                updateRoosterView(afspraken);
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
        uurAdapter.swap(afspraken);
        populateLinearLayout(uurView, uurAdapter);
    }

    private void updateCijferView(CijferList cijfers)
    {
        cijferAdapter.swap(cijfers);
        populateLinearLayout(cijferView, cijferAdapter);
    }
}
