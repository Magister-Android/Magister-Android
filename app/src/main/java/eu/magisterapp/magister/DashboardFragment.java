package eu.magisterapp.magister;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.joda.time.LocalDateTime;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.BadResponseException;
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
        Context c = getActivity();

        uurView = (LinearLayout) view.findViewById(R.id.volgende_uur_container);
        populateLinearLayout(uurView, new ResourceAdapter(getTestUren()));

        cijferView = (LinearLayout) view.findViewById(R.id.laatste_cijfers_container);
        populateLinearLayout(cijferView, new ResourceAdapter(getTestCijfers()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.dasboard_swipeview);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh()
            {
                Log.i("Refresh", "Refresh gesture made, refreshing");
                refreshDashboard();
                Log.i("Refresh", "Refresh finished");

                // mDashSwipeView.setRefreshing(false);
            }

        });

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

    public ResourceRow.Resource[] getTestUren()
    {
        return new ResourceRow.Resource[] {
            new ResourceRow.Resource("Natuurkunde", "K109", "C. Dopheide", "9.30 - 10.30"),
            new ResourceRow.Resource("Scheikunde", "K009", "R. Habich", "10.30 - 11.30"),
            new ResourceRow.Resource("Wiskunde B", "K235", "M. Traas", "11.50 - 12.50"),
            new ResourceRow.Resource("Wiskunde B", "K235", "M. Traas", "11.50 - 12.50"),
            new ResourceRow.Resource("Informagica", "K169", "M. de Krosse", "12.50 - 13.50", true),
        };
    }

    public ResourceRow.Resource[] getTestCijfers()
    {
        return new ResourceRow.Resource[] {
            new ResourceRow.Resource("Scheikunde", "7.3", "R. Habich", "19 minuten geleden"),
            new ResourceRow.Resource("Wiskunde B", "8.3", "M. Traas", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "17.3", "M. de Krosse", "2 jaar geleden"),
            new ResourceRow.Resource("Informatica", "17.3", "M. de Krosse", "2 jaar geleden"),
        };
    }

    public void refreshDashboard()
    {
        mSwipeRefreshLayout.setRefreshing(true);

        new HaalRoosterOpTask().execute((Void[]) null);
    }

    private class HaalRoosterOpTask extends AsyncTask<Void, Void, Void>
    {
        AfspraakCollection afspraken;
        private String message;

        @Override
        protected Void doInBackground(Void... params) {

            MagisterAPI api = ((Main) getContext()).api;

            try
            {
                Log.i("Afspraken", "Afspraken ophalen");

                afspraken = api.getAfspraken(Utils.now(), Utils.now()); // omdat het morgen pas maandag is.

                Log.i("Afspraken", "Afspraken opgehaald.");


            }

            catch (IOException | ParseException | JSONException e)
            {
                e.printStackTrace();

                if (e instanceof BadResponseException)
                {
                    message = e.getMessage();
                }

                else
                {
                    message = "Er is iets fout gegaan.";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSwipeRefreshLayout.setRefreshing(false);

            if (afspraken == null)
                makeAlertDialog(message).show();
            else
                updateRoosterView(afspraken);
        }
    }

    private AlertDialog makeAlertDialog(String body)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(body);
        builder.setCancelable(true);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private void updateRoosterView(AfspraakCollection afspraken)
    {
        List<ResourceRow.Resource> array = new ArrayList<ResourceRow.Resource>();

        for (Afspraak afspraak : afspraken) // we doen nu gwn 1 dag, dit is het dashboard. fck jou jurryt.
        {
            array.add(makeResourceRow(afspraak));
        }

        populateLinearLayout(uurView, new ResourceAdapter(array.toArray(new ResourceRow.Resource[array.size()])));
    }

    private ResourceRow.Resource makeResourceRow(Afspraak a)
    {
        String tijd = a.Start.toString("HH:mm") + " - " + a.Einde.toString("HH:mm");

        return new ResourceRow.Resource(a.getVakken(), a.getLokalen(), a.getDocenten(), tijd, a.valtUit());
    }

}
