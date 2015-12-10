package eu.magisterapp.magister;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.MagisterAPI;


public class CijfersFragment extends TitledFragment
{
    private RecyclerView cijferContainer;
    private ResourceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cijfers, container, false);

        cijferContainer = (RecyclerView) view.findViewById(R.id.cijfer_container);

        cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));

        setTitle("Alle Cijfers");

        getCijfersFromAPI();

        return view;
    }

    private void getCijfersFromAPI()
    {
        new HaalCijfersOpTask().execute((Void[]) null);
    }

    private class HaalCijfersOpTask extends AsyncTask<Void, Void, Void>
    {
        private CijferList cijfers;
        private String message;
        private MagisterAPI api = ((Main) getContext()).api;

        @Override
        protected Void doInBackground(Void... params) {

            try
            {
                cijfers = api.getCijfers();
            }

            catch (IOException e)
            {
                if (e instanceof BadResponseException)
                {
                    message = e.getMessage();
                }

                else
                {
                    message = "Kon geen cijfers ophalen.";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (cijfers == null)
            {
                Alerts.notify(getActivity(), "Er zijn geen cijfers gevonden").show();
            }

            else
            {
                if (adapter == null) adapter = new ResourceAdapter(cijfers);

                else adapter.swap(cijfers);
            }

        }
    }

    private void updateCijferList(CijferList cijfers)
    {
        adapter.swap(cijfers);
    }
}
