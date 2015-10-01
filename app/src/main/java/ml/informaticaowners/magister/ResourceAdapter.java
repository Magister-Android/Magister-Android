package ml.informaticaowners.magister;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ResourceAdapter extends ArrayAdapter<ResourceRow.Resource>
{
    /**
     * Created by max on 29-9-15.
     */
    public ResourceRow.Resource[] resources;

    public ResourceAdapter(Context context, ResourceRow.Resource[] resources)
    {
        super(context, 0, resources);
        this.resources = resources;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ResourceRow.Resource row = getItem(position);

        int resourceId = row.warning ? R.layout.resource_row_warning : R.layout.resource_row;

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }

        TextView vak = (TextView) convertView.findViewById(R.id.text_vak);
        TextView title = (TextView) convertView.findViewById(R.id.text_title);
        TextView docent = (TextView) convertView.findViewById(R.id.text_docent);
        TextView time = (TextView) convertView.findViewById(R.id.text_time);

        vak.setText(row.vak);
        title.setText(row.title);
        docent.setText(row.docent);
        time.setText(row.time);

        return convertView;
    }
}
