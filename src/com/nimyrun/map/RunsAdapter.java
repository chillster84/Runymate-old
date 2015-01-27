package com.nimyrun.map;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RunsAdapter extends BaseAdapter {

	private Activity activity;
	private List<Run> runs;
	private static LayoutInflater inflater = null;

	public RunsAdapter(Activity a, List<Run> r) {
		activity = a;
		runs = r;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return runs.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.row_layout_2, null);

		TextView text = (TextView) vi.findViewById(R.id.text2);
		TextView distanceText = (TextView) vi.findViewById(R.id.text3);
		TextView timeText = (TextView) vi.findViewById(R.id.text4);
		Run run = runs.get(position);
		text.setText("Run #" + position);
		distanceText.setText("Distance Travelled: " + run.getDistance());
		timeText.setText("Time Taken: " + run.getTime());

		return vi;
	}

}