package eu.magisterapp.magister;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

        public ViewHolder(LinearLayout row)
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

        int resourceType;

        switch (displayables.get(viewType).getType())
        {

            case INVALID:
            case NOTICE:
            case WARNING:
                resourceType = R.layout.resource_row_warning;
                break;

            case NORMAL:
            default:
                resourceType = R.layout.resource_row;
                break;
        }

        LinearLayout row = (LinearLayout) inflater.inflate(resourceType, parent, false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Displayable row = displayables.get(position);

        holder.vak.setText(row.getVak());
        holder.title.setText(row.getTitle());
        holder.docent.setText(row.getDocent());
        holder.time.setText(row.getTime());
    }

    @Override
    public int getItemCount() {
        return displayables.size();
    }

}