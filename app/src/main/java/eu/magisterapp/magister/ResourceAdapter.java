package eu.magisterapp.magister;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakCollection;
import eu.magisterapp.magisterapi.Cijfer;
import eu.magisterapp.magisterapi.CijferList;


public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder>
{
    /**
     * Created by max on 24-9-15.
     */

    public List<DataHolder> data;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView vak;
        public TextView title;
        public TextView docent;
        public TextView time;

        public ViewHolder(LinearLayout row)
        {
            super(row);

            vak = (TextView) row.findViewById(R.id.text_vak);
            title = (TextView) row.findViewById(R.id.text_title);
            docent = (TextView) row.findViewById(R.id.text_docent);
            time = (TextView) row.findViewById(R.id.text_time);
        }
    }

    private class DataHolder
    {
        public final String vak;
        public final String title;
        public final String docent;
        public final String time;

        public DataHolder(String vak, String title, String docent, String time)
        {
            this.vak = vak;
            this.title = title;
            this.docent = docent;
            this.time = time;
        }

        public DataHolder(String vak, String title, String docent, DateTime start, DateTime end)
        {
            String time = start.toString("HH:mm") + " - " + end.toString("HH:mm");

            this.vak = vak;
            this.title = title;
            this.docent = docent;
            this.time = time;
        }
    }

    public ResourceAdapter(CijferList cijferList)
    {
        data = createCijferHolder(cijferList);
    }

    public ResourceAdapter(AfspraakCollection afspraakCollection)
    {
        data = new ArrayList<>();


    }

    public List<DataHolder> createCijferHolder(CijferList cijfers)
    {
        List<DataHolder> shit = new ArrayList<>();

        for (Cijfer cijfer : cijfers)
        {
            shit.add(new DataHolder(cijfer.Vak.Omschrijving, cijfer.CijferStr, cijfer.Docent, cijfer.DatumIngevoerd.toString("yyyy-MM-dd")));
        }

        return shit;
    }

    public List<DataHolder> createAfspraakHolder(AfspraakCollection afspraken)
    {
        List<DataHolder> shit = new ArrayList<>();

        for (Afspraak afspraak : afspraken)
        {
            shit.add(new DataHolder(afspraak.Omschrijving, afspraak.getLokalen(), afspraak.getDocenten(), afspraak.Start, afspraak.Einde));
        }

        return shit;
    }

    public void swap(CijferList cijfers)
    {
        data.clear();
        data.addAll(createCijferHolder(cijfers));
        notifyDataSetChanged();
    }

    public void swap(AfspraakCollection afspraken)
    {
        data.clear();
        data.addAll(createAfspraakHolder(afspraken));
        notifyDataSetChanged();
    }

    @Override
    public ResourceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        int resourceType = data[viewType].warning ? R.layout.resource_row_warning : R.layout.resource_row;

        LinearLayout row = (LinearLayout) inflater.inflate(resourceType, parent, false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ResourceRow.Resource row = data[position];

        holder.vak.setText(row.vak);
        holder.title.setText(row.title);
        holder.docent.setText(row.docent);
        holder.time.setText(row.time);

    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public void setRijenMetShit(ResourceRow.Resource[] shit)
    {
        data = shit;
        notifyDataSetChanged();
    }


}