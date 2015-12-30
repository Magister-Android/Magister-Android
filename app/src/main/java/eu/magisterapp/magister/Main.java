package eu.magisterapp.magister;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import eu.magisterapp.magisterapi.Account;


public class Main extends AppCompatActivity implements DashboardFragment.DrawerUpdater
{
	Drawer drawer;
	AccountHeader drawerHeader;
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

		toolbar = makeToolbar();
		drawer = makeDrawer();

		new LoginFixer(this).startLoginSequence();
	}

	private Toolbar makeToolbar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setTitle(R.string.app_name);
		}

		return toolbar;
	}

	private Drawer makeDrawer()
	{
		drawerHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.header)
				.build();

		return new DrawerBuilder()
				.withActivity(this)
				.withAccountHeader(drawerHeader)
				.withToolbar(toolbar)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.drawer_dashboard).withIcon(GoogleMaterial.Icon.gmd_balance),
						new PrimaryDrawerItem().withName(R.string.drawer_rooster).withIcon(GoogleMaterial.Icon.gmd_calendar),
						new PrimaryDrawerItem().withName(R.string.drawer_cijfers).withIcon(GoogleMaterial.Icon.gmd_trending_up),
						new DividerDrawerItem(),
						new SecondaryDrawerItem().withName(R.string.drawer_instellingen).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_settings)
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

						selectItem(position);

						return true;
					}
				})
				.build();
	}

	@Override
	public void setAccount(Account account) {
		if (drawerHeader == null) return;

		drawerHeader.setActiveProfile(new ProfileDrawerItem().withName(account.getNaam()));
	}

	public void postLogin()
	{
		selectItem(fragmentPosition, false);
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
		if(drawer.isDrawerOpen())
			drawer.closeDrawer();
		else if (fragmentPosition != 1)
			drawer.setSelection(1, true);
		else
			super.onBackPressed();
	}

	private void selectItem(int position)
	{
		selectItem(position, false);
	}

	private void selectItem(int position, boolean backstack)
	{
		fragmentPosition = position;

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		int container = R.id.fragment_container;

		if (currentFragment != null)
			transaction.remove(currentFragment);

		switch (position)
		{
			case 2:
				transaction.replace(container, currentFragment = getRoosterFragment());
				break;

			case 3:
				transaction.replace(container, currentFragment = getCijfersFragment());
				break;

			case 4:
				startActivity(new Intent(this, Settings.class));
				break;
			case 1:
			default:
				transaction.replace(container, currentFragment = getDashboardFragment());
				break;
		}

		if (backstack)
		{
			transaction.addToBackStack(null);
		}

		transaction.commit();

		drawer.closeDrawer();
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
		if (toolbar == null)
		{
			return;
		}

		toolbar.setTitle(title);
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
