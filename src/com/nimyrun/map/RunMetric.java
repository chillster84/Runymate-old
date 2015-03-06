package com.nimyrun.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class RunMetric implements Parcelable {

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

	protected RunMetric(Parcel in) {
		latlng = (LatLng) in.readValue(LatLng.class.getClassLoader());
		speed = in.readDouble();
		heartRate = in.readDouble();
		timestamp = in.readDouble();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(latlng);
		dest.writeDouble(speed);
		dest.writeDouble(heartRate);
		dest.writeDouble(timestamp);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<RunMetric> CREATOR = new Parcelable.Creator<RunMetric>() {
		@Override
		public RunMetric createFromParcel(Parcel in) {
			return new RunMetric(in);
		}

		@Override
		public RunMetric[] newArray(int size) {
			return new RunMetric[size];
		}
	};
}