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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cijfers, container, false);

        RecyclerView cijferContainer = (RecyclerView) view.findViewById(R.id.cijfer_container);

        ResourceAdapter adapter = new ResourceAdapter(getCijfers());

        cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        cijferContainer.setAdapter(adapter);

        setTitle("Alle Cijfers");

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

            catch (IOException | ParseException | JSONException e)
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

            if (cijfers != null)
            {
                displayAlert(message);
            }

            else
            {
                updateCijferList(cijfers);
            }

        }
    }

    private void displayAlert(String message)
    {
        new AlertDialog.Builder(getContext())
                .setTitle("Je hebt kutcijfers")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // jemoeder
                    }
                });

    }

    private void updateCijferList(CijferList cijfers)
    {

    }

    private ResourceRow.Resource[] getCijfers()
    {
        return new ResourceRow.Resource[] {

            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
            new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),

        };
    }
}
