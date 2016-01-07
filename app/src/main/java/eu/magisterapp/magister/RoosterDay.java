package eu.magisterapp.magister;

import android.content.Context;
import android.os.Bundle;
import android.provider.*;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import eu.magisterapp.magisterapi.AfspraakCollection;

/**
 * Created by max on 7-1-16.
 */
public class RoosterDay extends Fragment {

    public static final String ARGUMENTS_KEY = "afspraken";

    private AfspraakCollection afspraken;

    private RecyclerView recyclerView;
    private ResourceAdapter adapter;
    private TextView dayTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.rooster_day, container);

        Bundle arguments = getArguments();

        afspraken = (AfspraakCollection) arguments.getSerializable(ARGUMENTS_KEY);

        dayTitle = (TextView) view.findViewById(R.id.rooster_day_title);
        recyclerView = (RecyclerView) view.findViewById(R.id.rooster_day_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new ResourceAdapter(afspraken));

        dayTitle.setText(getTitle(afspraken.getFirstDay()));

        return view;
    }

    private CharSequence getTitle(DateTime first)
    {
        if (Days.daysBetween(first, DateTime.now()).getDays() > 1)
        {
            // gisteren, vandaag, morgen
            return DateUtils.getRelativeTimeSpanString(first.getMillis(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
        }

        // Donderdag 7 januari, 2016
        return first.toString("EEEE d MMMM, YYYY");
    }
}
