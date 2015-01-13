package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Route {

	String name;
	List<LatLng> route;

	public Route(String name) {
		super();
		this.name = name;
		this.route = new ArrayList<LatLng>();
	}

	public void addPoint(double latitude, double longitude) {
		LatLng pt = new LatLng(latitude, longitude);
		route.add(pt);
	}

	public String getName() {
		return name;
	}

	public List<LatLng> getRoute() {
		return route;
	}

	public void setRoute(List<LatLng> route) {
		this.route = route;
	}

}

