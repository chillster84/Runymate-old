/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nymirun.nymirun;


import java.util.ArrayList;
import java.util.List;

import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventEcg;
import com.bionym.ncl.NclEventEcgStart;
import com.bionym.ncl.NclEventEcgStop;
import com.bionym.ncl.NclEventNotified;
import com.bionym.ncl.NclEventType;
import com.nymirun.nymirun.CurrentRun.MyNclCallbackRun;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;



public class Pedometer extends Activity {
	private static final String TAG = "Pedometer";
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private Utils mUtils;
    
    private TextView mStepValueView;
    private TextView mPaceValueView;
    private TextView mDistanceValueView;
    private TextView mSpeedValueView;
    private TextView mCaloriesValueView;
    TextView mDesiredPaceView;
    private int mStepValue;
    private int mPaceValue;
    private float mDistanceValue;
    private float mSpeedValue;
    private int mCaloriesValue;
    private float mDesiredPaceOrSpeed;
    private int mMaintain;
    private boolean mIsMetric;
    private float mMaintainInc;
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy
    
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

    
    /**
     * True, when service is running.
     */
    private boolean mIsRunning;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onCreate");
        super.onCreate(savedInstanceState);
        
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            nymiHandle =(int) b.get("nymiHandle");
            //interval = (int) b.get("interval");
            interval = 20;
        }
        
        if (nclCallback == null) {
			nclCallback = new MyNclCallbackRun();
		}
        
        tvIntervalHR = (TextView) findViewById(R.id.tvIntervalHR);
	
        Ncl.addBehavior(nclCallback, null, NclEventType.NCL_EVENT_ANY, nymiHandle);
        
        mStepValue = 0;
        mPaceValue = 0;
        
        setContentView(R.layout.activity_pedometer);
        
        mUtils = Utils.getInstance();
    }
    
    @Override
    protected void onStart() {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onResume");
        super.onResume();
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
        mUtils.setSpeak(mSettings.getBoolean("speak", false));
        
        // Read from preferences if the service was running on the last onPause
        mIsRunning = mPedometerSettings.isServiceRunning();
        
        // Start the service if this is considered to be an application start (last onPause was long ago)
        if (!mIsRunning && mPedometerSettings.isNewStart()) {
            startStepService();
            bindStepService();
        }
        else if (mIsRunning) {
            bindStepService();
        }
        
        mPedometerSettings.clearServiceRunning();

        mStepValueView     = (TextView) findViewById(R.id.step_value);
        mPaceValueView     = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.distance_value);
        mSpeedValueView    = (TextView) findViewById(R.id.speed_value);
        mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
        mDesiredPaceView   = (TextView) findViewById(R.id.desired_pace_value);

        mIsMetric = mPedometerSettings.isMetric();
        ((TextView) findViewById(R.id.distance_units)).setText(getString(
                mIsMetric
                ? R.string.kilometers
                : R.string.miles
        ));
        ((TextView) findViewById(R.id.speed_units)).setText(getString(
                mIsMetric
                ? R.string.kilometers_per_hour
                : R.string.miles_per_hour
        ));
        
        mMaintain = mPedometerSettings.getMaintainOption();
        ((LinearLayout) this.findViewById(R.id.desired_pace_control)).setVisibility(
                mMaintain != PedometerSettings.M_NONE
                ? View.VISIBLE
                : View.GONE
            );
        if (mMaintain == PedometerSettings.M_PACE) {
            mMaintainInc = 5f;
            mDesiredPaceOrSpeed = (float)mPedometerSettings.getDesiredPace();
        }
        else 
        if (mMaintain == PedometerSettings.M_SPEED) {
            mDesiredPaceOrSpeed = mPedometerSettings.getDesiredSpeed();
            mMaintainInc = 0.1f;
        }
        Button button1 = (Button) findViewById(R.id.button_desired_pace_lower);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDesiredPaceOrSpeed -= mMaintainInc;
                mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
                displayDesiredPaceOrSpeed();
                setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
            }
        });
        Button button2 = (Button) findViewById(R.id.button_desired_pace_raise);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDesiredPaceOrSpeed += mMaintainInc;
                mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
                displayDesiredPaceOrSpeed();
                setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
            }
        });
        if (mMaintain != PedometerSettings.M_NONE) {
            ((TextView) findViewById(R.id.desired_pace_label)).setText(
                    mMaintain == PedometerSettings.M_PACE
                    ? R.string.desired_pace
                    : R.string.desired_speed
            );
        }
        
        
        displayDesiredPaceOrSpeed();
    }
	
    
    private void displayDesiredPaceOrSpeed() {
        if (mMaintain == PedometerSettings.M_PACE) {
            mDesiredPaceView.setText("" + (int)mDesiredPaceOrSpeed);
        }
        else {
            mDesiredPaceView.setText("" + mDesiredPaceOrSpeed);
        }
    }
    
    @Override
    protected void onPause() {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onPause");
        if (mIsRunning) {
            unbindStepService();
        }
        if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }

        super.onPause();
        savePaceSetting();
    }

    @Override
    protected void onStop() {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onStop");
        super.onStop();
    }

    protected void onDestroy() {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onDestroy");
        super.onDestroy();
    }
    
    protected void onRestart() {
        LoginScreen.appendLog(TAG, "[ACTIVITY] onRestart");
        super.onDestroy();
    }

    private void setDesiredPaceOrSpeed(float desiredPaceOrSpeed) {
        if (mService != null) {
            if (mMaintain == PedometerSettings.M_PACE) {
                mService.setDesiredPace((int)desiredPaceOrSpeed);
            }
            else
            if (mMaintain == PedometerSettings.M_SPEED) {
                mService.setDesiredSpeed(desiredPaceOrSpeed);
            }
        }
    }
    
    private void savePaceSetting() {
        mPedometerSettings.savePaceOrSpeedSetting(mMaintain, mDesiredPaceOrSpeed);
    }

    private StepService mService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((StepService.StepBinder)service).getService();

            mService.registerCallback(mCallback);
            mService.reloadSettings();
            
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    

    private void startStepService() {
        if (! mIsRunning) {
            LoginScreen.appendLog(TAG, "[SERVICE] Start");
            mIsRunning = true;
            startService(new Intent(Pedometer.this,
                    StepService.class));
        }
    }
    
    private void bindStepService() {
        LoginScreen.appendLog(TAG, "[SERVICE] Bind");
        bindService(new Intent(Pedometer.this, 
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService() {
        LoginScreen.appendLog(TAG, "[SERVICE] Unbind");
        unbindService(mConnection);
    }
    
    private void stopStepService() {
        LoginScreen.appendLog(TAG, "[SERVICE] Stop");
        if (mService != null) {
            LoginScreen.appendLog(TAG, "[SERVICE] stopService");
            stopService(new Intent(Pedometer.this,
                  StepService.class));
        }
        mIsRunning = false;
    }
    
    private void resetValues(boolean updateDisplay) {
        if (mService != null && mIsRunning) {
            mService.resetValues();                    
        }
        else {
            mStepValueView.setText("0");
            mPaceValueView.setText("0");
            mDistanceValueView.setText("0");
            mSpeedValueView.setText("0");
            mCaloriesValueView.setText("0");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
                stateEditor.putInt("pace", 0);
                stateEditor.putFloat("distance", 0);
                stateEditor.putFloat("speed", 0);
                stateEditor.putFloat("calories", 0);
                stateEditor.commit();
            }
        }
    }

    private static final int MENU_SETTINGS = 8;
    private static final int MENU_QUIT     = 9;

    private static final int MENU_PAUSE = 1;
    private static final int MENU_RESUME = 2;
    private static final int MENU_RESET = 3;
    
    /* Creates the menu items */
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mIsRunning) {
            menu.add(0, MENU_PAUSE, 0, R.string.pause)
            .setIcon(android.R.drawable.ic_media_pause)
            .setShortcut('1', 'p');
        }
        else {
            menu.add(0, MENU_RESUME, 0, R.string.resume)
            .setIcon(android.R.drawable.ic_media_play)
            .setShortcut('1', 'p');
        }
        menu.add(0, MENU_RESET, 0, R.string.reset)
        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
        .setShortcut('2', 'r');
        menu.add(0, MENU_SETTINGS, 0, R.string.settings)
        .setIcon(android.R.drawable.ic_menu_preferences)
        .setShortcut('8', 's')
        .setIntent(new Intent(this, Settings.class));
        menu.add(0, MENU_QUIT, 0, R.string.quit)
        .setIcon(android.R.drawable.ic_lock_power_off)
        .setShortcut('9', 'q');
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PAUSE:
                unbindStepService();
                stopStepService();
                return true;
            case MENU_RESUME:
                startStepService();
                bindStepService();
                return true;
            case MENU_RESET:
                resetValues(true);
                return true;
            case MENU_QUIT:
                resetValues(false);
                unbindStepService();
                stopStepService();
                mQuitting = true;
                finish();
                return true;
        }
        return false;
    }
 
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
        public void paceChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }
        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
        }
        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)(value*1000), 0));
        }
        public void caloriesChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG, (int)(value), 0));
        }
    };
    
    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
    private static final int CALORIES_MSG = 5;
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = (int)msg.arg1;
                    mStepValueView.setText("" + mStepValue);
                    break;
                case PACE_MSG:
                    mPaceValue = msg.arg1;
                    if (mPaceValue <= 0) { 
                        mPaceValueView.setText("0");
                    }
                    else {
                        mPaceValueView.setText("" + (int)mPaceValue);
                    }
                    break;
                case DISTANCE_MSG:
                    mDistanceValue = ((int)msg.arg1)/1000f;
                    if (mDistanceValue <= 0) { 
                        mDistanceValueView.setText("0");
                    }
                    else {
                        mDistanceValueView.setText(
                                ("" + (mDistanceValue + 0.000001f)).substring(0, 5)
                        );
                    }
                    break;
                case SPEED_MSG:
                    mSpeedValue = ((int)msg.arg1)/1000f;
                    if (mSpeedValue <= 0) { 
                        mSpeedValueView.setText("0");
                    }
                    else {
                        mSpeedValueView.setText(
                                ("" + (mSpeedValue + 0.000001f)).substring(0, 4)
                        );
                    }
                    break;
                case CALORIES_MSG:
                    mCaloriesValue = msg.arg1;
                    if (mCaloriesValue <= 0) { 
                        mCaloriesValueView.setText("0");
                    }
                    else {
                        mCaloriesValueView.setText("" + (int)mCaloriesValue);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
    
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
    		 
             /*
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
     		});*/
             
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