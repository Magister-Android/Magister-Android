package eu.magisterapp.magisterapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapp.sync.Refresh;
import eu.magisterapp.magisterapp.sync.RefreshHolder;


public class CijfersFragment extends TitledFragment implements Refreshable
{
    private RecyclerView cijferContainer;
    private CijferAdapter adapter;

    private CijferList cijfers;

    private boolean mNeedsUpdate = false;

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


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNeedsUpdate) updateCijferList(cijfers);
    }

    @Override
    public RefreshHolder[] getRefreshers(MagisterApp app) {

        return new RefreshHolder[] { RefreshHolder.getCijferRefresh(app) };
    }

    @Override
    public void readDatabase(DataFixer data) throws IOException {
        final CijferList cijfers = data.getCijfersFromCache();

        if (cijferContainer == null)
        {
            this.cijfers = cijfers;
            mNeedsUpdate = true;

            return;
        }

        cijferContainer.post(new Runnable() {
            @Override
            public void run() {
                updateCijferList(cijfers);
            }
        });
    }

    public void onResult(DataFixer.ResultBundle result) {
		if(result.cijfers == null)
			return;

        if (! isVisible())
        {
            cijfers = result.cijfers;
            mNeedsUpdate = true;

            return;
        }

        updateCijferList(result.cijfers);
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
