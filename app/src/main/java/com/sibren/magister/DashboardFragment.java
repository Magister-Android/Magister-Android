package com.sibren.magister;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        Integer cijferContainer = R.id.volgende_uur_container;

        // We gebruiken hier gewoon nog een fragment. Ik weet niet of dit een heel goed idee is.
        // Misschien moeten we later iets van een List/Recycler View maken ofzo.
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        Bundle args = new Bundle();
        args.putString("Dit worden", "de dynamische parameters");

        ResourceRow rij = new ResourceRow();
        rij.setArguments(args);

        transaction.add(cijferContainer, rij);
        transaction.add(cijferContainer, rij);
        transaction.add(cijferContainer, rij);
        transaction.add(cijferContainer, rij);
        transaction.commit();

        return view;
    }
}
