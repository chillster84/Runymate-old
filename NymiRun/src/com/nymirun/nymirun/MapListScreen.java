package com.nymirun.nymirun;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventEcg;
import com.bionym.ncl.NclEventEcgStart;
import com.bionym.ncl.NclEventEcgStop;
import com.bionym.ncl.NclEventType;

public class MapListScreen extends Activity {

	Button readECG;
	protected int nymiHandle;
	protected NclCallback nclCallback;
	public static List<Integer> ecgSamples = new ArrayList<Integer>();
	public static List<Integer> possibleRvalues = new ArrayList<Integer>();
	
	protected final int MAX_ECG_SAMPLES = 3750;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_list_screen);
		
		readECG = (Button) findViewById(R.id.readECGButton);
		Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            nymiHandle =(int) b.get("nymiHandle");
            LoginScreen.appendLog("onCreate", "nymiHandle passed over to maplistscreen is " + nymiHandle);
        }
        
        if (nclCallback == null) {
			nclCallback = new MyNclCallbackECG();
		}
	
        Ncl.addBehavior(nclCallback, null, NclEventType.NCL_EVENT_ANY, nymiHandle);
		
		
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
	public void readECG(View v) {
		LoginScreen.appendLog("readECG", "Button clicked");
		
		if(Ncl.startEcgStream(nymiHandle) == false) {
			//error calling NCL Start ECG Stream. TODO: Abort?
		}
	}
	
	/*stopECG function. On user click, stop recording ECG */ 
	public void stopECG(View v) {
		
		if(Ncl.stopEcgStream(nymiHandle) == false) {
			//error calling NCL Start ECG Stream. TODO: Abort?
		}
	}
	
	/* MyNclCallback. The callback class which
	 * handles all events triggered by the Nymi
	 * band, such as ECG Stream (for now). */
	class MyNclCallbackECG implements NclCallback {

		@Override
		public void call(NclEvent event, Object userData) {
			// TODO Auto-generated method stub

			//LoginScreen.appendLog("Nymi Callback", this.toString() + ": " + event.getClass().getName());
			
			if (event instanceof NclEventEcgStart) {
				LoginScreen.appendLog("Nymi Start ECG", "Started ECG Stream on nymi " + 
						((NclEventEcgStart) event).nymiHandle);
				Long tsLong = System.currentTimeMillis()/1000;
		         String ts = tsLong.toString();
		         LoginScreen.appendLog("ECG Started ", ts);
			}
			
			if (event instanceof NclEventEcg) {
				//LoginScreen.appendLog("Nymi ECG Event", "Sample read from nymi " + ((NclEventEcg) event).nymiHandle);
				
				//5 samples come in in ((NclEventEcg) event).samples[i] i = 0-4
				for(int i=0; i<5; i++) {
					//tvECGSamples.append(Integer.toString(((NclEventEcg) event).samples[i]) + ", ");
					LoginScreen.appendLog("", ((NclEventEcg) event).samples[i] + ", ");
					ecgSamples.add(((NclEventEcg) event).samples[i]);
					
				}
				
				if(ecgSamples.size() >= MAX_ECG_SAMPLES) {
					LoginScreen.appendLog("Event ECG", "Maximum ECG Samples received. Stopping ECG Stream");
					if(Ncl.stopEcgStream(nymiHandle) == false) {
						//error calling NCL Start ECG Stream. TODO: Abort?
					}
				}
			}
			
			if (event instanceof NclEventEcgStop) {
				LoginScreen.appendLog("Nymi Stop ECG", "Stopped ECG Stream on nymi " + 
					((NclEventEcgStop) event).nymiHandle);
				
				int delta_x = 5;
				int delta_y = 10000;
				
				Long tsLong = System.currentTimeMillis()/1000;
		         String ts = tsLong.toString();
		         LoginScreen.appendLog("ECG Stopped ", ts);
		         
		         LoginScreen.appendLog("Vibrating", " started");
		         Ncl.notify(nymiHandle, true);
		         LoginScreen.appendLog("Vibrating", " after");
		         
		         //ecgSamples array filled
		         for(int i=600; i < ecgSamples.size(); i++) {
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
		         double heart_rate = 0;
		         double max_diff = 0.3;
		         for(int i=0; i < possibleRvalues.size() + 3; i+=4) {
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
		         
				
			}
			
			}
		}
	}
