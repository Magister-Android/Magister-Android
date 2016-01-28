package eu.magisterapp.magisterapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapi.CijferList;


public class CijfersFragment extends TitledFragment implements OnMainRefreshListener
{
    private RecyclerView cijferContainer;
    private CijferAdapter adapter;

    private CijferList cijfers;

    private boolean refreshed = false;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view != null) return view;

        view = inflater.inflate(R.layout.fragment_cijfers, container, false);

        cijferContainer = (RecyclerView) view.findViewById(R.id.cijfer_container);
        adapter = new CijferAdapter(getContext());

        cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        cijferContainer.setAdapter(adapter);

        if (refreshed) onPostRefresh();

        return view;
    }

    @Override
    public void onRefreshed(MagisterApp app) {

        try
        {
            cijfers = app.getDataStore().getCijfersFromCache();

            refreshed = true;
        }

        catch (IOException e)
        {
            e.printStackTrace();

            // Dit komt voor als er een error ontstaat tijdens het deserializen,
            // of als je opslag corrupt is. Alleen low-level shit.
            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPostRefresh() {
        refreshed = false;
        updateCijferList(cijfers);
    }

    @Override
    public Object[] quickUpdate(MagisterApp app) {

        DataFixer data = app.getDataStore();

        try {
            return new Object[] {
                data.getCijfersFromCache()
            };
        }

        catch (IOException e)
        {
            // jemoeder
        }

        return new Object[0];
    }

    @Override
    public void onQuickUpdated(Object... result) {
        if (result.length != 1) return;

        updateCijferList((CijferList) result[0]);
    }

    public void updateCijferList(CijferList cijfers)
    {
        adapter.setData(cijfers);
    }

		public void deleteView()
		{
			view = null;
		}
}
