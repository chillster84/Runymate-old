package com.nimyrun.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RunMetricsActivity extends Activity {
	private GoogleMap gMap;
	Run run;
	LatLngBounds bounds;
	TextView speed;
	TextView time;
	TextView heartRate;
	Marker prevMarker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		run = (Run) b.getParcelable("run");

		setContentView(R.layout.activity_run_metrics);
		// Initialize map
		try {
			initilizeMap();
		} catch (Exception e) {
			e.printStackTrace();
		}

		PolylineOptions polylineOptions = new PolylineOptions();
		Builder builder = LatLngBounds.builder();
		for (RunMetric runMetric : run.getRunMetrics()) {
			LatLng point = runMetric.getLatlng();
			gMap.addMarker(new MarkerOptions().position(point));
			polylineOptions.add(point);
			builder.include(point);
		}
		gMap.addPolyline(polylineOptions);
		speed = (TextView) findViewById(R.id.textView1);
		time = (TextView) findViewById(R.id.textView2);
		heartRate = (TextView) findViewById(R.id.textView3);
		bounds = builder.build();
		gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				int padding = 50; // offset from edges of the map in pixels
				CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
						padding);
				gMap.moveCamera(cu);
				gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
					
					@Override
					public boolean onMarkerClick(Marker marker) {
						RunMetric runMetric = getRunMetric(marker);
						speed.setText("Speed: " + runMetric.getSpeed() + " m/s");
						time.setText("Time: " + runMetric.getTimestamp() + " min");
						heartRate.setText("Heart rate: "
								+ runMetric.getHeartRate() + " bpm");
						if (prevMarker != null) {
							prevMarker.setIcon(BitmapDescriptorFactory
									.defaultMarker());
						}
						marker.setIcon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
						prevMarker = marker;
				        return true;
				    }
				});
			}
		});

	}

	private RunMetric getRunMetric(Marker marker) {
		RunMetric found = null;
		for (RunMetric runMetric : run.getRunMetrics()) {
			if (runMetric.getLatlng().equals(marker.getPosition())) {
				found = runMetric;
				break;
			}
		}
		return found;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.run_metrics, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Initialize Google map.
	 */
	private void initilizeMap() {
		if (gMap == null) {
			gMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map2)).getMap();

			// Check if map is created successfully
			if (gMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create map.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
