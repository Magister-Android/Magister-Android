package com.sibren.magister;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Main extends AppCompatActivity{
	String[] items;
	ArrayAdapter<String> adapter;

	ListView vlist;
	DrawerLayout dlayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		selectItem(0, false);
	}

	@Override
	public void onBackPressed(){
		if(dlayout.isDrawerOpen(GravityCompat.START))
			dlayout.closeDrawers();
		else
			super.onBackPressed();
	}

	private void selectItem(int position) {

		selectItem(position, true);
	}

	private void selectItem(int position, boolean backstack)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		int container = R.id.fragment_container;

		switch (position)
		{
			case 1:
				transaction.replace(container, new RoosterFragment());
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

	private void setupToolbar(){
		android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout dlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle abdtoggle = new ActionBarDrawerToggle(this, dlayout, toolbar, R.string.nav_open, R.string.nav_close);
		dlayout.setDrawerListener(abdtoggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		abdtoggle.syncState();
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
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
