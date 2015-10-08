package eu.magisterapp.magister;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class CijfersFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cijfers, container, false);

        RecyclerView cijferContainer = (RecyclerView) view.findViewById(R.id.cijfer_container);

        ResourceAdapter adapter = new ResourceAdapter(getCijfers());

        cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        cijferContainer.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) (view.findViewById(R.id.toolbar));
        toolbar.setTitle(getResources().getString(R.string.alle_cijfers));

        ((Main) getActivity()).changeTitle(getResources().getString(R.string.alle_cijfers));

        return view;
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
