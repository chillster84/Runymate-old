package com.nimyrun.map;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import android.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;

public class MainActivity extends Activity {

	private GoogleMap mMap;
	private MapFragment mMapFragment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded(); // set some options
        
    }
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
        	mMapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
            mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
            	
            	mMapFragment = MapFragment.newInstance(setUpOptions());
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.map, mMapFragment);
                fragmentTransaction.commit();
            	
            	enableLocation();
            }
        }
    }

    private void enableLocation() {
    	mMap.setMyLocationEnabled(true);
    }
    
	private GoogleMapOptions setUpOptions() {
		GoogleMapOptions options = new GoogleMapOptions();
		
		options.mapType(GoogleMap.MAP_TYPE_NORMAL)
	    .compassEnabled(true)
	    .rotateGesturesEnabled(false)
	    .tiltGesturesEnabled(true);
		
		return options;
	}

    
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
    */
}
