package eu.magisterapp.magister;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;

import eu.magisterapp.magisterapi.Cijfer;
import eu.magisterapp.magisterapi.CijferList;

/**
 * Created by max on 29-12-15.
 */

/**
 * Dit bestand niet gebruiken voor het dashboard. die recentcijfers zijn heel anders dan normale cijfers.
 */
public class CijferAdapter extends RecyclerView.Adapter<CijferAdapter.ViewHolder>
{
    private CijferList cijfers;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView gemiddelde;
        TextView vak;
        TextView cijfer;
        TextView omschrijving;

        public ViewHolder(LinearLayout row)
        {
            super(row);

            gemiddelde = (TextView) row.findViewById(R.id.cijfer_header_gemiddelde);
            vak = (TextView) row.findViewById(R.id.cijfer_header_vak);
            cijfer = (TextView) row.findViewById(R.id.cijfer);
            omschrijving = (TextView) row.findViewById(R.id.cijfer_omschrijving);
        }
    }

    public void setData(CijferList cijfers)
    {
        if (cijfers == null) return;

        // sort op vak - nodig voor headers
        Collections.sort(cijfers, new Comparator<Cijfer>() {
            @Override
            public int compare(Cijfer lhs, Cijfer rhs) {
                return lhs.Vak.Id - rhs.Vak.Id;
            }
        });

        this.cijfers = cijfers;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        LinearLayout row = (LinearLayout) inflater.inflate(viewType, parent, false);

        return new ViewHolder(row);
    }

    @Override
    public int getItemViewType(int position) {
        if (cijfers.size() == 0) return R.layout.cijfer_row_header;

        if (position == 0) return R.layout.cijfer_row_header;

        // Als het vak van het vorige cijfer anders is, is het een header.
        if (! cijfers.get(position).Vak.Id.equals(cijfers.get(position - 1).Vak.Id))
        {
            return R.layout.cijfer_row_header;
        }

        return R.layout.cijfer_row;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Cijfer cijfer = cijfers.get(position);

        if (holder.gemiddelde != null)
        {
            holder.gemiddelde.setText(String.format("%.1f", cijfer.Vak.getGemiddelde()));
            holder.vak.setText(cijfer.getVak());
        }

        holder.cijfer.setText(cijfer.CijferStr);
        holder.omschrijving.setText(cijfer.info.KolomOmschrijving);
    }

    @Override
    public int getItemCount() {
        return cijfers == null ? 0 : cijfers.size();
    }
}
