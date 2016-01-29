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
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.joda.time.DateTime;

import java.io.IOException;

import eu.magisterapp.magisterapp.Storage.DataFixer;
import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapi.Utils;


public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener
{
	private boolean swappedSinceRefresh = false;

	private static final int AFSPRAAK_REFRESH       = 0b0001;
	private static final int CIJFER_REFRESH         = 0b0010;
	private static final int RECENT_CIJFER_REFRESH  = 0b0100;
	private static final int NEXT_DAY_REFRESH       = 0b1000;

	private OphaalTask currentTask;

	DrawerLayout mDrawerLayout;
	NavigationView navigationView;
	Toolbar toolbar;

	ProgressDialog progress;

	SwipeRefreshLayout mSwipeRefreshLayout;

	FragmentView currentFragment = FragmentView.DASHBOARD;

	static int fragCounter = 0;

	private enum FragmentView
	{
		DASHBOARD(new DashboardFragment(), R.string.app_name, R.id.nav_dashboard, RECENT_CIJFER_REFRESH | NEXT_DAY_REFRESH), // 0
		ROOSTER(new RoosterFragment(), R.string.nav_rooster, R.id.nav_rooster, AFSPRAAK_REFRESH), // 1
		CIJFERS(new CijfersFragment(), R.string.nav_cijfers, R.id.nav_cijfers, CIJFER_REFRESH); // 2

		public final Fragment instance;
		public final int title;
		public final int navId;
		public final int id;
		public final int mask;

		FragmentView(Fragment fragment, int title, int navId, int refreshMask)
		{
			instance = fragment;
			this.title = title;
			this.navId = navId;
			id = fragCounter++;

			mask = refreshMask;
		}
	}

	private final SparseArray<FragmentView> fragmentMap = new SparseArray<FragmentView>() {{
		put(FragmentView.DASHBOARD.id, FragmentView.DASHBOARD);
		put(FragmentView.ROOSTER.id, FragmentView.ROOSTER);
		put(FragmentView.CIJFERS.id, FragmentView.CIJFERS);
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

        if (currentTask != null && (currentTask.ongoing & currentFragment.mask) == currentFragment.mask)
        {
            currentTask.doContinue();
        }

		else if (currentFragment == FragmentView.ROOSTER && ! swappedSinceRefresh)
		{
			// andere content boeit niet echt, update alleen rooster.
			((RoosterFragment) FragmentView.ROOSTER.instance).selfUpdate(mSwipeRefreshLayout);
		}

        else (currentTask = new OphaalTask()).execute();
	}

	public class OphaalTask extends AsyncTask<Void, DataFixer.ResultBundle, Void>
	{
		private boolean isQuick = true;
		private boolean finished = false;

		public int ongoing;

		private DataFixer.ResultBundle result = new DataFixer.ResultBundle();

		private MagisterApp app = getMagisterApplication();
		private DataFixer data = app.getDataStore();

		@Override
		protected Void doInBackground(Void... params) {

			// Dit zijn alle flags die aangeven welke delen gerefresht moeten worden.
			int mask = ongoing = currentFragment.mask;

			// Dit zijn alle flags die aangeven welke delen NIET gerefresht moeten worden.
			// We fixen ze alleen wel, zodat je sneller je shit hebt.
			int unnecessary = ~mask & (0b1111);

			try {
				quickRefresh(mask);

				publishProgress(result);

			} catch (IOException e) {
				// Jammer als er hier iets ontstaan.
				// TODO: misschien database nuken, zodat opslag niet meer faalt? Of misschien een berichtje sturen dat niks gecached kan worden.
				e.printStackTrace();
			}

			isQuick = false;

			try {
				longRefresh(mask);

				publishProgress(result);

			} catch (IOException e) {
				// Als hier een error is, is het wel legit balen. Dan is je internet gay o.i.d.
				e.printStackTrace();
				handleError(e);
			}

			// Zodra het nodige is gepublisht kunnen we net doen alsof deze task al klaar is.
			finished = true;

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mSwipeRefreshLayout.setRefreshing(false);
				}
			});

			// In werkelijkheid gaat hij nu verder met de andere dingen refreshen, die niet zichtbaar zijn.
			// VB: RoosterFragment is zichtbaar -> hij refresht nu cijfers.
			// VB2: DashboardFragment is zichtbaar -> hij refresht nu niks, want het dashboard had alles al nodig.

			// Geef status aan, zodat er kan worden bepaald of aan hetgene wat iemand wil refreshen gewerkt wordt.
			ongoing = unnecessary;

			try {
				longRefresh(unnecessary);

				if (! finished) publishProgress(result);
			} catch (IOException e) {
				e.printStackTrace();

                if (! finished) handleError(e);
			}

			ongoing = 0;

			return null;
		}

		private void quickRefresh(int toRefresh) throws IOException
		{
			if ((NEXT_DAY_REFRESH & toRefresh) == NEXT_DAY_REFRESH)
			{
				result.setAfspraken(data.getNextDayFromCache());

                toRefresh = toRefresh & ~NEXT_DAY_REFRESH;
			}

			else if ((AFSPRAAK_REFRESH & toRefresh) == AFSPRAAK_REFRESH) toRefresh = quickRefreshAfspraken(toRefresh);

			if ((CIJFER_REFRESH & toRefresh) == CIJFER_REFRESH) toRefresh = quickRefreshCijfers(toRefresh);

			if ((RECENT_CIJFER_REFRESH & toRefresh) == RECENT_CIJFER_REFRESH) toRefresh = quickRefreshRecentCijfers(toRefresh);

			if (toRefresh > 0) Log.wtf("Refresh", "mask = " + toRefresh);
		}

		private int quickRefreshAfspraken(int mask) throws IOException
		{
			DateTime van = Utils.now();
			DateTime tot = van.plusDays(app.getDaysInAdvance());

			result.setAfspraken(data.getAfsprakenFromCache(van, tot));

			return mask & ~AFSPRAAK_REFRESH;
		}

		private int quickRefreshCijfers(int mask) throws IOException
		{
			result.setCijfers(data.getCijfersFromCache());

			return mask & ~CIJFER_REFRESH;
		}

		private int quickRefreshRecentCijfers(int mask) throws IOException
		{
			result.setCijfers(data.getRecentCijfersFromCache());

            return mask & ~RECENT_CIJFER_REFRESH;
		}

		private void longRefresh(int toRefresh) throws IOException
		{
            if ((NEXT_DAY_REFRESH & toRefresh) == NEXT_DAY_REFRESH)
            {
                longRefreshAfspraken(toRefresh);

                result.setAfspraken(data.getNextDayFromCache());

                toRefresh = toRefresh & ~ NEXT_DAY_REFRESH;
            }

			if ((AFSPRAAK_REFRESH & toRefresh) == AFSPRAAK_REFRESH) toRefresh = longRefreshAfspraken(toRefresh);

			if ((CIJFER_REFRESH & toRefresh) == CIJFER_REFRESH) toRefresh = longRefreshCijfers(toRefresh);

            if ((RECENT_CIJFER_REFRESH & toRefresh) == RECENT_CIJFER_REFRESH) toRefresh = longRefreshRecentCijfers(toRefresh);

			if (toRefresh > 0) Log.wtf("Refresh", "mask = " + toRefresh);
		}

		private int longRefreshAfspraken(int mask) throws IOException
		{
			DateTime van = Utils.now();
			DateTime tot = van.plusDays(app.getDaysInAdvance());

			result.setAfspraken(data.getAfspraken(van, tot));

			return mask & ~AFSPRAAK_REFRESH;
		}

		private int longRefreshCijfers(int mask) throws IOException
		{
			result.setCijfers(data.getCijfers());

			return mask & ~CIJFER_REFRESH;
		}

        private int longRefreshRecentCijfers(int mask) throws IOException
        {
            result.setCijfers(data.getRecentCijfers());

            return mask & ~RECENT_CIJFER_REFRESH;
        }

		@Override
		protected void onProgressUpdate(DataFixer.ResultBundle... values) {

            if ((ongoing & currentFragment.mask) != currentFragment.mask) return;

			if (currentFragment.instance.isVisible() || ! isQuick)
			{
				((DataFixer.OnResultInterface) currentFragment.instance).onResult(result);
			}
		}

		/**
		 * Wordt geroepen als iemand probeert om het overbodige deel te refreshen.
		 */
		public void doContinue()
		{
			finished = false;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			if (! finished)
			{
				// Tussen de periode dat de "oorspronkelijke" refresh klaar was
				// refreshte iemand nog een keer, en werd er gewerkt aan het toen overbodige deel.
				mSwipeRefreshLayout.setRefreshing(false);

				finished = true;
			}
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
		else if (currentFragment != FragmentView.DASHBOARD) {
			navigationView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
			setFragment(FragmentView.DASHBOARD);
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
				setFragment(FragmentView.DASHBOARD);
				return true;

			case R.id.nav_rooster:
				setFragment(FragmentView.ROOSTER);
				return true;

			case R.id.nav_cijfers:
				setFragment(FragmentView.CIJFERS);
				return true;

			case R.id.nav_settings:
			case R.id.action_settings:
				startActivity(new Intent(this, Settings.class));
				return true;

			case R.id.action_logout:
				finish();
				getMagisterApplication().getApi().disconnect();
				getMagisterApplication().voidCredentails();
				startActivity(new Intent(this, this.getClass()));
				return true;

			default:
				return false;
		}
	}

	public void setFragment(FragmentView fragment)
	{
		setFragment(fragment, false);
	}

	public void setFragment(FragmentView fragment, boolean backstack)
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

		((DashboardFragment) FragmentView.DASHBOARD.instance).deleteView();
		((RoosterFragment) FragmentView.ROOSTER.instance).deleteView();
		((CijfersFragment) FragmentView.CIJFERS.instance).deleteView();

	}
}
