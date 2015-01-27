package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;


public class Run {

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
}

