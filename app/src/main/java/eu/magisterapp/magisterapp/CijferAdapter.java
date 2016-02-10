package eu.magisterapp.magisterapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;

import eu.magisterapp.magisterapi.Cijfer;
import eu.magisterapp.magisterapi.CijferList;
import eu.magisterapp.magisterapi.Vak;

/**
 * Created by max on 29-12-15.
 */

/**
 * Dit bestand niet gebruiken voor het dashboard. die recentcijfers zijn heel anders dan normale cijfers.
 */
public class CijferAdapter extends RecyclerView.Adapter<CijferAdapter.ViewHolder>
{
    private CijferList cijfers;

	private Context context;

    private int[] colorsVoldoende;
    private int[] colorsOnVoldoende;

    public CijferAdapter(Context context)
    {
        super();

		this.context = context;

        colorsVoldoende = context.getResources().getIntArray(R.array.cijfer_colors_voldoende);
        colorsOnVoldoende = context.getResources().getIntArray(R.array.cijfer_colors_onvoldoende);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView gemiddelde;
        TextView vak;
        TextView cijfer;
        TextView omschrijving;

        View background;
        View cijferHeaderContainer;

        public ViewHolder(RelativeLayout row)
        {
            super(row);

            gemiddelde = (TextView) row.findViewById(R.id.cijfer_header_gemiddelde);
            vak = (TextView) row.findViewById(R.id.cijfer_header_vak);
            cijfer = (TextView) row.findViewById(R.id.cijfer);
            omschrijving = (TextView) row.findViewById(R.id.cijfer_omschrijving);

            background = row.findViewById(R.id.cijfer_background);
            cijferHeaderContainer = row.findViewById(R.id.cijfer_header_container);
        }
    }

    public void setData(CijferList cijfers)
    {
        if (cijfers == null) return;

        Collections.sort(cijfers, new Comparator<Cijfer>() {
            @Override
            public int compare(Cijfer lhs, Cijfer rhs) {
                int alfabetisch = lhs.Vak.Omschrijving.compareToIgnoreCase(rhs.Vak.Omschrijving);

                if (alfabetisch != 0) return alfabetisch;

                // voor het geval omschrijving niet beschikbaar is.
                int vakId = lhs.Vak.Id - rhs.Vak.Id;

                if (vakId != 0) return vakId;

                return lhs.CijferId - rhs.CijferId;
            }
        });

        this.cijfers = cijfers;

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        RelativeLayout row = (RelativeLayout) inflater.inflate(viewType, parent, false);

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

        final Cijfer cijfer = cijfers.get(position);

        if (holder.gemiddelde != null)
        {
            holder.gemiddelde.setText(String.format("%.1f", cijfer.Vak.getGemiddelde()));
            holder.vak.setText(cijfer.Vak.Afkorting);
        }

        int color = getColorForVak(cijfer.Vak, cijfer.IsVoldoende);

        holder.background.setBackgroundColor(color);

        holder.cijfer.setText(cijfer.CijferStr);
        holder.omschrijving.setText(cijfer.info.KolomOmschrijving);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context.getApplicationContext(), CijferDetail.class);
				intent.putExtra("cijfer", cijfer);
				context.startActivity(intent);
			}
		});
    }

    @Override
    public int getItemCount() {
        return cijfers == null ? 0 : cijfers.size();
    }

    private int getColorForVak(Vak vak, boolean voldoende)
    {
        int length = colorsVoldoende.length;

        return voldoende ? colorsVoldoende[vak.Id % length] : colorsOnVoldoende[vak.Id % length];
    }
}
