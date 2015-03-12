package com.nimyrun.map;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import android.graphics.Color;
//import android.view.WindowManager;
//import java.util.Arrays;
//import java.util.Collections;

/**
 * A straightforward example of using AndroidPlot to plot some data.
 */
public class ActivityResults extends Activity {
	private XYPlot speedPlot;
	private XYPlot heartPlot;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		Bundle b = getIntent().getExtras();
		Run run = (Run) b.getParcelable("run");
		boolean isNewRoute = (boolean) b.getBoolean("isNewRoute");

		if (isNewRoute) {
			addNewRouteFromRun(preferences, run);
		} else {
			int routePosition = (int) b.getInt("routePosition");
			addRunToRoute(preferences, run, routePosition);
		}

		// blah

		// fun little snippet that prevents users from taking screenshots
		// on ICS+ devices :-)
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
		// WindowManager.LayoutParams.FLAG_SECURE);

		setContentView(R.layout.activity_results);
		
		HashMap<String, Number> speedMap = (HashMap<String, Number>) getIntent()
				.getSerializableExtra("speedPoints");
		List speedList = new ArrayList(speedMap.values());

		double distance = getIntent().getDoubleExtra("distance", 0);
		TextView distanceField = (TextView) findViewById(R.id.distance);
		distanceField.setText(distance + " m");

		double time = getIntent().getDoubleExtra("time", 0);
		TextView timeField = (TextView) findViewById(R.id.time);
		timeField.setText(time / 60 + " min");

		HashMap<String, Number> heartMap = (HashMap<String, Number>) getIntent()
				.getSerializableExtra("heartPoints");
		List heartList = new ArrayList(heartMap.values());
		TextView heartField = (TextView) findViewById(R.id.heart);
		heartField.setText(getListAverage(heartList) + " bpm");
		
		speedPlot = (XYPlot) findViewById(R.id.speedPlot);
		heartPlot = (XYPlot) findViewById(R.id.heartPlot);

		drawPlot(speedList, speedPlot, 5);
		drawPlot(heartList, heartPlot, 5);
	}
	
	private void drawPlot(List list, XYPlot plot, int xIncrement) {
		int x = 0;
		
		List xInterval = new ArrayList();
		for (Object element : list) {
			xInterval.add(x);
			x = x + xIncrement;
		}
		
			XYSeries series1 = new SimpleXYSeries(
					//list,
					//SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Use element index as x value
					xInterval,
					list,
					"Current run");

			// Create a formatter to use for drawing a series using LineAndPointRenderer
			LineAndPointFormatter series1Format = new LineAndPointFormatter();
			series1Format.setPointLabelFormatter(new PointLabelFormatter());
			series1Format.configure(getApplicationContext(),
					R.xml.line_point_formatter_with_plf1);
			
			// Add new series to the plot
			plot.addSeries(series1, series1Format);
			
			plot.setPlotMargins(10, 10, 10, 10);
			plot.setPlotPadding(10, 10, 10, 10);
			
			// Reduce the number of range labels
			plot.setTicksPerRangeLabel(3);
			plot.getGraphWidget().setDomainLabelOrientation(-45);
			plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 5);
	}

	private double getListAverage(List<Double> values) {
		Double sum = 0.0;
		if (!values.isEmpty()) {
			for (Double value : values) {
				sum += value;
			}
			return sum.doubleValue() / values.size();
		}
		return sum;
	}

	public void onButtonClick(View v) {
		if (v.getId() == R.id.buttonGoBack) {
			Intent intent = new Intent(getApplicationContext(),
					RouteSelectionActivity.class);
			startActivity(intent);
		}

	}

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class
	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	/**
	 * Using Gson, converts object into a json string, and stores with
	 * sharedPreferences
	 * 
	 * @param sharedPreferences
	 * @param routes
	 */
	public static void storeRoutes(SharedPreferences sharedPreferences,
			List<Route> routes) {
		Editor editor = sharedPreferences.edit();
		String json = new Gson().toJson(routes);
		editor.putString("routes", json);
		editor.commit();
	}

	public static void addRunToRoute(SharedPreferences sharedPreferences,
			Run run, int routePosition) {
		// get old route list
		List<Route> routes = retrieveRoutes(sharedPreferences);

		// get the route to be updated
		Route route = routes.get(routePosition);

		// add run the the route
		route.addRun(run);

		// update route in the position
		routes.set(routePosition, route);

		// store updated list of routes
		storeRoutes(sharedPreferences, routes);
	}

	public static void addNewRouteFromRun(SharedPreferences sharedPreferences,
			Run run) {
		// create route from run
		Route route = new Route("blah");
		route.addRun(run);
		for (RunMetric runMetric : run.getRunMetrics()) {
			LatLng point = runMetric.getLatlng();
			route.addPoint(point.latitude, point.longitude);
		}

		// get old route list
		List<Route> routes = retrieveRoutes(sharedPreferences);

		// add new route to route list
		routes.add(route);

		// store updated list of routes
		storeRoutes(sharedPreferences, routes);
	}

	/**
	 * Retrieve json string from sharedPreferences, and using Gson converts the
	 * json string back to object
	 * 
	 * @param sharedPreferences
	 * @return
	 */
	public static List<Route> retrieveRoutes(SharedPreferences sharedPreferences) {
		String json = sharedPreferences.getString("routes", null);
		Type type = new TypeToken<List<Route>>() {
		}.getType();
		List<Route> routes = new Gson().fromJson(json, type);
		if (routes == null) {
			routes = new ArrayList<Route>();
		}
		return routes;
	}

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class
	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

	// ignore these methods they should be moved to the LocalStorageUtil.java
	// class

}