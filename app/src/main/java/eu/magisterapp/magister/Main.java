package eu.magisterapp.magister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;


public class Main extends AppCompatActivity
{
	String[] items;
	ArrayAdapter<String> adapter;

	ListView vlist;
	DrawerLayout dlayout;
	Toolbar toolbar;
	int fragmentPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
		{
			fragmentPosition = savedInstanceState.getInt("current_fragment", 0);
		}

		setContentView(R.layout.activity_main);
		setupToolbar();
		dlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		items = getResources().getStringArray(R.array.nav_items);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		vlist = (ListView) findViewById(R.id.left_drawer);
		vlist.setAdapter(adapter);

		vlist.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position);
			}
		});

		doLoginSequence();
	}

	private void doLoginSequence()
	{
		// Als je nog niet ingelogd bent, maak een login scherm
		if (! ((MagisterApp) getApplication()).isAuthenticated())
		{
			createAuthDialog().show();
		}

		// Als je dat al wel bent, ga dan gwn verder naar het 1e fragment.
		else
		{
			postLogin();
		}
	}

	private void postLogin()
	{
		selectItem(fragmentPosition, false);
	}

	public AlertDialog createAuthDialog()
	{
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_login, null);

		return new AlertDialog.Builder(this)
				.setTitle(R.string.login)
				.setView(dialogView)
				.setCancelable(false)
				.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						TextView schoolView = (TextView) dialogView.findViewById(R.id.school_input);
						TextView usernameView = (TextView) dialogView.findViewById(R.id.username_input);
						TextView passwordView = (TextView) dialogView.findViewById(R.id.password_input);

						String school = schoolView.getText().toString();
						String username = usernameView.getText().toString();
						String password = passwordView.getText().toString();

						dialog.dismiss();

						login(school, username, password);
					}
				})
				.create();
	}

	public void login(String school, String username, String password)
	{
		Log.i("Logging in: ", "School: " + school + ", Username: " + username);

		if (! getMagisterApplication().hasInternet())
		{
			new AlertDialog.Builder(this)
					.setMessage(R.string.no_internet)
					.setCancelable(true)
					.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							doLoginSequence();
						}
					})
					.create().show();
		}

		else
		{
			new LoginTask().execute(school, username, password);
		}

	}

	private class LoginTask extends AsyncTask<String, Void, Boolean>
	{
		private ProgressDialog progress;

		private Main main;

		@Override
		protected void onPreExecute()
		{
			main = Main.this;

			progress = new ProgressDialog(Main.this);
			progress.setMessage(getString(R.string.login));
			progress.setIndeterminate(true);
			progress.setCancelable(false);

			progress.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {

			String school = params[0];
			String username = params[1];
			String password = params[2];

			if (main.getMagisterApplication().validateCredentials(school, username, password))
			{
				main.getMagisterApplication().updateCredentials(school, username, password);

				return true;
			}

			else
			{
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			progress.dismiss();

			if (! success)
			{
				new AlertDialog.Builder(main)
						.setTitle(R.string.login)
						.setMessage(R.string.credential_failure)
						.setCancelable(false)
						.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();

								doLoginSequence();
							}
						})
						.create().show();
			}

			else
			{
				postLogin();
			}
		}
	}

	public MagisterApp getMagisterApplication()
	{
		return (MagisterApp) getApplication();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("current_fragment", fragmentPosition);
	}

	@Override
	public void onBackPressed()
	{
		if(dlayout.isDrawerOpen(GravityCompat.START))
			dlayout.closeDrawers();
		else
			super.onBackPressed();
	}

	private void selectItem(int position)
	{
		selectItem(position, true);
	}

	private void selectItem(int position, boolean backstack)
	{
		fragmentPosition = position;

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		int container = R.id.fragment_container;

		switch (position)
		{
			case 1:
				transaction.replace(container, new RoosterFragment());
				break;

			case 2:
				transaction.replace(container, new CijfersFragment());
				break;
			case 0:
			default:
				transaction.replace(container, new DashboardFragment());
				break;
		}

		if (backstack)
		{
			transaction.addToBackStack(null);
		}

		transaction.commit();

		dlayout.closeDrawers();
	}

	public void changeTitle(String title)
	{
		if (toolbar == null)
		{
			return;
		}

		toolbar.setTitle(title);
	}

	private void setupToolbar(){
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout dlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle abdtoggle = new ActionBarDrawerToggle(this, dlayout, toolbar, R.string.nav_open, R.string.nav_close);
		dlayout.setDrawerListener(abdtoggle);

		// Set a correct drawer width
		ListView drawerList = (ListView) findViewById(R.id.left_drawer);

		DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerList.getLayoutParams();
		params.width = getDrawerWidth();

		drawerList.setLayoutParams(params);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		abdtoggle.syncState();
	}

	private int getDrawerWidth()
	{
		int screenwidth = getResources().getDisplayMetrics().widthPixels;

		TypedValue tv = new TypedValue();

		getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);

		int actionbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());

		return Math.min(screenwidth - actionbarHeight, actionbarHeight * 6);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
			case R.id.action_settings:
				Intent settingsactivity = new Intent(this, Settings.class);
				startActivity(settingsactivity);
				return true;

			case R.id.action_logout:
				getMagisterApplication().getApi().disconnect();
				getMagisterApplication().voidCredentails();
				startActivity(new Intent(this, this.getClass()));
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
