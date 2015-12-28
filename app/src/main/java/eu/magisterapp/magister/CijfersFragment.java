package eu.magisterapp.magister;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.*;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import eu.magisterapp.magister.Storage.DataFixer;
import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.MagisterAPI;


public class CijfersFragment extends TitledFragment
{
    private RecyclerView cijferContainer;
    private ResourceAdapter adapter;
    private MagisterApp app;
    private DataFixer data;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_cijfers, container, false);

        cijferContainer = (RecyclerView) view.findViewById(R.id.cijfer_container);
        adapter = new ResourceAdapter();

        cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        cijferContainer.setAdapter(adapter);

        app = ((Main) getActivity()).getMagisterApplication();
        data = app.getDataStore();

        setTitle("Alle Cijfers");

        return view;
    }

    private class CijferFixerTask extends AsyncTask<Void, CijferList, Boolean>
    {
        private boolean internetError = false;
        private CijferList cijfers;
        private IOException e;

        @Override
        protected Boolean doInBackground(Void... params) {

            try
            {
                publishProgress(data.getCijfersFromCache());
            }

            catch (IOException e)
            {
                // het is jammer. geen progress voor jou bitch.
            }

            if (! app.hasInternet())
            {
                internetError = true;
            }

            else
            {
                try
                {
                    cijfers = data.getCijfers();

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
                cijfers = data.getCijfersFromCache();

                return true;
            }

            catch (IOException e)
            {
                this.e = e;

                return false;
            }
        }

        @Override
        protected void onProgressUpdate(CijferList... values) {

            updateCijferList(values[0]);

        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (internetError)
            {
                Alerts.notify(getActivity(), R.string.no_internet_cache).setAction("INTERNET", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    }
                }).show();
            }

            if (success)
            {
                updateCijferList(cijfers);
            }

            if (e != null)
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
        }
    }

    public void updateCijferList(CijferList cijfers)
    {

    }
}
