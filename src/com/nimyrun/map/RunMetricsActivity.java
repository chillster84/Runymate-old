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
		setContentView(R.layout.activity_run_metrics);
		// Initialize map
		try {
			initilizeMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		run = mockRun();
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
						time.setText("Time: " + runMetric.getTimestamp());
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

	private Run mockRun() {
		Route routeA = new Route("Route A");

		// add points
		routeA.addPoint(43.66151856, -79.39409156);
		routeA.addPoint(43.66214725, -79.39433832);
		routeA.addPoint(43.66246547, -79.39487476);
		routeA.addPoint(43.66230248, -79.3957438);
		routeA.addPoint(43.66184455, -79.39619441);
		routeA.addPoint(43.66108391, -79.395894);
		routeA.addPoint(43.66099077, -79.3951859);
		routeA.addPoint(43.6612275, -79.39416666);
		routeA.addPoint(43.66151856, -79.39409156);

		Run run = new Run(222.22, 22.2);
		int i = 0;
		for (LatLng point : routeA.getPath()) {
			i++;
			run.addRunMetrics(new RunMetric(point, 22.2 + i, 22.2 + i,
					20150101 + i));
		}

		return run;
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
