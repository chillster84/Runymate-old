package com.nimyrun.map;

import com.google.android.gms.maps.model.LatLng;

public class RunMetric {

	LatLng latlng;
	double speed;
	double heartRate;
	double timestamp;

	public RunMetric(LatLng latlng, double speed, double heartRate,
			double timestamp) {
		super();
		this.latlng = latlng;
		this.speed = speed;
		this.heartRate = heartRate;
		this.timestamp = timestamp;
	}
	

	public LatLng getLatlng() {
		return latlng;
	}

	public double getSpeed() {
		return speed;
	}

	public double getHeartRate() {
		return heartRate;
	}

	public double getTimestamp() {
		return timestamp;
	}
}
