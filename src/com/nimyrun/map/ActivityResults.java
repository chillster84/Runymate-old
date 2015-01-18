package com.nimyrun.map;

import android.app.Activity;
//import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
//import android.view.WindowManager;
import com.androidplot.xy.*;

import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A straightforward example of using AndroidPlot to plot some data.
 */
public class ActivityResults extends Activity {
	private XYPlot speedPlot;
	private XYPlot heartPlot;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
}