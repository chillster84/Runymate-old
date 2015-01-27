package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Route {

	String name;
	List<LatLng> path;

	public Route(String name) {
		super();
		this.name = name;
		this.path = new ArrayList<LatLng>();
	}

	public void addPoint(double latitude, double longitude) {
		LatLng pt = new LatLng(latitude, longitude);
		path.add(pt);
	}

	public String getName() {
		return name;
	}

	public List<LatLng> getPath() {
		return path;
	}

	public void setPath(List<LatLng> path) {
		this.path = path;
	}

}

