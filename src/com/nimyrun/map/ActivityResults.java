package com.nimyrun.map;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import com.androidplot.xy.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
 
/**
 * A straightforward example of using AndroidPlot to plot some data.
 */
public class ActivityResults extends Activity {
 
    private XYPlot plot;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        // fun little snippet that prevents users from taking screenshots
        // on ICS+ devices :-)
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
        //                         WindowManager.LayoutParams.FLAG_SECURE);
 
        setContentView(R.layout.activity_results);
 
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        
        HashMap<String, Number> speedMap = (HashMap<String, Number>) getIntent().getSerializableExtra("speedPoints");
        List list = new ArrayList(speedMap.values());
        //Collections.sort(list);
        
        //double[] speedPoints = getIntent().getDoubleArrayExtra("speedPoints");
        //ArrayList<Number> series1Numbers = new ArrayList<Number>(Arrays.asList(speedPoints));
        
        // Create a couple arrays of y-values to plot:
        //Number[] series1Numbers = speedMap.values(); //{1, 8, 5, 2, 7, 4};
        //Number[] series2Numbers = {4, 6, 3, 8, 2, 10};
        
        /*for (int i = 0; i < speedPoints.length; i++) {
       	 series1Numbers[i] = speedPoints[i];
       	 }*/
 
        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
        		list, //Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Current run");                             // Set the display title of the series
 
        // same as above
        //XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");
 
        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(), R.xml.line_point_formatter_with_plf1);
        
        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.setPlotMargins(10, 10, 10, 10);
        plot.setPlotPadding(10, 10, 10, 10);
 
        // same as above:
        /*
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
        plot.addSeries(series2, series2Format);
        */
 
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
 
    }
}