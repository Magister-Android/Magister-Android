package com.sibren.magister;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by max on 24-9-15.
 */
public class ResourceRow extends Fragment
{
    public static class Resource
    {
        public String vak;
        public String title;
        public String docent;
        public String time;

        public Resource(String vak, String title, String docent, String time)
        {
            this.vak = vak;
            this.title = title;
            this.docent = docent;
            this.time = time;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.resource_row, container, false);
    }
}
