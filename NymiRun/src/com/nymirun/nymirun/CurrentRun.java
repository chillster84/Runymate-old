package com.nymirun.nymirun;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventEcg;
import com.bionym.ncl.NclEventEcgStart;
import com.bionym.ncl.NclEventEcgStop;
import com.bionym.ncl.NclEventNotified;
import com.bionym.ncl.NclEventType;

public class CurrentRun extends Activity {

	protected int nymiHandle;
	protected NclCallback nclCallback;
	public int interval=0;
	double heart_rate = 0;
	boolean stopped = false;
	boolean notified = false;
	boolean startingECG = true;
	Activity mActivity = this;
	TextView tvIntervalHR;
	public static List<Integer> ecgSamples = new ArrayList<Integer>();
	public static List<Integer> possibleRvalues = new ArrayList<Integer>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_run);
		
		Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            nymiHandle =(int) b.get("nymiHandle");
            interval = (int) b.get("interval");
        }
        
        if (nclCallback == null) {
			nclCallback = new MyNclCallbackRun();
		}
        
        tvIntervalHR = (TextView) findViewById(R.id.tvIntervalHR);
	
        Ncl.addBehavior(nclCallback, null, NclEventType.NCL_EVENT_ANY, nymiHandle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.current_run, menu);
		return true;
	}
	
	/* runIntervalTimer function
	 * We want to record ECG after every interval.
	 * at end of interval, call readECG. */
	public void runIntervalTimer(View v) {
		CountDownTimer runIntervalTimer = new CountDownTimer(interval*1000, 1000) { //15second intervals for now
			public void onTick(long millisUntilFinished) {
				LoginScreen.appendLog("runIntervalTimer", "seconds remaining: " + millisUntilFinished / 1000);
			}
    		public void onFinish() {
    			startingECG = true;
    			Ncl.notify(nymiHandle, true);
    			readECG();
    		}
		}.start();
	}
	
	/* readECG function. start the ECG Stream,
	 * use a timer to record samples. once timer
	 * runs out, stop stream. */
	public void readECG() {
		
		stopped = false;
		
		while(!notified); //spin until Nymi communication channel is free
		
		LoginScreen.appendLog("readECG", "Starting ecg stream");
		if(Ncl.startEcgStream(nymiHandle) == false) {
			//error calling NCL Start ECG Stream. TODO: Abort?
		}
		
		CountDownTimer ecgScan = new CountDownTimer(7000, 1000) {
			public void onTick(long millisUntilFinished) {
				LoginScreen.appendLog("readECG Timer", "seconds remaining: " + millisUntilFinished / 1000);
			}
    		public void onFinish() {
    			stopped = true;
    			LoginScreen.appendLog("readECG", "Stopping ecg stream");
    			if(Ncl.stopEcgStream(nymiHandle) == false) {
					//error calling NCL Start ECG Stream. TODO: Abort?
				}
    		}
		}.start();
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
	
public void calculateHeartRate() {
		
		int delta_x = 5;
		int delta_y = 10000;
		
		try {
			LoginScreen.appendLog("Calculating heart rate", "0");
			
			//ecgSamples array filled
	         for(int i=600; i < ecgSamples.size()-6; i++) {
	        	 if(Math.abs(ecgSamples.get(i) - ecgSamples.get(i-delta_x)) > delta_y) { //check for big enough rise from Q to R
	        		 if(Math.abs(ecgSamples.get(i+delta_x) - ecgSamples.get(i)) > delta_y) { // check for big enough fall from R to S
	        			 if((ecgSamples.get(i) - ecgSamples.get(i+delta_x)) * (ecgSamples.get(i) - ecgSamples.get(i-delta_x)) > 0) {
	        				 //make sure R is higher than Q and S or lower than Q and S (opposite hand measurements upside down)
	        					 possibleRvalues.add(i); //add to R value array
		        				 LoginScreen.appendLog("Possible R value:", i + "\n");
		        				 i=i+50; //skip some samples to avoid duplicates
	        			 }
	        		 }
	        	 }
	         }
	       
	         //have possibleRvalues
	         //look at delta_x's
	         int diff1, diff2, diff3;
	         double max_diff = 0.3;
	         for(int i=0; i < possibleRvalues.size(); i+=4) {
	        	 LoginScreen.appendLog("Possible r value size", possibleRvalues.size() + "\n");
	        	 diff1 = possibleRvalues.get(i + 1) - possibleRvalues.get(i);
	        	 diff2 = possibleRvalues.get(i + 2) - possibleRvalues.get(i + 1);
	        	 diff3 = possibleRvalues.get(i + 3) - possibleRvalues.get(i + 2);
	        	 
	        	 LoginScreen.appendLog("diff1 = ", diff1 + "\n");
	        	 LoginScreen.appendLog("diff2 = ", diff2 + "\n");
	        	 LoginScreen.appendLog("diff3 = ", diff3 + "\n");
	        	 
	        	 if(Math.abs(diff1 - diff2)/diff1 < max_diff && Math.abs(diff2 - diff3)/diff2 < max_diff && Math.abs(diff1 - diff3)/diff1 < max_diff) {
	        		 //make sure the samples are within some range of each other; avoid undetected peak
	        		 heart_rate = 250.0*3.0*60.0/(diff1+diff2+diff3);
	        		 LoginScreen.appendLog("Possible heart rate: ", heart_rate + "\n");
	        		 break;
	        	 }
	         }
	         
	         mActivity.runOnUiThread(new Runnable() {
	 			public void run() {
	 				String result = "Your Heart Rate: " + String.format("%.2f", heart_rate) + " BPM";
	 				tvIntervalHR.append(result);
	 			}
	 		});
	         
	         //clear arrays if user wants to measure again
	         ecgSamples.clear();
	         possibleRvalues.clear();
	         notified = false;
	         
	       //start the interval timer again
	         mActivity.runOnUiThread(new Runnable() {
		 			public void run() {
		 				runIntervalTimer(null);
		 			}
	         });
	         
		} catch (Exception e) {
			LoginScreen.appendLog("exception", e.getMessage());
		}
	}

public void calculateHeartRatePrecise() {
	
	int delta_x = 5;
	int delta_y = 10000;
	
	try {
		LoginScreen.appendLog("Calculating heart rate", "0");
		
		//ecgSamples array filled
         for(int i=600; i < ecgSamples.size()-6; i++) {
        	 if(Math.abs(ecgSamples.get(i) - ecgSamples.get(i-delta_x)) > delta_y) { //check for big enough rise from Q to R
        		 if(Math.abs(ecgSamples.get(i+delta_x) - ecgSamples.get(i)) > delta_y) { // check for big enough fall from R to S
        			 if((ecgSamples.get(i) - ecgSamples.get(i+delta_x)) * (ecgSamples.get(i) - ecgSamples.get(i-delta_x)) > 0) {
        				 //make sure R is higher than Q and S or lower than Q and S (opposite hand measurements upside down)
        					 possibleRvalues.add(i); //add to R value array
	        				 LoginScreen.appendLog("Possible R value:", i + "\n");
	        				 i=i+50; //skip some samples to avoid duplicates
        			 }
        		 }
        	 }
         }
       
         //have possibleRvalues
         //look at delta_x's
         int diff_sum = 0;
         int current_diff;
         double diff_avg = 0;
         int divisor = (possibleRvalues.size()-1);
         for(int i=0; i < possibleRvalues.size()-1; i++) {
        	 current_diff = (possibleRvalues.get(i+1)-possibleRvalues.get(i));
        	 if(current_diff < 250 && current_diff > 75) {
        		 diff_sum += current_diff;
        	 }
        	 else {
        		 divisor--;
        	 }
         }
         

    	 LoginScreen.appendLog("Possible r value size", possibleRvalues.size() + "\n");
         
         diff_avg = diff_sum/divisor;
         
         heart_rate = 250.0*60.0/diff_avg;
         
         LoginScreen.appendLog("Possible heart rate: ", heart_rate + "\n");
		 
         
         mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				String result = "";
 				if(heart_rate > 60 && heart_rate < 200) {
 					result = "Your Heart Rate: " + String.format("%.2f", heart_rate) + " BPM" + "\n";
 				}
 				else {
 					 result = "Error in calculation, skipped.\n";
 				}
 				
 				tvIntervalHR.append(result);
 			}
 		});
         
         //clear arrays if user wants to measure again
         ecgSamples.clear();
         possibleRvalues.clear();
         notified = false;
         
       //start the interval timer again
         mActivity.runOnUiThread(new Runnable() {
	 			public void run() {
	 				runIntervalTimer(null);
	 			}
         });
         
	} catch (Exception e) {
		LoginScreen.appendLog("exception", e.getMessage());
	}
}
	
	/* MyNclCallback. The callback class which
	 * handles all events triggered by the Nymi
	 * band, such as ECG Stream (for now). */
	class MyNclCallbackRun implements NclCallback {

		@Override
		public void call(NclEvent event, Object userData) {
			// TODO Auto-generated method stub

			if (event instanceof NclEventNotified && startingECG == true) {
				LoginScreen.appendLog("Nymi Notify", "event notified");
				notified = true;
			}
			
			if (event instanceof NclEventEcgStart) {
				LoginScreen.appendLog("Nymi Start ECG", "Started ECG Stream on nymi " + 
						((NclEventEcgStart) event).nymiHandle);
				Long tsLong = System.currentTimeMillis()/1000;
		         String ts = tsLong.toString();
		         LoginScreen.appendLog("ECG Started ", ts);
			}
			
			if (event instanceof NclEventEcg) {
				//5 samples come in in ((NclEventEcg) event).samples[i] i = 0-4
				if(!stopped) {
					for(int i=0; i<5; i++) {
						LoginScreen.appendLog("", ((NclEventEcg) event).samples[i] + ", ");
						ecgSamples.add(((NclEventEcg) event).samples[i]);
					}
				}
			}
			
			if (event instanceof NclEventEcgStop) {
				LoginScreen.appendLog("Nymi Stop ECG", "Stopped ECG Stream on nymi " + 
					((NclEventEcgStop) event).nymiHandle);
				
				 //notify once the user can stop recording
				startingECG = false;
		         Ncl.notify(nymiHandle, true);
		         
		       //calculate HR from previous data set while next interval has started
		         calculateHeartRatePrecise();
		         
			}
			
			}
	}
	
}
