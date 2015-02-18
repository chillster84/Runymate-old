package com.nymirun.nymirun;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MapListScreen extends Activity {

	double heart_rate = 0;
	protected int nymiHandle;
	boolean stopped = false;
	Activity mActivity = this;
	public static List<Integer> ecgSamples = new ArrayList<Integer>();
	public static List<Integer> possibleRvalues = new ArrayList<Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list_screen);
		
		Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            nymiHandle =(int) b.get("nymiHandle");
            LoginScreen.appendLog("onCreate", "nymiHandle passed over to maplistscreen is " + nymiHandle);
        }
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_list_screen, menu);
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
	
	/*readECG function. On user click, prompt user to hold sensor 
	 * Start ECG */
	public void testPedometer(View v) {
		Intent intent = new Intent(mActivity, Pedometer.class);
        startActivity(intent);
	}
	
	public void startRun(View v) {
		
		final CharSequence intervals[] = new CharSequence[] {"1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes"};
		
		 mActivity.runOnUiThread(new Runnable() {
	 		public void run() {
		
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle("Performance Tracking Interval");
				builder.setItems(intervals, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        // the user clicked on intervals[which]
				    	Intent intent = new Intent(mActivity, CurrentRun.class);
				    	intent.putExtra("interval", (which+1)*60);
				    	intent.putExtra("nymiHandle", nymiHandle);
				        startActivity(intent);
				    }
				});
				builder.show();
	 		}
		 });
	}

}
