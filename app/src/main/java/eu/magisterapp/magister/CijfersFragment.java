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
import java.util.ArrayList;

import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.Cijfer;
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

        adapter = new ResourceAdapter(getCijfers());

        cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        cijferContainer.setAdapter(adapter);

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
                displayAlert("nemoeder");
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
        ArrayList<ResourceRow.Resource> list = new ArrayList<>();

        for (Cijfer cijfer : cijfers)
        {
            list.add(convertToResource(cijfer));
        }

        adapter.setRijenMetShit(list.toArray(new ResourceRow.Resource[list.size()]));
    }

    private ResourceRow.Resource convertToResource(Cijfer cijfer)
    {
        return new ResourceRow.Resource(cijfer.Vak.Omschrijving, cijfer.CijferStr, cijfer.Docent, cijfer.DatumIngevoerd.toString("yyyy-MM-dd"));
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
