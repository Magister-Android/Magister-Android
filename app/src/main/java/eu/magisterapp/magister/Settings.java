package eu.magisterapp.magister;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import eu.magisterapp.magister.Storage.DataFixer;


public class Settings extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		final SharedPreferences settings = getSharedPreferences(MagisterApp.PREFS_NAME, 0);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				// TODO Ik weet niet of dit een goede manier is, dit stopt de activity, misschien kunnen we beter terug in de backstack
			}
		});

		final EditText username = (EditText) findViewById(R.id.username_input);
		final EditText password = (EditText) findViewById(R.id.password_input);
		final EditText school = (EditText) findViewById(R.id.school_input);

		Button button = (Button) findViewById(R.id.login_button);

		button.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(MagisterApp.PREFS_USERNAME, username.getText().toString());
				editor.putString(MagisterApp.PREFS_PASSWORD, password.getText().toString());
				editor.putString(MagisterApp.PREFS_SCHOOL, school.getText().toString());

				editor.apply();

				((MagisterApp) getApplication()).notifyCredentialsUpdated();

				finish();
			}
		});

		username.setText(settings.getString("username", ""));
		password.setText(settings.getString("password", ""));
		school.setText(settings.getString("school", ""));

	}


}
