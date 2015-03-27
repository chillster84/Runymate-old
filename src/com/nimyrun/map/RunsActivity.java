package com.nimyrun.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class RunsActivity extends Activity {

	ListView list;
	RunsAdapter adapter;
	Route route;
	int routePosition;
	boolean validated = false;
	protected int nymiHandle;
	Activity mActivity = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_runs);

		Bundle b = getIntent().getExtras();
		route = (Route) b.getParcelable("route");
		routePosition = (int) b.getInt("routePosition");
		validated = b.getBoolean("validated");
		nymiHandle = b.getInt("nymiHandle");
		
		Button newRunButton = (Button)findViewById(R.id.newRun);
		//if(!validated) {
		//	newRunButton.setVisibility(View.GONE); //to set visible
		//}

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
			
			final CharSequence intervals[] = new CharSequence[] {"1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes"};
			
			 mActivity.runOnUiThread(new Runnable() {
		 		public void run() {
			
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setTitle("Performance Tracking Interval");
					builder.setItems(intervals, new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        // the user clicked on intervals[which]
			
							Intent intent = new Intent(getApplicationContext(),
									MainActivity.class);
							intent.putExtra("isNewRoute", false);
							intent.putExtra("routePosition", routePosition);
							intent.putExtra("interval", (which+1)*60);
					    	intent.putExtra("nymiHandle", nymiHandle);
							startActivity(intent);
					    }
					});
					builder.show();
		 		}
			 });
		}
	}
}
