package eu.magisterapp.magister;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import eu.magisterapp.magister.Storage.MagisterDatabase;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.Cijfer;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.MagisterAPI;
import eu.magisterapp.magisterapi.Utils;


public class DashboardFragment extends TitledFragment
{
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected LinearLayout uurView;
    protected LinearLayout cijferView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        uurView = (LinearLayout) view.findViewById(R.id.volgende_uur_container);
        cijferView = (LinearLayout) view.findViewById(R.id.laatste_cijfers_container);

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

    public ArrayList<ResourceAdapter.DataHolder> getTestCijfers()
    {
        ArrayList<ResourceAdapter.DataHolder> schijt = new ArrayList<>();

        schijt.add(new ResourceAdapter.DataHolder("Natuurkunde", "69.2", "Doppie dopheide", "eergisteren"));
        schijt.add(new ResourceAdapter.DataHolder("Scheikunde", "8.6", "R. Habich", "69 jaar geleden"));
        schijt.add(new ResourceAdapter.DataHolder("Duits", "-15.6", "taaljoker", "morgen"));
        schijt.add(new ResourceAdapter.DataHolder("Engels", "7.6", "taaljoker", "vandaag"));
        schijt.add(new ResourceAdapter.DataHolder("Wiskunde B", "6.9", "L. Siekman", "kleine pauze"));
        schijt.add(new ResourceAdapter.DataHolder("Informagica", "696969", "Marc de baas", "geskipt"));

        return schijt;
    }

    public void refreshDashboard()
    {
        mSwipeRefreshLayout.setRefreshing(true);

        new DashboardFixerTask().execute((Void[]) null);
    }

    private class DashboardFixerTask extends ErrorableAsyncTask
    {
        private CijferList cijfers;
        private AfspraakCollection afspraken;

        private MagisterApp application = (MagisterApp) getActivity().getApplication();

        private MagisterAPI api = application.getApi();

        public DashboardFixerTask()
        {
            super(getContext());
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                AfspraakCollection httpAfspraken = api.getAfspraken(Utils.now(), Utils.deltaDays(7)); // comment dit als je je shit 1 keer hebt opgehaald.

                // Dit dips ik even om de DB te testen.
                MagisterDatabase db = new MagisterDatabase(getContext());

                db.nuke(); // comment dit als je je shit 1 keer hebt opgehaald.

                db.insertAfspraken(api.getMainSessie().id, httpAfspraken); // comment dit als je je shit 1 keer hebt opgehaald.

                afspraken = db.queryAfspraken("WHERE Einde > ? ORDER BY Start ASC LIMIT ?",
                        new String[] {String.valueOf(Utils.now().getMillis()), "12"});


                // cijfers = api.getCijfers(); // Nog geen DB implementatie voor cijfers..
            }

            catch (IOException e)
            {
                if (e instanceof BadResponseException && e.getMessage().startsWith("Ongeldig account of verkeerde combinatie van gebruikersnaam en wachtwoord"))
                    credentialFailure = true;

                else internetFailure = true;


                error(e, "Fix je internet.. Bitch.");
            }

            return null;
        }

        @Override
        public void onSuccess()
        {
            updateRoosterView(afspraken);
//            updateCijferView(cijfers); // Nog geen DB implementatie voor cijfers./
        }

        @Override
        public void onFinish() {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateRoosterView(AfspraakCollection afspraken)
    {
        populateLinearLayout(uurView, new ResourceAdapter(afspraken));
    }

    private void updateCijferView(CijferList cijfers)
    {
        populateLinearLayout(cijferView, new ResourceAdapter(cijfers));
    }
}
