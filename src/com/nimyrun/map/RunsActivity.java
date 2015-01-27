package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
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
