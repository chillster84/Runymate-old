package com.nimyrun.map;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// CAROUSEL
////////////////////////////////////////////////////
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.LinearLayout;
////////////////////////////////////////////////////

public class RoutesActivity extends Activity {
	
	// CAROUSEL
	/////////////////////////////////////////////	
	/**
     * Define the number of items visible when the carousel is first shown.
     */
    private static final float INITIAL_ITEMS_COUNT = 3.5F;

    /**
     * Carousel container layout
     */
    private LinearLayout mCarouselContainer;
	/////////////////////////////////////////////

	ListView list;
	RoutesImageAdapter adapter;
	List<Route> routes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);
		
		// CAROUSEL
		////////////////////////////////////////////////////
        // Get reference to carousel container
        mCarouselContainer = (LinearLayout) findViewById(R.id.carousel);
		////////////////////////////////////////////////////

		// This is the main location of routes
		// This should get loaded from device storage
		// Using StoreRoutes.java > RetrieveRoutes()
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		routes = retrieveRoutes(preferences);
         
		list = (ListView) findViewById(R.id.listView1);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				Intent intent = new Intent(getApplicationContext(),
						RunsActivity.class);
				intent.putExtra("route", routes.get(pos));
				intent.putExtra("routePosition", pos);
				startActivity(intent);
			}
		});
		adapter = new RoutesImageAdapter(this, routes);
        list.setAdapter(adapter);
    }
     
	// This methods should go into LocalStorageUtils.java
	public static List<Route> retrieveRoutes(SharedPreferences sharedPreferences) {
		String json = sharedPreferences.getString("routes", null);
		Type type = new TypeToken<List<Route>>() {
		}.getType();
		List<Route> routes = new Gson().fromJson(json, type);
		if (routes == null) {
			routes = new ArrayList<Route>();

			// add/store fake routes if no routes present
			routes.addAll(getFakeRoutes());
			storeRoutes(sharedPreferences, routes);
		}
		return routes;
	}

	public static void storeRoutes(SharedPreferences sharedPreferences,
			List<Route> routes) {
		Editor editor = sharedPreferences.edit();
		String json = new Gson().toJson(routes);
		editor.putString("routes", json);
		editor.commit();
	}

	// This methods should go into LocalStorageUtils.java

	private static List<Route> getFakeRoutes() {
		List<Route> routes = new ArrayList<Route>();
		// create routes
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

		// extra fake routes
		Route routeB = new Route("Route B");
		Route routeC = new Route("Route C");
		Route routeD = new Route("Route D");
		Route routeE = new Route("Route E");
		routeB.setPath(routeA.getPath());
		routeC.setPath(routeA.getPath());
		routeD.setPath(routeA.getPath());
		routeE.setPath(routeA.getPath());

		// add fake runs
		routeA.setRuns(getMockRuns(5));
		routeB.setRuns(getMockRuns(4));
		routeC.setRuns(getMockRuns(3));
		routeD.setRuns(getMockRuns(2));
		routeE.setRuns(getMockRuns(1));

		// put in list
		routes.add(routeA);
		routes.add(routeB);
		routes.add(routeC);
		routes.add(routeD);
		routes.add(routeE);
		return routes;
	}

	private static List<Run> getMockRuns(int n) {
		List<Run> runs = new ArrayList<Run>();

		// add n runs
		while (n != 0) {
			runs.add(mockRun());
			n--;
		}
		return runs;
	}

	private static Run mockRun() {
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
    public void onDestroy()
    {
        list.setAdapter(null);
        super.onDestroy();
    }
	
	//CAROUSEL
	//////////////////////////////////////////
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Compute the width of a carousel item based on the screen width and number of initial items.
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);

        // Get the array of puppy resources
        final TypedArray puppyResourcesTypedArray = getResources().obtainTypedArray(R.array.puppies_array);

        // Populate the carousel with items
        ImageView imageItem;
        for (int i = 0; i < puppyResourcesTypedArray.length(); ++i) {
            // Create new ImageView
            imageItem = new ImageView(this);
            
            imageItem.setId(i);

            // Set the shadow background
            //imageItem.setBackgroundResource(R.drawable.shadow);

            // Set the image view resource
            imageItem.setImageResource(puppyResourcesTypedArray.getResourceId(i, -1));

            // Set the size of the image view to the previously computed value
            imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));

            /// Add image view to the carousel container
            mCarouselContainer.addView(imageItem);
            
            mCarouselContainer.getChildAt(i).setOnClickListener(btn_click);
        }
    }
	/////////////////////////////////
	
	private ImageView routeImageZoom;

	OnClickListener btn_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//final DisplayMetrics displayMetrics = new DisplayMetrics();
	        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	        //final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);
	        
			//int[] loc = {0, 0};
			
			routeImageZoom = (ImageView) findViewById(R.id.routeImageZoom);
			TypedArray puppyResourcesTypedArray = getResources().obtainTypedArray(R.array.puppies_array);
			routeImageZoom.setImageResource(puppyResourcesTypedArray.getResourceId(v.getId(), -1));
			for (int i = 0; i < puppyResourcesTypedArray.length(); ++i) {
				mCarouselContainer.getChildAt(i).setPadding(0, 0, 0, 0);
			}
			v.setPadding(15, 15, 15, 15);
			v.setBackgroundColor(getResources().getColor(R.color.teal));
			//v.getLocationOnScreen(loc);
			//mCarouselContainer.setScrollX(halfScreenWidth - loc[0]);
			//mCarouselContainer.offsetLeftAndRight(imageWidth * v.getId());
			//mCarouselContainer.setScrollX(imageWidth * v.getId());
			Toast.makeText(getApplicationContext(),
					"ID: " + v.getId(), Toast.LENGTH_SHORT).show();
		}
	};
}
