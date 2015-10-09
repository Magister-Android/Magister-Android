package eu.magisterapp.magister;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author  Max Verbeek <m4xv3rb33k@gmail.com>
 * @version 1.0
 * @since   24-9-15
 */
public class RoosterFragment extends Fragment
{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
				// Inflate the layout for this fragment
				return inflater.inflate(R.layout.fragment_rooster, container, false);
		}
}
