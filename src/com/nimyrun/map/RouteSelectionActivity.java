package com.nimyrun.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class RouteSelectionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_selection);
		Toast.makeText(getApplicationContext(),
				"onCreate in RouteSelectionActivity.java!", Toast.LENGTH_SHORT).show();
	}

	/*
	 * Set actions for button clicks.
	 */
	public void onButtonClick(View v) {
		if (v.getId() == R.id.button1) {
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.putExtra("isNewRoute", true);
			startActivity(intent);
		}
		if (v.getId() == R.id.button2) {
			Intent intent = new Intent(getApplicationContext(),
					RoutesActivity.class);
			startActivity(intent);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_selection, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
