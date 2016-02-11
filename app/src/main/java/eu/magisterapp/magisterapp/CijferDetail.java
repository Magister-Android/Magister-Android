package eu.magisterapp.magisterapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import eu.magisterapp.magisterapi.Cijfer;

public class CijferDetail extends AppCompatActivity {

	protected Cijfer cijfer;
	protected Cijfer.CijferInfo info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cijfer_detail);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		cijfer = (Cijfer) getIntent().getExtras().get("cijfer");
		info = (Cijfer.CijferInfo) getIntent().getExtras().get("info");


		TextView cijfer_big = (TextView) findViewById(R.id.cijfer_big);
		TextView vak = (TextView) findViewById(R.id.cijfer_vak);

		cijfer_big.setText(cijfer.CijferStr);
		vak.setText(cijfer.Vak.Omschrijving);

		toolbar.setTitle(info.KolomOmschrijving);

		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}
}
