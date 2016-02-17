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
		TextView vak = (TextView) findViewById(R.id.vak);
		TextView docent = (TextView) findViewById(R.id.docent);
		TextView omschrijving = (TextView) findViewById(R.id.omschrijving);
		TextView weging = (TextView) findViewById(R.id.weging);

		cijfer_big.setText(cijfer.CijferStr);
		vak.setText(cijfer.Vak.Omschrijving);
		docent.setText(cijfer.Docent);
		omschrijving.setText(info.KolomOmschrijving);
		weging.setText(String.format("%dx", info.KolomSoortKolom));

		toolbar.setTitle(cijfer.Vak.Omschrijving);

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
