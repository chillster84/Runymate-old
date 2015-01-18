package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class RoutesActivity extends Activity {

	ListView list;
	RoutesImageAdapter adapter;
	List<Route> routes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);
		routes = getSampleRoutes();
         
		list = (ListView) findViewById(R.id.listView1);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				Intent intent = new Intent(getApplicationContext(),
						RunsActivity.class);
				startActivity(intent);
			}
		});
		adapter = new RoutesImageAdapter(this, routes);
        list.setAdapter(adapter);
    }
     
	private List<Route> getSampleRoutes() {
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

		// put in list
		routes.add(routeA);
		routes.add(routeB);
		routes.add(routeC);
		routes.add(routeD);
		routes.add(routeE);
		return routes;
	}

	@Override
    public void onDestroy()
    {
        list.setAdapter(null);
        super.onDestroy();
    }
}
