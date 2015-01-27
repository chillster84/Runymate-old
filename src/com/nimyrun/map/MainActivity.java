package com.nimyrun.map;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
//import android.widget.FrameLayout;
//import java.util.ArrayList;
//import com.google.android.gms.maps.CameraUpdate;

public class MainActivity extends Activity implements LocationListener {
	private final static double EARTH_RADIUS = 6371000; // in metres
	private final static int LOCATION_MIN_TIME = 1; // in seconds
	private final static int SAMPLING_INTERVAL = 5;
	private final static int ZOOM_LEVEL = 18; // on a scale of 1 - 22

	private TextView latitudeField;
	private TextView longitudeField;
	private TextView prevLatitudeField;
	private TextView prevLongitudeField;
	private TextView speedField;
	private ImageView speedImage;
	private ImageView speedBlockerImage;
	private TextView heartField;
	private ImageView heartPeakImage;
	private Button button;

	private LocationManager locationManager;
	private Location location;
	private String provider;
	private GoogleMap gMap;
	private Polyline polyline;

	private double latitude = 0;
	private double longitude = 0;
	private double newLatitude = 0;
	private double newLongitude = 0;
	//private double lastValidLatitude = 0;
	//private double lastValidLongitude = 0;
	private double speed = 0;
	private double heartbeat = 0;
	private double distance = 0;

	private int count = 0;
	private int errorFactor = 1;
	private HashMap<String, Double> speedMap = new HashMap<String, Double>();
	private HashMap<String, Double> heartMap = new HashMap<String, Double>();

	// Horizontal offset of speed animation graphic from previous speed
	// measurement.
	private int previousSpeedAnimationXOffset = 0;

	// Instantiate a new Polyline object for drawing the current route on the
	// map.
	private PolylineOptions rectOptions = new PolylineOptions();

	/*
	 * Set actions for initial creation of activity.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Assign views to variables
		latitudeField = (TextView) findViewById(R.id.TextView02);
		longitudeField = (TextView) findViewById(R.id.TextView04);
		prevLatitudeField = (TextView) findViewById(R.id.TextView06);
		prevLongitudeField = (TextView) findViewById(R.id.TextView08);
		speedField = (TextView) findViewById(R.id.speedValue);
		speedImage = (ImageView) findViewById(R.id.speedImage);
		speedBlockerImage = (ImageView) findViewById(R.id.speedBlockerImage);
		heartField = (TextView) findViewById(R.id.heartValue);
		heartPeakImage = (ImageView) findViewById(R.id.heartPeakImage);
		button = (Button) findViewById(R.id.button01);

		// Initialize map
		try {
			initilizeMap();
			gMap.setMyLocationEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Define the criteria for selecting the location provider (default
		// used)
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);

		// Initialize location fields
		if (location != null) {
			/*
			 * new CountDownTimer(10000, 1000) {
			 * 
			 * public void onTick(long millisUntilFinished) {
			 * latitudeField.setText("seconds remaining: " + millisUntilFinished
			 * / 1000); }
			 * 
			 * public void onFinish() { latitudeField.setText("done!"); }
			 * }.start();
			 */
			System.out.println("Provider " + provider + " has been selected.");

			onLocationChanged(location);
			
			setTimer(SAMPLING_INTERVAL, 5);
		} else {
			latitudeField.setText("Location not available.");
			longitudeField.setText("Location not available.");
		}
	}

	/*
	 * Request updates at startup.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
		locationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME * 1000, 0,
				this);
		updateLocation();
	}

	/*
	 * Remove locationlistener updates when the activity is paused.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	/*
	 * Set actions on location change. (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onLocationChanged(android.location.
	 * Location)
	 */
	@Override
	public void onLocationChanged(Location newLocation) {
		location = newLocation;
		updateLocation();
	}

	/*
	 * Set actions on status change. (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String,
	 * int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			Toast.makeText(this, "GPS Provider out of service",
					Toast.LENGTH_SHORT).show();
		} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			Toast.makeText(this, "GPS Provider temporarily unavailable",
					Toast.LENGTH_SHORT).show();
		} else if (status == LocationProvider.AVAILABLE) {
			Toast.makeText(this, "GPS Provider available", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/*
	 * Set actions on provider enabled. (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * Set actions on provider disabled. (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * Set actions for button clicks.
	 */
	public void onButtonClick(View v) {
		if (v.getId() == R.id.button01) {
			Intent intent = new Intent(getApplicationContext(),
					ActivityResults.class);
			intent.putExtra("speedPoints", speedMap);
			intent.putExtra("distance", distance);
			intent.putExtra("time", (double) (count * LOCATION_MIN_TIME));
			intent.putExtra("heartPoints", heartMap);
			startActivity(intent);
		}
		if (v.getId() == R.id.button02) {
			Intent intent = new Intent(getApplicationContext(),
					RoutesActivity.class);
			startActivity(intent);
		}

	}

	/*
	 * Set animation for heart beat and speed measurements.
	 */
	private void setAnimation(ImageView image, double value) {
		if (image.equals(speedBlockerImage)) {
			TranslateAnimation animation = new TranslateAnimation(
					previousSpeedAnimationXOffset, (int) (value / 0.2 * 576),
					0, 0);
			animation.setDuration(LOCATION_MIN_TIME * 500);
			animation.setFillAfter(true);
			image.startAnimation(animation);
			previousSpeedAnimationXOffset = (int) (value / 0.2 * 576);
		} else if (image.equals(heartPeakImage)) {
			TranslateAnimation animation = new TranslateAnimation(0, -576 * 2,
					0, 0);
			animation.setDuration((int) (60000 / value));
			// animation.setFillAfter(true);
			image.startAnimation(animation);
		}
	}

	/*
	 * Initialize Google map.
	 */
	private void initilizeMap() {
		if (gMap == null) {
			gMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// Check if map is created successfully
			if (gMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create map.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/*
	 * Calculate distance using two latitude and longitude values. Spherical law
	 * of cosines formula and example Excel script referenced from
	 * http://www.movable-type.co.uk/scripts/latlong.html.
	 */
	private double getDistance(double lat_1, double long_1, double lat_2, double long_2) {
		// Convert coordinates to radians
		lat_1 = lat_1 * Math.PI / 180.0;
		long_1 = long_1 * Math.PI / 180.0;
		lat_2 = lat_2 * Math.PI / 180.0;
		long_2 = long_2 * Math.PI / 180.0;

		double distance = Math
				.acos(Math.sin(lat_1) * Math.sin(lat_2) + Math.cos(lat_1)
						* Math.cos(lat_2) * Math.cos(long_2 - long_1))
				* EARTH_RADIUS;
		return distance;
	}

	/*
	 * Focus Google map camera to current location.
	 */
	private void getCameraToCurrentLocation(double latitude, double longitude) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude)).zoom(ZOOM_LEVEL)
				.build();
		gMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	/*
	 * Update polyline object with a new latitude and longitude.
	 */
	private void updatePolyline(double latitude, double longitude) {
		rectOptions.add(new LatLng(latitude, longitude));
		polyline = gMap.addPolyline(rectOptions);
	}
	
	private void setTimer(int frequency, int delay) {
		final Handler handler = new Handler();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						try {
							sample();
						} catch (Exception e) {
						}
					}
				});
			}
		}, frequency * 1000, delay * 1000);
	}
	
	private void sample() {
		//count = count + SAMPLING_INTERVAL;
		count++;

		if (speed < 15) { // Fastest recorded running speed is about 12 m/s

			speedMap.put(count + "", speed);
			heartMap.put(count + "", heartbeat);
		}
		
		Toast.makeText(this, "SAMPLED! Speed = " + speed,
				Toast.LENGTH_SHORT).show();
	}
	
	private void updateLocation() {		
		double newDistance = 0;
		
		newLatitude = location.getLatitude();
		newLongitude = location.getLongitude();

		prevLatitudeField.setText(String.valueOf(latitude));
		prevLongitudeField.setText(String.valueOf(longitude));
		latitudeField.setText(String.valueOf(newLatitude));
		longitudeField.setText(String.valueOf(newLongitude));
		
		newDistance = getDistance(latitude, longitude, newLatitude,
				newLongitude);
		
		speed = newDistance / (LOCATION_MIN_TIME * errorFactor);
		
		speedField.setText((double) Math.round(speed * 100) / 100 + "");
		heartbeat = 50 + (count / 5 * 10);
		heartField.setText(heartbeat + "");
		
		setAnimation(speedBlockerImage, speed);
		setAnimation(heartPeakImage, heartbeat);
		
		if (speed < 15) { // If latest detected location makes sense
			distance = distance + newDistance;
			
			try {
				updatePolyline(newLatitude, newLongitude);
				getCameraToCurrentLocation(newLatitude, newLongitude);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			latitude = newLatitude;
			longitude = newLongitude;
			
			errorFactor = 1;
		}
		else { // If latest detected location is off
			errorFactor++;
		}
		
		// The first time location is detected - TO IMPROVE LATER
		if (latitude == 0 && longitude == 0) {
			latitude = newLatitude;
			longitude = newLongitude;
		}
	}
}