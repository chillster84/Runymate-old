package com.nimyrun.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class RouteSelectionActivity extends Activity {
	
	protected int nymiHandle;
	boolean validated = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_selection);
		
		Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            nymiHandle = b.getInt("nymiHandle");
            validated = b.getBoolean("validated");
            LoginScreen.appendLog("onCreate", "nymiHandle passed over to routesactivity is " + nymiHandle);
        }
	}

	/*
	 * Set actions for button clicks.
	 */
	public void onButtonClick(View v) {
		if (v.getId() == R.id.button1) {
			Intent intent = new Intent(getApplicationContext(),
					MainActivity.class);
			intent.putExtra("isNewRoute", true);
			intent.putExtra("validated", validated);
			intent.putExtra("nymiHandle", nymiHandle);
			startActivity(intent);
		}
		if (v.getId() == R.id.button2) {
			Intent intent = new Intent(getApplicationContext(),
					RoutesActivity.class);
			intent.putExtra("validated", validated);
			intent.putExtra("nymiHandle", nymiHandle);
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
