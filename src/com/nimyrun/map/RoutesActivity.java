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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
// CAROUSEL
////////////////////////////////////////////////////
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

	RoutesImageAdapter adapter;
	List<Route> routes;
	public ImageLoader imageLoader;

	protected int nymiHandle;
	boolean validated = false;
	
	TextView routeNameLabel;
	TextView routeDistanceLabel;
	TextView routeTimeLabel;
	TextView routeHeartLabel;
	TextView metres;
	TextView minutes;
	TextView bpm;	

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);
		
		Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            nymiHandle = b.getInt("nymiHandle");
            validated = b.getBoolean("validated");
            LoginScreen.appendLog("onCreate", "nymiHandle passed over to routesactivity is " + nymiHandle);
        }
		
		// CAROUSEL
		////////////////////////////////////////////////////
        // Get reference to carousel container
        mCarouselContainer = (LinearLayout) findViewById(R.id.carousel);
		////////////////////////////////////////////////////
        
        routeImageZoom = (ImageView) findViewById(R.id.routeImageZoom);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        routeImageZoom.getLayoutParams().height = displayMetrics.widthPixels;
        routeImageZoom.setBackgroundResource(R.drawable.bg_sel_route);
        
        routeNameLabel = (TextView) findViewById(R.id.routeNameLabel);
		routeDistanceLabel = (TextView) findViewById(R.id.routeDistanceLabel);
		routeTimeLabel = (TextView) findViewById(R.id.routeTimeLabel);
		routeHeartLabel = (TextView) findViewById(R.id.routeHeartLabel);
		metres = (TextView) findViewById(R.id.metres);
		minutes = (TextView) findViewById(R.id.minutes);
		bpm = (TextView) findViewById(R.id.bpm);
		
		routeNameLabel.setVisibility(View.INVISIBLE);
		routeDistanceLabel.setVisibility(View.INVISIBLE);
		routeTimeLabel.setVisibility(View.INVISIBLE);
		routeHeartLabel.setVisibility(View.INVISIBLE);
		metres.setVisibility(View.INVISIBLE);
		minutes.setVisibility(View.INVISIBLE);
		bpm.setVisibility(View.INVISIBLE);
		
		// This is the main location of routes
		// This should get loaded from device storage
		// Using StoreRoutes.java > RetrieveRoutes()
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		routes = retrieveRoutes(preferences);

         
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
		routeB.setPath(routeA.getPath());
		routeC.setPath(routeA.getPath());

		// add fake runs
		routeA.setRuns(getMockRuns(3));
		routeB.setRuns(getMockRuns(4));
		routeC.setRuns(getMockRuns(5));

		// put in list
		routes.add(routeA);
		routes.add(routeB);
		routes.add(routeC);
		return routes;
	}

	private static List<Run> getMockRuns(int n) {
		List<Run> runs = new ArrayList<Run>();

		if(n==5) {
			// add n runs
			runs.add(mockRun());
			runs.add(mockRun2());
			runs.add(mockRun3());
			runs.add(mockRun2());
			runs.add(mockRun3());
			return runs;
		}
		else if (n==4) {
			// add n runs
			while (n != 0) {
				runs.add(mockRun2());
				runs.add(mockRun3());
				n=n-2;
			}
			return runs;
		}
		else {
			runs.add(mockRun());
			runs.add(mockRun2());
			runs.add(mockRun3());
			return runs;
		}
		
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

		Run run = new Run(625, 8);
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(0), 2.4, 171,
				1427,8));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(1), 2.5, 0,
				379, 2));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(2), 2.5, 126,
						379, 2));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(3), 2.7, 0,
				824, 4));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(4), 2.7, 147,
				824, 4));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(5), 2.8, 0,
				1125, 6));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(6), 2.8, 173,
				1125, 6));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(7), 2.8, 0,
				1125, 6));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(8), 2.4, 171,
				1427,8));
		
		return run;
	}
	
	private static Run mockRun2() {
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

		Run run = new Run(625, 7.5);
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(0), 2.9, 177,
				1995,7.5));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(1), 2.7, 0,
				412, 1.75));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(2), 2.7, 139,
						412, 1.75));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(3), 2.7, 0,
				824, 4));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(4), 3.3, 165,
				940, 3.5));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(5), 2.8, 0,
				1125, 6));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(6), 3.7, 182,
				1525, 6));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(7), 2.8, 0,
				1125, 6));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(8), 2.9, 177,
				1995,7.5));
		
		return run;
	}
	
	private static Run mockRun3() {
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

		Run run = new Run(625, 7);
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(0), 3.4, 181,
				2074,7));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(1), 2.7, 0,
				412, 1.75));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(2), 3.0, 144,
						427, 2));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(3), 2.7, 0,
				824, 3.5));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(4), 3.4, 160,
				1024, 3.5));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(5), 2.8, 0,
				1125, 4.75));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(6), 3.7, 179,
				1599, 4.75));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(7), 2.8, 0,
				1125, 4.75));
		
		run.addRunMetrics(new RunMetric(routeA.getPath().get(8), 3.4, 181,
				2074,7));
		
		return run;
	}
	
	//CAROUSEL
	//////////////////////////////////////////
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

		drawCarousel();
	}

	private void drawCarousel() {
		imageLoader = new ImageLoader(getApplicationContext());

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		routes = retrieveRoutes(preferences);

        // Compute the width of a carousel item based on the screen width and number of initial items.
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);

        // Populate the carousel with items
        ImageView imageItem;
		for (int i = 0; i < routes.size(); i++) {
            // Create new ImageView
            imageItem = new ImageView(this);
			String imageUrl = getRouteUrl(routes.get(i));
			imageLoader.DisplayImage(imageUrl, imageItem, i);
            mCarouselContainer.addView(imageItem);
            mCarouselContainer.getChildAt(i).setPadding(15, 15, 15, 15);
            mCarouselContainer.getChildAt(i).setOnClickListener(btn_click);
        }
	}
	/////////////////////////////////
	
	private ImageView routeImageZoom;
	int pos;

	OnClickListener btn_click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//final DisplayMetrics displayMetrics = new DisplayMetrics();
	        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
	        //final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);

			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			LoginScreen.appendLog("got shraed prefs", "");
			routes = retrieveRoutes(preferences);
			LoginScreen.appendLog("retrieved routes", "");
			pos = v.getId();

			Route route = routes.get(pos);
			//int[] loc = {0, 0};
			
			//show visible the labels
			routeNameLabel.setVisibility(View.VISIBLE);
			routeDistanceLabel.setVisibility(View.VISIBLE);
			routeTimeLabel.setVisibility(View.VISIBLE);
			routeHeartLabel.setVisibility(View.VISIBLE);
			metres.setVisibility(View.VISIBLE);
			minutes.setVisibility(View.VISIBLE);
			bpm.setVisibility(View.VISIBLE);
			
			TextView routeName = (TextView) findViewById(R.id.routeName);
			TextView routeDistance = (TextView) findViewById(R.id.routeDistance);
			TextView routeTime = (TextView) findViewById(R.id.routeTime);
			TextView routeHeart = (TextView) findViewById(R.id.routeHeart);

			routeName.setText(" " + route.getName());
			LoginScreen.appendLog("route name", " " + route.getName());
			// we can write a method that gets the average or the best out of
			// the runs
			//distance is the same for the same route
			routeDistance.setText(" " + route.getRuns().get(0).getDistance());
			
			//want to show best time, go through runs to find lowest time. get avg hr from best run
			double bestTime = route.getRuns().get(0).getTime();
			double bestHR = route.getRuns().get(0).getAverageHeartRate();
			for (Run run : route.getRuns()) {
				if (run.getTime() < bestTime) {
					bestTime = run.getTime();
					bestHR = run.getAverageHeartRate();
				}
			}
			routeTime.setText(" " + bestTime);
			routeHeart.setText(" " + bestHR);

			String url = getRouteUrl(route);
			imageLoader.DisplayImage(url, routeImageZoom);

			for (int i = 0; i < routes.size(); ++i) {
				mCarouselContainer.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			
			v.setBackgroundColor(getResources().getColor(R.color.teal));
			
			final DisplayMetrics displayMetrics = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			routeImageZoom.getLayoutParams().height = displayMetrics.widthPixels;

			routeImageZoom.setOnClickListener(new OnClickListener() {
				// Start new list activity
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(),
							RunsActivity.class);
					intent.putExtra("route", routes.get(pos));
					intent.putExtra("routePosition", pos);
					intent.putExtra("validated", validated);
					intent.putExtra("nymiHandle", nymiHandle);
					startActivity(intent);
				}
			});
		}
	};

	public void onDeleteRouteButtonClick(View v) {
		//Toast.makeText(getApplicationContext(), "Deleting route #" + pos + ".",
		//		Toast.LENGTH_SHORT).show();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		deleteRoute(preferences, pos);
		startActivity(getIntent()); 
		finish();
	}

	private void deleteRoute(SharedPreferences preferences, int routePosition) {
		routes.remove(routePosition);
		storeRoutes(preferences, routes);
	}

	public String getRouteUrl(Route route) {
		String baseUrl = "https://maps.googleapis.com/maps/api/staticmap?path=color:0x0000ff%7Cweight:5%7C";
		int i = 0;
		for (LatLng pt : route.getPath()) {
			if (i == 0) {
				String point = pt.latitude + "," + pt.longitude;
				baseUrl += point;
			} else {
				String point = "%7C" + pt.latitude + "," + pt.longitude;
				baseUrl += point;
			}
			i++;
		}
		baseUrl += "&size=1080x1080";
		baseUrl += "&zoom=18";
		baseUrl += "&key=AIzaSyBlSoG9MOexZBwYnwRQq0QWVGY9a7eDab0";
		return baseUrl;
	}
}
