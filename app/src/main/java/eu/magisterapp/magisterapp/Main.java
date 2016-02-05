package eu.magisterapp.magisterapp;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.magisterapp.magisterapi.BadResponseException;
import eu.magisterapp.magisterapp.sync.Refresh;
import eu.magisterapp.magisterapp.sync.RefreshManager;


public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RefreshHandlerInterface, ErrorHandlerInterface
{
	DrawerLayout mDrawerLayout;
	NavigationView navigationView;
	Toolbar toolbar;

	ProgressDialog progress;

	SwipeRefreshLayout mSwipeRefreshLayout;

	FragmentView currentFragment = FragmentView.DASHBOARD;

	static int fragCounter = 0;

	private enum FragmentView
	{
		DASHBOARD(new DashboardFragment(), R.string.app_name, R.id.nav_dashboard), // 0
		ROOSTER(new RoosterFragment(), R.string.nav_rooster, R.id.nav_rooster), // 1
		CIJFERS(new CijfersFragment(), R.string.nav_cijfers, R.id.nav_cijfers); // 2

		public final Fragment instance;
		public final int title;
		public final int navId;
		public final int id;

        public final Refresh[] refreshers;

		FragmentView(Refreshable fragment, int title, int navId)
		{
			instance = (Fragment) fragment;
			this.title = title;
			this.navId = navId;
			id = fragCounter++;

            refreshers = fragment.getRefreshers();
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

        RefreshManager rm = getMagisterApplication().getRefreshManager();

        rm.setErrorHandler(this);

        List<Refresh> refreshers = new ArrayList<>();

        for (FragmentView fragment : FragmentView.values())
        {
            if (fragment == currentFragment) continue;

            refreshers.addAll(new ArrayList<>(Arrays.asList(fragment.refreshers)));
        }

        rm.first(currentFragment.refreshers) // Elk fragment heeft een of meer "Refresh" runnables
                .then(refreshers.toArray(new Refresh[refreshers.size()]))
                .done(this) // zet een callback onDoneRefreshing
                .run(); // voer refresh uit.
	}

    @Override
    public void onDoneRefreshing() {
        mSwipeRefreshLayout.setRefreshing(false);

        // TODO: update local data van fragments
        // TODO: call een onDone method op currentFragment.instance,
            // die fixt dat hij zn shit refresht. (kan misschien bij de data update)
    }

    @Override
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

			case R.id.action_debug_nukedb:
				// Deze optie is alleen zichtbaar in de "debug" variant.
				// Handig zodat je niet de heletijd data moet verwijderen & opnieuw
				// in te loggen als je iets wilt testen.
				// De app crasht als je dit gebruikt tijdens een refresh btw..
				getMagisterApplication().getDataStore().getDB().nuke();
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
