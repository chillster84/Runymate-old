package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class RunsActivity extends Activity {

	ListView list;
	RunsAdapter adapter;
	List<Run> runs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);

		list = (ListView) findViewById(R.id.listView1);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				Intent intent = new Intent(getApplicationContext(),
						RunMetricsActivity.class);
				startActivity(intent);
			}
		});
		runs = getMockRuns();
		adapter = new RunsAdapter(this, runs);
		list.setAdapter(adapter);
	}

	private List<Run> getMockRuns() {
		List<Run> runs = new ArrayList<Run>();
		runs.add(new Run(2000, 2000));
		runs.add(new Run(2000, 2000));
		runs.add(new Run(2000, 2000));
		runs.add(new Run(2000, 2000));
		return runs;
	}
}
