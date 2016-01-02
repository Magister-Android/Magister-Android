package eu.magisterapp.magister;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;


public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	DrawerLayout mDrawerLayout;
	NavigationView navigationView;
	Toolbar toolbar;

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

		navigationView = (NavigationView) findViewById(R.id.nav_view);

		if (navigationView != null)
		{
			navigationView.setNavigationItemSelectedListener(this);
		}

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

	public void postLogin()
	{
		setFragment(currentFragment, false);
		navigationView.getMenu().findItem(currentFragment.navId).setChecked(true);
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
				startActivity(new Intent(this, this.getClass()));
				finish();
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
	}

}
