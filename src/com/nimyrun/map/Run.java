package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


public class Run implements Parcelable {

	double distance;
	double time;
	List<RunMetric> runMetrics;

	public Run(double distance, double time) {
		super();
		this.distance = distance;
		this.time = time;
		this.runMetrics = new ArrayList<RunMetric>();

	}

	public List<RunMetric> getRunMetrics() {
		return runMetrics;
	}

	public void addRunMetrics(RunMetric runMetric) {
		this.runMetrics.add(runMetric);
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public void setRunMetrics(List<RunMetric> runMetrics) {
		this.runMetrics = runMetrics;
	}

	protected Run(Parcel in) {
		distance = in.readDouble();
		time = in.readDouble();
		if (in.readByte() == 0x01) {
			runMetrics = new ArrayList<RunMetric>();
			in.readList(runMetrics, RunMetric.class.getClassLoader());
		} else {
			runMetrics = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(distance);
		dest.writeDouble(time);
		if (runMetrics == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(runMetrics);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<Run> CREATOR = new Parcelable.Creator<Run>() {
		@Override
		public Run createFromParcel(Parcel in) {
			return new Run(in);
		}

		@Override
		public Run[] newArray(int size) {
			return new Run[size];
		}
	};
}