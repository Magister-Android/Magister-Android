package ml.informaticaowners.magister;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder>
{
    /**
     * Created by max on 24-9-15.
     */

    public ResourceRow.Resource[] data;

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

    public ResourceAdapter(ResourceRow.Resource[] rijenMetShit)
    {
        data = rijenMetShit;
    }

    @Override
    public ResourceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        LinearLayout row = (LinearLayout) inflater.inflate(R.layout.resource_row, parent, false);

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
}
