package eu.magisterapp.magisterapp;

import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.magisterapp.magisterapi.Afspraak;
import eu.magisterapp.magisterapi.AfspraakList;
import eu.magisterapp.magisterapi.Displayable;


public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder>
{
    /**
     * Created by max on 24-9-15.
     */

    public List<? extends Displayable> displayables;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView vak;
        public TextView title;
        public TextView docent;
        public TextView time;

        public ViewHolder(RelativeLayout row)
        {
            super(row);

            vak = (TextView) row.findViewById(R.id.text_vak);
            title = (TextView) row.findViewById(R.id.text_title);
            docent = (TextView) row.findViewById(R.id.text_docent);
            time = (TextView) row.findViewById(R.id.text_time);
        }
    }

    public ResourceAdapter(List<? extends Displayable> displayables)
    {
        this.displayables = displayables;
    }

    public ResourceAdapter()
    {
        displayables = new ArrayList<>();
    }

    public void swap(List<? extends Displayable> newData)
    {
        displayables = newData;

        notifyDataSetChanged();
    }

    @Override
    public ResourceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        RelativeLayout row = (RelativeLayout) inflater.inflate(R.layout.resource_row, parent, false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Displayable row = displayables.get(position);

        switch (row.getType())
        {
            case INVALID:
                holder.vak.setPaintFlags(holder.vak.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.docent.setPaintFlags(holder.docent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.time.setPaintFlags(holder.time.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                holder.vak.setTextColor(ContextCompat.getColor(holder.vak.getContext(), R.color.uitval_text));
                holder.title.setTextColor(ContextCompat.getColor(holder.title.getContext(), R.color.uitval_text));
                break;

            case NOTICE:
                holder.title.setTextColor(ContextCompat.getColor(holder.title.getContext(), R.color.accent));
                break;

            case WARNING:
                holder.title.setTextColor(ContextCompat.getColor(holder.title.getContext(), R.color.onvoldoende_text));
        }

        holder.vak.setText(row.getVak());
        holder.title.setText(row.getTitle());
        holder.docent.setText(row.getDocent());

        if (row.getTimeInstance().getMillis() - System.currentTimeMillis() < 3600 * 1000)
        {
            // Dit geldt dus altijd voor cijfers, want die hebben altijd een timestamp uit het verleden.
            holder.time.setText(DateUtils.getRelativeTimeSpanString(row.getTimeInstance().getMillis(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        }

        else
        {
            holder.time.setText(row.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return displayables.size();
    }

}