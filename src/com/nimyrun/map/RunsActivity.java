package com.nimyrun.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class RunsActivity extends Activity {

	ListView list;
	RunsAdapter adapter;
	Route route;
	int routePosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		route = (Route) b.getParcelable("route");
		routePosition = (int) b.getInt("routePosition");

		setContentView(R.layout.activity_runs);

		list = (ListView) findViewById(R.id.listView2);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				Intent intent = new Intent(getApplicationContext(),
						RunMetricsActivity.class);
				intent.putExtra("run", route.getRuns().get(pos));
				startActivity(intent);
			}
		});
		adapter = new RunsAdapter(this, route.getRuns());
		list.setAdapter(adapter);
	}

	public void onButtonClick(View v) {
		if (v.getId() == R.id.newRun) {
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.putExtra("isNewRoute", false);
			intent.putExtra("routePosition", routePosition);
			startActivity(intent);
		}

	}
}
