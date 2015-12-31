package eu.magisterapp.magister;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;


public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	DrawerLayout mDrawerLayout;
	NavigationView navigationView;
	Toolbar toolbar;
	int fragmentPosition = 0;

	Fragment currentFragment;

	// Al onze fragments
	DashboardFragment dashboardFragment;
	CijfersFragment cijfersFragment;
	RoosterFragment roosterFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
		{
			fragmentPosition = savedInstanceState.getInt("current_fragment", 0);
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
		Log.i("position", String.valueOf(item.getOrder()));

		setFragment(getFragmentPosition(item.getItemId()));

		mDrawerLayout.closeDrawers();

		return true;
	}

	public void postLogin()
	{
		setFragment(fragmentPosition, false);
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
		if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
			mDrawerLayout.closeDrawers();
		else if (fragmentPosition != 0)
			setFragment(0);
		else
			super.onBackPressed();
	}

	private int getFragmentPosition(int res)
	{
		switch (res)
		{
			case R.id.nav_dashboard:
				return 0;
			case R.id.nav_rooster:
				return 1;
			case R.id.nav_cijfers:
				return 2;
		}

		return 0;
	}

	private void setFragment(int position)
	{
		setFragment(position, false);
	}

	private void setFragment(int position, boolean backstack)
	{
		fragmentPosition = position;

		MenuItem item = navigationView.getMenu().getItem(position);
		if (item != null) item.setChecked(true);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		int container = R.id.fragment_container;

		if (currentFragment != null)
			transaction.remove(currentFragment);

		switch (position)
		{
			case 1:
				transaction.replace(container, currentFragment = getRoosterFragment());
				break;

			case 2:
				transaction.replace(container, currentFragment = getCijfersFragment());
				break;
			case 0:
			default:
				transaction.replace(container, currentFragment = getDashboardFragment());
				break;
		}

		if (backstack)
		{
			transaction.addToBackStack(null);
		}

		transaction.commit();

		mDrawerLayout.closeDrawers();
	}

	private RoosterFragment getRoosterFragment()
	{
		if (roosterFragment == null)
		{
			roosterFragment = new RoosterFragment();
		}

		return roosterFragment;
	}

	private CijfersFragment getCijfersFragment()
	{
		if (cijfersFragment == null)
		{
			cijfersFragment = new CijfersFragment();
		}

		return cijfersFragment;
	}

	private DashboardFragment getDashboardFragment()
	{
		if (dashboardFragment == null)
		{
			dashboardFragment = new DashboardFragment();
		}

		return dashboardFragment;
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
