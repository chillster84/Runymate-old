package com.nimyrun.map;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class RoutesImageAdapter extends BaseAdapter {

	private Activity activity;
	private List<Route> routes;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public RoutesImageAdapter(Activity a, List<Route> r) {
		activity = a;
		routes = r;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return routes.size();
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
			vi = inflater.inflate(R.layout.row_layout, null);

		TextView text = (TextView) vi.findViewById(R.id.text);
		ImageView image = (ImageView) vi.findViewById(R.id.image);
		Route route = routes.get(position);
		text.setText("Route " + route.getName());
		String routeUrl = getRouteUrl(route);
		imageLoader.DisplayImage(routeUrl, image);
		return vi;
	}

	public String getRouteUrl(Route route) {
		String baseUrl = "https://maps.googleapis.com/maps/api/staticmap?path=color:0x0000ff%7Cweight:5%7C";
		int i = 0;
		for(LatLng pt : route.getPath()) {
			if (i == 0) {
				String point = pt.latitude + "," + pt.longitude;
				baseUrl += point;
			}
			else {
				String point =  "%7C" + pt.latitude + "," + pt.longitude;
				baseUrl += point;
			}
			i++;
		}
		baseUrl += "&size=480x280";
		baseUrl += "&zoom=17";
		baseUrl += "&key=AIzaSyBlSoG9MOexZBwYnwRQq0QWVGY9a7eDab0";
		return baseUrl;
	}
}