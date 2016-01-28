package eu.magisterapp.magisterapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.Utils;


public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener
{
	private boolean swappedSinceRefresh = false;
	private Long refreshedSince = 0L;

	DrawerLayout mDrawerLayout;
	NavigationView navigationView;
	Toolbar toolbar;

	ProgressDialog progress;

	SwipeRefreshLayout mSwipeRefreshLayout;

	Fragments currentFragment = Fragments.DASHBOARD;

	static int fragCounter = 0;

	private enum Fragments
	{
		DASHBOARD(new DashboardFragment(), R.string.app_name, R.id.nav_dashboard), // 0
		ROOSTER(new RoosterFragment(), R.string.nav_rooster, R.id.nav_rooster), // 1
		CIJFERS(new CijfersFragment(), R.string.nav_cijfers, R.id.nav_cijfers); // 2

		public final Fragment instance;
		public final int title;
		public final int navId;
		public final int id;

		Fragments(Fragment fragment, int title, int navId)
		{
			instance = fragment;
			this.title = title;
			this.navId = navId;
			id = fragCounter++;
		}

		public static void pushUpdate(MagisterApp app)
		{
			for (Fragments fragment : values())
			{
				if (fragment.instance instanceof OnMainRefreshListener)
					((OnMainRefreshListener) fragment.instance).onRefreshed(app);
			}
		}
	}

	private final SparseArray<Fragments> fragmentMap = new SparseArray<Fragments>() {{
		put(Fragments.DASHBOARD.id, Fragments.DASHBOARD);
		put(Fragments.ROOSTER.id, Fragments.ROOSTER);
		put(Fragments.CIJFERS.id, Fragments.CIJFERS);
	}};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
		{
			currentFragment = fragmentMap.get(savedInstanceState.getInt("current_fragment", 0));
		}

		setContentView(R.layout.activity_main);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle adbToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.nav_open, R.string.nav_close)
		{
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				// zorgt ervoor dat hij niet draait en in een pijltje verandert.
				super.onDrawerSlide(drawerView, 0);
			}
		};

		mDrawerLayout.setDrawerListener(adbToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		navigationView = (NavigationView) findViewById(R.id.nav_view);

		if (navigationView != null)
		{
			navigationView.setNavigationItemSelectedListener(this);
		}

		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
		mSwipeRefreshLayout.setOnRefreshListener(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		adbToggle.syncState();

		new LoginFixer(this).startLoginSequence();
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {

		boolean handled = handleAction(item.getItemId());

		mDrawerLayout.closeDrawers();

		return handled;
	}

	@Override
	public void onRefresh() {

		mSwipeRefreshLayout.setRefreshing(true);

		if (currentFragment == Fragments.ROOSTER && ! swappedSinceRefresh)
		{
			// andere content boeit niet echt, update alleen rooster.
			((RoosterFragment) Fragments.ROOSTER.instance).selfUpdate(mSwipeRefreshLayout);
		}

		new OphaalTask().execute();
	}

	public class OphaalTask extends AsyncTask<Void, Object, IOException>
	{
		@Override
		protected IOException doInBackground(Void... params) {

			MagisterApp app = getMagisterApplication();
			DataFixer data = app.getDataStore();

			if (currentFragment.instance instanceof OnMainRefreshListener && currentFragment.instance.isVisible())
				publishProgress(((OnMainRefreshListener) currentFragment.instance).quickUpdate(app));


			try
			{
				RoosterFragment rfrag = (RoosterFragment) Fragments.ROOSTER.instance;

				if (currentFragment == Fragments.ROOSTER && rfrag.van != null && rfrag.tot != null)
				{
					data.fetchOnlineAfspraken(rfrag.van, rfrag.tot);
				}

				DateTime van = Utils.now();
				DateTime tot = van.plusDays(app.getDaysInAdvance());

				data.fetchOnlineAfspraken(van, tot);
				data.fetchOnlineCijfers();
				data.fetchOnlineRecentCijfers();

				Fragments.pushUpdate(app);

				return null;
			}

			catch (IOException e)
			{
				// Als er een error ontstaat wordt die hier gereturnt, zodat hij
				// in onPostExecute wordt afgehandelt.
				return e;
			}
		}

		@Override
		protected void onProgressUpdate(Object... values) {

			for (Fragments fragment : Fragments.values())
			{
				if (fragment.instance instanceof OnMainRefreshListener && fragment.instance.isVisible())
					((OnMainRefreshListener) fragment.instance).onQuickUpdated(values);
			}
		}

		@Override
		protected void onPostExecute(IOException e) {

			if (e != null)
			{
				e.printStackTrace();

				handleError(e);
			}

			else if ((currentFragment.instance instanceof OnMainRefreshListener) && currentFragment.instance.isVisible())
			{
				// Huidige fragment heeft een view, en is implement een method die UI update.
				((OnMainRefreshListener) currentFragment.instance).onPostRefresh();
			}

			// Zorg ervoor dat de refreshlayout stopt met de refresh animatie na het refreshen.
			mSwipeRefreshLayout.setRefreshing(false);

			// Update swapped status
			swappedSinceRefresh = false;

			// Update timestamp
			refreshedSince = System.currentTimeMillis();
		}
	}

	public void handleError(IOException e)
	{
		Snackbar snackbar;
		View view = findViewById(R.id.coordinator_layout);

		if (view == null) return;

		if (e instanceof BadResponseException)
			snackbar = Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG);
		else if (e instanceof NoInternetException)
			snackbar = Snackbar.make(view, R.string.no_internet, Snackbar.LENGTH_LONG).setAction("INTERNET", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
				}
			});
		else
			snackbar = Snackbar.make(view, R.string.error_generic, Snackbar.LENGTH_LONG);

		snackbar.show();
	}

	public void postLogin()
	{
		setFragment(currentFragment, false);
		navigationView.getMenu().findItem(currentFragment.navId).setChecked(true);

		mSwipeRefreshLayout.post(new Runnable() {
			@Override
			public void run() {
				onRefresh();
			}
		});
	}

	public MagisterApp getMagisterApplication()
	{
		return (MagisterApp) getApplication();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("current_fragment", currentFragment.id);
	}

	@Override
	public void onBackPressed()
	{
		if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
			mDrawerLayout.closeDrawers();
		else if (currentFragment != Fragments.DASHBOARD) {
			navigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
			setFragment(Fragments.DASHBOARD);
		}
		else
			super.onBackPressed();
	}

	public void changeTitle(String title)
	{
		if (getSupportActionBar() == null)
		{
			return;
		}

		getSupportActionBar().setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return handleAction(item.getItemId()) || super.onOptionsItemSelected(item);
	}

	public boolean handleAction(int actionId)
	{
		switch (actionId)
		{
			case R.id.nav_dashboard:
				setFragment(Fragments.DASHBOARD);
				return true;

			case R.id.nav_rooster:
				setFragment(Fragments.ROOSTER);
				return true;

			case R.id.nav_cijfers:
				setFragment(Fragments.CIJFERS);
				return true;

			case R.id.nav_settings:
			case R.id.action_settings:
				startActivity(new Intent(this, Settings.class));
				return true;

			case R.id.action_logout:
				getMagisterApplication().getApi().disconnect();
				getMagisterApplication().voidCredentails();
				finish();
				startActivity(new Intent(this, this.getClass()));
				return true;

			default:
				return false;
		}
	}

	public void setFragment(Fragments fragment)
	{
		setFragment(fragment, false);
	}

	public void setFragment(Fragments fragment, boolean backstack)
	{
		int fragmentContainer = R.id.fragment_container;

		currentFragment = fragment;

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		transaction.replace(fragmentContainer, fragment.instance);

		if (backstack) transaction.addToBackStack(null);

		setTitle(getString(fragment.title));

		transaction.commit();

		swappedSinceRefresh = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (progress != null)
		{
			progress.dismiss();
			progress = null;
		}
	}
}
