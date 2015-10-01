package ml.informaticaowners.magister;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;


public class DashboardFragment extends Fragment
{
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Context c = getActivity();

        LinearLayout uurView = (LinearLayout) view.findViewById(R.id.volgende_uur_container);
        populateLinearLayout(uurView, new ResourceAdapter(c, getTestUren()));

        LinearLayout cijferView = (LinearLayout) view.findViewById(R.id.laatste_cijfers_container);
        populateLinearLayout(cijferView, new ResourceAdapter(c, getTestCijfers()));

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

    protected void populateLinearLayout(LinearLayout layout, Adapter adapter)
    {
        int count = adapter.getCount();

        for (int i = 0; i < count; i++)
        {
            layout.addView(adapter.getView(i, null, null));
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

}
