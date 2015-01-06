package com.nimyrun.map;

import java.util.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
//import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
//import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.MapFragment;

public class MainActivity extends Activity implements LocationListener {
	private final static double EARTH_RADIUS = 6371000; // in metres
	private final static int INTERVAL = 1; // in seconds
	
	private TextView latitudeField;
	private TextView longitudeField;
	private TextView prevLatitudeField;
	private TextView prevLongitudeField;
	private TextView speedField;
	private ImageView speedImage;
	private ImageView speedBlockerImage;
	private TextView heartField;
	private ImageView heartPeakImage;
	
	private int count = 0;
	private HashMap<String, Double> speedMap = new HashMap<String, Double>();
	
	private LocationManager locationManager;
	private String provider;
	
	private int prevX = 0;
	
	private GoogleMap gMap;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		latitudeField = (TextView) findViewById(R.id.TextView02);
		longitudeField = (TextView) findViewById(R.id.TextView04);
		prevLatitudeField = (TextView) findViewById(R.id.TextView06);
		prevLongitudeField = (TextView) findViewById(R.id.TextView08);
		speedField = (TextView) findViewById(R.id.speedValue);
		speedImage = (ImageView) findViewById(R.id.speedImage);
		speedBlockerImage = (ImageView) findViewById(R.id.speedBlockerImage);
		heartField = (TextView) findViewById(R.id.heartValue);
		heartPeakImage = (ImageView) findViewById(R.id.heartPeakImage);
		
		Button button = (Button) findViewById(R.id.button01);
		
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// Define the criteria how to select the location provider -> use default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
	    // Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
	    }
		else {
	    	latitudeField.setText("Location not available");
	    	longitudeField.setText("Location not available");
	    }
		
		// Initialize map
		try {
            initilizeMap();
        }
		catch (Exception e) {
            e.printStackTrace();
        }
		
		gMap.setMyLocationEnabled(true);
		
		// DRAW TEST ROUTE
		
		// Instantiates a new Polyline object and adds points to define a rectangle
		PolylineOptions rectOptions = new PolylineOptions()
		        .add(new LatLng(37.35, -122.0))
		        .add(new LatLng(37.45, -122.0))  // North of the previous point, but at the same longitude
		        .add(new LatLng(37.45, -122.2))  // Same latitude, and 30km to the west
		        .add(new LatLng(37.35, -122.2))  // Same longitude, and 16km to the south
		        .add(new LatLng(37.35, -122.0)); // Closes the polyline.

		// Get back the mutable Polyline
		Polyline polyline = gMap.addPolyline(rectOptions);
		
	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, INTERVAL * 1000, 0, this);
		initilizeMap();
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		count++;
		int heartbeat;
		double speed;
		//double previousSpeed;
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		
		double lat_1, long_1;
		
		if (latitudeField.getText().equals("unknown")) {
			lat_1 = latitude;
		}
		else {
			lat_1 = Double.parseDouble(latitudeField.getText().toString());
		}
	    
	    if (longitudeField.getText().equals("unknown")) {
	    	long_1 = longitude;
	    }
	    else {
	    	long_1 = Double.parseDouble(longitudeField.getText().toString());
	    }
	    
	    prevLatitudeField.setText(String.valueOf(lat_1));
	    prevLongitudeField.setText(String.valueOf(long_1));
	    
	    latitudeField.setText(String.valueOf(latitude));
	    longitudeField.setText(String.valueOf(longitude));
	    
	    //previousSpeed = speed;
	    
	    speed = getDistance(latitude, longitude, lat_1, long_1)/INTERVAL;
	    speedMap.put(count + "", speed);
	    speedField.setText((double)Math.round(speed * 100) / 100 + "");
	    
	    heartbeat = 50 + (count/5 * 10);
	    heartField.setText(heartbeat + "");
	    
	    setAnimation(speedBlockerImage, speed);
	    setAnimation(heartPeakImage, heartbeat);
	    
	    //getCameraToCurrentLocation(lat_1, long_1);
	    LatLng latLng = new LatLng(0, 0);
	    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
	    //gMap.animateCamera(cameraUpdate);
	    //locationManager.removeUpdates(this);
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}
	
	public void onButtonClick(View v){
	    if (v.getId() == R.id.button01) {
	    	Intent intent = new Intent(getApplicationContext(), ActivityResults.class);
	    	
	    	intent.putExtra("speedPoints", speedMap);
	    	//intent.putExtra("speedPoints", speedMap.values().toArray());
	    	startActivity(intent);
	    	//speedField.setText("HELLO!!!");
	    }
	}
	
	void setAnimation(ImageView image, double value) {
		//image.setLayoutParams(new FrameLayout.LayoutParams((int)speed/5*576, 152));
		/*
		int previousX;
		
		if (previousValue == 0) {
			previousX = 0;
		}
		else {
			previousX = (-1)*(int)(576 - previousValue/5*576);
		}
		*/
		
		if (image.equals(speedBlockerImage)) {
			TranslateAnimation animation = new TranslateAnimation(prevX, (int)(value/0.2*576), 0, 0);
		    animation.setDuration(INTERVAL * 500);
		    animation.setFillAfter(true);
		    image.startAnimation(animation);
		    prevX = (int)(value/0.2*576);
		}
		else if (image.equals(heartPeakImage)) {
			TranslateAnimation animation = new TranslateAnimation(0, -576 * 2, 0, 0);
		    animation.setDuration((int) (60000 / value));
		    //animation.setFillAfter(true);
		    image.startAnimation(animation);
		}
	}
	
	double getDistance(double lat_1, double long_1, double lat_2, double long_2) {
	    // Convert coordinates to radians
	    lat_1 = lat_1 * Math.PI / 180.0;
	    long_1 = long_1 * Math.PI / 180.0;
	    lat_2 = lat_2 * Math.PI / 180.0;
	    long_2 = long_2 * Math.PI / 180.0;
	    
	    // Point P
	    double rho1 = EARTH_RADIUS * Math.cos(lat_1);
	    double z1 = EARTH_RADIUS * Math.sin(lat_1);
	    double x1 = rho1 * Math.cos(long_1);
	    double y1 = rho1 * Math.sin(long_1);
	    
	    // Point Q
	    double rho2 = EARTH_RADIUS * Math.cos(lat_2);
	    double z2 = EARTH_RADIUS * Math.sin(lat_2);
	    double x2 = rho2 * Math.cos(long_2);
	    double y2 = rho2 * Math.sin(long_2);
	 
	    // Dot product
	    double dot = (x1 * x2 + y1 * y2 + z1 * z2);
	    double cos_theta = dot / (EARTH_RADIUS * EARTH_RADIUS);
	 
	    double theta = Math.acos(cos_theta);
	 
	    // Distance in metres
	    return EARTH_RADIUS * theta;
	}
	
	/**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (gMap == null) {
        	gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        	
        	// check if map is created successfully or not
            if (gMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    
    private void getCameraToCurrentLocation(double lat, double lng) {
    	CameraPosition cameraPosition = new CameraPosition.Builder().target(
    	
                new LatLng(-42, 73)).zoom(12).build();
    	
    	gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}