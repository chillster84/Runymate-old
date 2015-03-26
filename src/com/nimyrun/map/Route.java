package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Route implements Parcelable {

	String name;
	List<LatLng> path;
	List<Run> runs;

	public Route(String name) {
		super();
		this.name = name;
		this.path = new ArrayList<LatLng>();
		this.runs = new ArrayList<Run>();
	}

	public void addPoint(double latitude, double longitude) {
		LatLng pt = new LatLng(latitude, longitude);
		path.add(pt);
	}

	public void addRun(Run run) {
		runs.add(run);
	}

	public void setRuns(List<Run> runs) {
		this.runs = runs;
	}

	public List<Run> getRuns() {
		return runs;
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

	/*
	 * Checks if a point is on a given route
	 */
	public boolean isPointInRoute(LatLng point) {
		for (LatLng pathPoint : path) {
			if (isPointCloseToPathPoint(point, pathPoint)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPointCloseToPathPoint(LatLng point, LatLng pathPoint) {
		float threshold = 10;
		float[] result = { 0, 0 };
		Location.distanceBetween(point.latitude, point.longitude,pathPoint.latitude, pathPoint.longitude,result);
		if (result[0] < threshold) {
			return true;
		} else {
			return false;
		}
	}

	protected Route(Parcel in) {
		name = in.readString();
		if (in.readByte() == 0x01) {
			path = new ArrayList<LatLng>();
			in.readList(path, LatLng.class.getClassLoader());
		} else {
			path = null;
		}
		if (in.readByte() == 0x01) {
			runs = new ArrayList<Run>();
			in.readList(runs, Run.class.getClassLoader());
		} else {
			runs = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		if (path == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(path);
		}
		if (runs == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(runs);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		@Override
		public Route createFromParcel(Parcel in) {
			return new Route(in);
		}

		@Override
		public Route[] newArray(int size) {
			return new Route[size];
		}
	};
}