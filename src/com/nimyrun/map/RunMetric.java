package com.nimyrun.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class RunMetric implements Parcelable {

	LatLng latlng;
	double speed;
	double heartRate;
	int totalStepsTaken;
	double timestamp;


	public RunMetric(LatLng latlng, double speed, double heartRate, int totalStepsTaken,
			double timestamp) {
		super();
		this.latlng = latlng;
		this.speed = speed;
		this.heartRate = heartRate;
		this.totalStepsTaken = totalStepsTaken;
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

	public int getTotalStepsTaken() {
		return totalStepsTaken;
	}

	protected RunMetric(Parcel in) {
		latlng = (LatLng) in.readValue(LatLng.class.getClassLoader());
		speed = in.readDouble();
		heartRate = in.readDouble();
		totalStepsTaken = in.readInt();
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
		dest.writeInt(totalStepsTaken);
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