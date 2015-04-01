package com.nimyrun.map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class RouteSelectionActivity extends Activity {
	
	protected int nymiHandle;
	boolean validated = false;
	Activity mActivity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_selection);
		
		Intent iin= getIntent();
        Bundle b = iin.getExtras();
        
        LoginScreen.appendLog("got intent", "from loginscreen");

        if(b!=null)
        {
            nymiHandle = b.getInt("nymiHandle");
            validated = b.getBoolean("validated");
            LoginScreen.appendLog("onCreate", "nymiHandle passed over to routeselectionactivity is " + nymiHandle);
        }
	}

	/*
	 * Set actions for button clicks.
	 */
	public void onButtonClick(View v) {
		if (v.getId() == R.id.button1) {
			
			final CharSequence intervals[] = new CharSequence[] {"30 seconds", "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes"};
			
			 mActivity.runOnUiThread(new Runnable() {
		 		public void run() {
			
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setTitle("Performance Tracking Interval");
					builder.setItems(intervals, new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        // the user clicked on intervals[which]
			
							Intent intent = new Intent(getApplicationContext(),
									MainActivity.class);
							intent.putExtra("isNewRoute", true);
							if(which==0) {
								intent.putExtra("interval", 30);
							}
							else {
								intent.putExtra("interval", (which)*60);
							}
							intent.putExtra("validated", validated);
							intent.putExtra("nymiHandle", nymiHandle);
							
							startActivity(intent);
					    }
					});
					builder.show();
		 		}
			 });
			
		}
		if (v.getId() == R.id.button2) {
			LoginScreen.appendLog("routeselection activity ", "selected browse previous - routesactivity");
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
