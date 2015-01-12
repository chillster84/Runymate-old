package com.nimyrun.map;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RoutesActivity extends ListActivity {

	private TextView text;
	private List<String> listValues;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);

		text = (TextView) findViewById(R.id.mainText);

		listValues = new ArrayList<String>();
		listValues.add("Route 1");
		listValues.add("Route 2");
		listValues.add("Route 3");
		listValues.add("Route 4");
		listValues.add("Add New Route +");

		// initiate the listadapter
		ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,
				R.layout.row_layout, R.id.listText, listValues);

		// assign the list adapter
		setListAdapter(myAdapter);

	}

	// when an item of the list is clicked
	@Override
	protected void onListItemClick(ListView list, View view, int position,
			long id) {
		super.onListItemClick(list, view, position, id);

		String selectedItem = (String) getListView()
				.getItemAtPosition(position);
		// String selectedItem = (String) getListAdapter().getItem(position);

		text.setText("You clicked " + selectedItem + " at position " + position);
	}

}