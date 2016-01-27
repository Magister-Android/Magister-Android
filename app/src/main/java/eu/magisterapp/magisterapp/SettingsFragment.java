package eu.magisterapp.magisterapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by sibren on 1/27/16.
 */
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(final Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
