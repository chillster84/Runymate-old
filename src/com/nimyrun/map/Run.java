package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;


public class Run {

	String routeName;
	double distance;
	double time;
	List<RunMetric> runMetrics;

	public Run(String routeName) {
		super();
		this.routeName = routeName;
		this.runMetrics = new ArrayList<RunMetric>();

	}

	public String getRouteName() {
		return routeName;
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

