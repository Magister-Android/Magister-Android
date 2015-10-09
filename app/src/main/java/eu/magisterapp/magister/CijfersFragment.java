package eu.magisterapp.magister;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author  Max Verbeek <m4xv3rb33k@gmail.com>
 * @version 1.0
 * @since   24-9-15
 */
public class CijfersFragment extends Fragment
{
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_cijfers, container, false);

		RecyclerView cijferContainer = (RecyclerView) view.findViewById(R.id.cijfer_container);

		ResourceAdapter adapter = new ResourceAdapter(getCijfers());

		cijferContainer.setLayoutManager(new LinearLayoutManager(getContext()));
		cijferContainer.setAdapter(adapter);

		return view;
	}

	/**
	 * Vraag de cijfers op
	 * @return ResourceRow.Resource[]
	 */
	private ResourceRow.Resource[] getCijfers()
	{
		// TODO API stuff om de cijfers echt op te vragen
		return new ResourceRow.Resource[] {
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
			new ResourceRow.Resource("Informatica", "8,7", "M. de Krosse", "2 uur geleden"),
		};
	}
}
