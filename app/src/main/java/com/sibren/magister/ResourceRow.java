package com.sibren.magister;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by max on 24-9-15.
 */
public class ResourceRow extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Maak een resource rij
        View view =  inflater.inflate(R.layout.fragment_resource_row, container, false);

        // Zet dingen in de text views.

        setTextOnView(view, R.id.text_vak, "Natuurkunde");
        setTextOnView(view, R.id.text_primary_resource, "K009");
        setTextOnView(view, R.id.text_docent, "C. Dopheide");
        setTextOnView(view, R.id.text_time, "8.30 - 9.30");

        return view;
    }

    private void setTextOnView(View view, Integer id, String text)
    {
        ((TextView) view.findViewById(id)).setText(text);
    }
}
