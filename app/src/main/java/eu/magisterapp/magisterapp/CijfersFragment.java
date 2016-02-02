package eu.magisterapp.magisterapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapi.CijferList;


public class CijfersFragment extends TitledFragment implements DataFixer.OnResultInterface
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

        if (mNeedsUpdate) updateCijferList(cijfers);

        return view;
    }

    @Override
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
