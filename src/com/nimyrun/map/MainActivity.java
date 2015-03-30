package com.nimyrun.map;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventEcg;
import com.bionym.ncl.NclEventEcgStart;
import com.bionym.ncl.NclEventEcgStop;
import com.bionym.ncl.NclEventNotified;
import com.bionym.ncl.NclEventType;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity implements LocationListener {
	private final static double EARTH_RADIUS = 6371000; // in metres
	private final static int LOCATION_MIN_TIME = 1; // in seconds
	private final static int SAMPLING_INTERVAL = 5;
	private final static int ZOOM_LEVEL = 18; // on a scale of 1 - 22

	//private TextView latitudeField;
	//private TextView longitudeField;
	//private TextView prevLatitudeField;
	//private TextView prevLongitudeField;
	private TextView speedField;
	private ImageView speedImage;
	private ImageView speedBlockerImage;
	private TextView heartField;
	private ImageView heartPeakImage;
	private Button button;

	private LocationManager locationManager;
	private Location location;
	private String provider;
	private GoogleMap gMap;
	private Polyline polyline;

	private Timer timer;

	private double latitude = 0;
	private double longitude = 0;
	private double newLatitude = 0;
	private double newLongitude = 0;
	//private double lastValidLatitude = 0;
	//private double lastValidLongitude = 0;
	private double speed = 0;
	private double heartbeat = 0;
	private double distance = 0;

	private int count = 0;
	private int errorFactor = 1;
	private HashMap<String, Double> speedMap = new HashMap<String, Double>();
	private HashMap<String, Double> heartMap = new HashMap<String, Double>();
	private List<RunMetric> runMetrics = new ArrayList<RunMetric>();

	// new route or existing route?
	private boolean isNewRoute;
	private int routePosition;

	// Horizontal offset of speed animation graphic from previous speed
	// measurement.
	private int previousSpeedAnimationXOffset = 0;

	// Instantiate a new Polyline object for drawing the current route on the
	// map.
	private PolylineOptions rectOptions = new PolylineOptions();
	
	protected int nymiHandle;
	public int interval=60; //default
	protected NclCallback nclCallback;
	double heart_rate = 0;
	boolean stopped = false;
	boolean notified = false;
	boolean startingECG = true;
	Activity mActivity = this;
	public static List<Integer> ecgSamples = new ArrayList<Integer>();
	public static List<Integer> possibleRvalues = new ArrayList<Integer>();
	private int mStepValue;
    private int mPaceValue;
    private float mDistanceValue;
    private float mSpeedValue;
    private int mCaloriesValue;
    private Utils mUtils;
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private boolean mIsRunning;
    private static final String TAG = "Pedometer";
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy
    CountDownTimer runIntervalTimer;
	Route route;
	private String m_Text = "";

	/*
	 * Set actions for initial creation of activity.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get info from caller
		Bundle b = getIntent().getExtras();
		nymiHandle = b.getInt("nymiHandle");
		LoginScreen.appendLog("onCreate", "nymiHandle passed over to mainactivity is " + nymiHandle);
		//interval = b.getInt("interval");
		interval = 20;
		LoginScreen.appendLog("onCreate", "interval is " + interval);
		isNewRoute = (boolean) b.getBoolean("isNewRoute");
		LoginScreen.appendLog("onCreate", "isnewRoute " + isNewRoute);
		if (!isNewRoute) {
			routePosition = (int) b.getInt("routePosition");
			LoginScreen.appendLog("onCreate", "routeposition " + routePosition);
		}
		
		if (nclCallback == null) {
			nclCallback = new MyNclCallbackRun();
		}
        
        Ncl.addBehavior(nclCallback, null, NclEventType.NCL_EVENT_ANY, nymiHandle);
        LoginScreen.appendLog("onCreate", "added ncl behaviour");

		// Assign views to variables
		
		// DEBUG MESSAGES
		//latitudeField = (TextView) findViewById(R.id.TextView02);
		//longitudeField = (TextView) findViewById(R.id.TextView04);
		//prevLatitudeField = (TextView) findViewById(R.id.TextView06);
		//prevLongitudeField = (TextView) findViewById(R.id.TextView08);
		
		speedField = (TextView) findViewById(R.id.speedValue);
		speedImage = (ImageView) findViewById(R.id.speedImage);
		speedBlockerImage = (ImageView) findViewById(R.id.speedBlockerImage);
		heartField = (TextView) findViewById(R.id.heartValue);
		heartPeakImage = (ImageView) findViewById(R.id.heartPeakImage);
		button = (Button) findViewById(R.id.button01);
		LoginScreen.appendLog("onCreate", "just doing view ids");
		
		mStepValue = 0;
        mPaceValue = 0;
        
        mUtils = Utils.getInstance();
        LoginScreen.appendLog("onCreate", "mutils got instance");
        
        runIntervalTimer = new CountDownTimer(interval*1000, 1000) { //15second intervals for now
			public void onTick(long millisUntilFinished) {
				LoginScreen.appendLog("runIntervalTimer", "seconds remaining: " + millisUntilFinished / 1000);
			}
    		public void onFinish() {
    			startingECG = true;
    			Ncl.notify(nymiHandle, true);
    			readECG();
    		}
		};
        

		// Initialize map
		try {
			LoginScreen.appendLog("onCreate", "initilize map");
			initilizeMap();
			LoginScreen.appendLog("onCreate", "after initilize map");
			gMap.setMyLocationEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Draw existing route
		if(!isNewRoute) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			route = retrieveRoute(preferences, routePosition);
			PolylineOptions polylineOptions = new PolylineOptions();
			for (LatLng point : route.getPath()) {
				polylineOptions.add(point);
			}
			gMap.addPolyline(polylineOptions);
		}

		// Get the location manager
		LoginScreen.appendLog("onCreate", "locationManager getService");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LoginScreen.appendLog("onCreate", "after locationManager getService");
		// Define the criteria for selecting the location provider (default
		// used)
		Criteria criteria = new Criteria();
		LoginScreen.appendLog("onCreate", "after criter");
		provider = locationManager.getBestProvider(criteria, false);
		LoginScreen.appendLog("onCreate", "after getbestprov");
		location = locationManager.getLastKnownLocation(provider);
		LoginScreen.appendLog("onCreate", "after lastknown");

		// Initialize location fields
		if (location != null) {
			/*
			 * new CountDownTimer(10000, 1000) {
			 * 
			 * public void onTick(long millisUntilFinished) {
			 * latitudeField.setText("seconds remaining: " + millisUntilFinished
			 * / 1000); }
			 * 
			 * public void onFinish() { latitudeField.setText("done!"); }
			 * }.start();
			 */
			LoginScreen.appendLog("oncreate if location !=null:  ","Provider " + provider + " has been selected.");

			onLocationChanged(location);
			
			setTimer(SAMPLING_INTERVAL, 5);
		} else {
			//latitudeField.setText("Location not available.");
			//longitudeField.setText("Location not available.");
		}
		
		runIntervalTimer(null);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
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
		
		LoginScreen.appendLog("End of oncreate"," Here");
	}

	/*
	 * Request updates at startup.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		LoginScreen.appendLog("onresume", "b4 initilize");
		initilizeMap();
		LoginScreen.appendLog("onresume()", "after initilize");
		locationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME * 1000, 0,
				this);
		LoginScreen.appendLog("onresume()", "after requestlocationupdates");
		updateLocation();
		LoginScreen.appendLog("onresume", "after updatelocation");
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        
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
	}

	/*
	 * Remove locationlistener updates when the activity is paused.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		//locationManager.removeUpdates(this);
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
	}

	/*
	 * Set actions on location change. (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onLocationChanged(android.location.
	 * Location)
	 */
	
	@Override
	public void onLocationChanged(Location newLocation) {
		location = newLocation;
		LoginScreen.appendLog("onlocationchanged", " = " + newLocation.toString());
		if (!isNewRoute) {
			LatLng point = new LatLng(location.getLatitude(),
					location.getLongitude());
			if (!route.isPointInRoute(point, location.getAccuracy())) {
				// user is outside the route
				Toast.makeText(this, "You went off route. Run terminated!",
						Toast.LENGTH_SHORT).show();
				locationManager.removeUpdates(this);
				locationManager = null;
				
				Ncl.removeBehavior(nclCallback, null, NclEventType.NCL_EVENT_ANY, nymiHandle);
				LoginScreen.appendLog("offroute, ", "removed Nymi actions");
				runIntervalTimer.cancel();
				LoginScreen.appendLog("offroute, ", "cancelled interval timer");
				if (mIsRunning) {
		            unbindStepService();
		            stopStepService();
		            LoginScreen.appendLog("offroute, ", "stopping pedometer serv");
		        }
		        if (mQuitting) {
		            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
		        }
		        else {
		            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
		        }
				
				Intent intent = new Intent(getApplicationContext(),
						RouteSelectionActivity.class);
				intent.putExtra("validated", true);
				intent.putExtra("nymiHandle", nymiHandle);
				startActivity(intent);
			}
		}
		updateLocation();
	}

	/*
	 * Set actions on status change. (non-Javadoc)
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String,
	 * int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			Toast.makeText(this, "GPS Provider out of service",
					Toast.LENGTH_SHORT).show();
		} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			Toast.makeText(this, "GPS Provider temporarily unavailable",
					Toast.LENGTH_SHORT).show();
		} else if (status == LocationProvider.AVAILABLE) {
			Toast.makeText(this, "GPS Provider available", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/*
	 * Set actions on provider enabled. (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * Set actions on provider disabled. (non-Javadoc)
	 * 
	 * @see
	 * android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	
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
                    mStepValue = (int)msg.arg1; //HOSSEIN: Updated steps value. send to runmetric? idk
                    LoginScreen.appendLog("step count", " = " + mStepValue);
                    //HOSSEIN PLS
                    break;
                case PACE_MSG:
                    mPaceValue = msg.arg1;
                    LoginScreen.appendLog("pace count", " = " + mPaceValue);
                    break;
                case DISTANCE_MSG:
                    mDistanceValue = ((int)msg.arg1)/1000f;
                    LoginScreen.appendLog("distance count", " = " + mDistanceValue);
                    break;
                case SPEED_MSG:
                    mSpeedValue = ((int)msg.arg1)/1000f;
                    LoginScreen.appendLog("speed count", " = " + mSpeedValue);
                    break;
                case CALORIES_MSG:
                    mCaloriesValue = msg.arg1;
                    LoginScreen.appendLog("calories count", " = " + mStepValue);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
	
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
            startService(new Intent(MainActivity.this,
                    StepService.class));
        }
    }
    
    private void bindStepService() {
        LoginScreen.appendLog(TAG, "[SERVICE] Bind");
        bindService(new Intent(MainActivity.this, 
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
        
        if (mService != null && mIsRunning) {
        	LoginScreen.appendLog("bind step service", "mService is not null");
            mService.resetValues();                    
        }
        else {    
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
                stateEditor.putInt("steps", 0);
                stateEditor.putInt("pace", 0);
                stateEditor.putFloat("distance", 0);
                stateEditor.putFloat("speed", 0);
                stateEditor.putFloat("calories", 0);
                stateEditor.commit();
        }
        LoginScreen.appendLog(TAG, "[SERVICE] Bind - reset step values");
    }

    private void unbindStepService() {
        LoginScreen.appendLog(TAG, "[SERVICE] Unbind");
        unbindService(mConnection);
    }
    
    private void stopStepService() {
        LoginScreen.appendLog(TAG, "[SERVICE] Stop");
        if (mService != null) {
            LoginScreen.appendLog(TAG, "[SERVICE] stopService");
            stopService(new Intent(MainActivity.this,
                  StepService.class));
        }
        mIsRunning = false;
    }

	/*
	 * Set actions for button clicks.
	 */
	public void onButtonClick(View v) {
		if (v.getId() == R.id.button01) {

			Run run = new Run(distance, (double) (count * LOCATION_MIN_TIME));
			run.setRunMetrics(runMetrics);
			
			LoginScreen.appendLog("clicked Finish, ", "set run metrics");
			Ncl.removeBehavior(nclCallback, null, NclEventType.NCL_EVENT_ANY, nymiHandle);
			LoginScreen.appendLog("clicked Finish, ", "removed Nymi actions");
			locationManager.removeUpdates(this);
			locationManager = null;
			runIntervalTimer.cancel();
			LoginScreen.appendLog("clicked Finish, ", "cancelled interval timer");
			if (mIsRunning) {
	            unbindStepService();
	            stopStepService();
	            LoginScreen.appendLog("clicked Finish, ", "stopping pedometer serv");
	        }
	        if (mQuitting) {
	            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
	        }
	        else {
	            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
	        }
			
			final Intent intent = new Intent(getApplicationContext(),
					ActivityResults.class);
			intent.putExtra("speedPoints", speedMap);
			intent.putExtra("distance", distance);
			intent.putExtra("time", (double) (count * LOCATION_MIN_TIME));
			intent.putExtra("heartPoints", heartMap);
			intent.putExtra("run", run);
			// set to true when this is a newly captured route
			intent.putExtra("isNewRoute", isNewRoute);
			
			
			if (isNewRoute) {
				
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
				LoginScreen.appendLog("clicked Finish, ", "building alert");
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle("Good job!");
				builder.setMessage("Please select a name for this run");

				// Set up the input
				final EditText input = new EditText(mActivity);
				builder.setView(input);

				// Set up the buttons
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								m_Text = input.getText().toString();
								intent.putExtra("routeName", m_Text);
								startActivity(intent);
							}
						});

				builder.show();
					}
				});
				LoginScreen.appendLog("clicked Finish, ", "built alert");
			}
			
			else {
				intent.putExtra("routePosition", routePosition);
				startActivity(intent);
			}
			
			
		}

	}

	/*
	 * Set animation for heart beat and speed measurements.
	 */
	private void setAnimation(ImageView image, double value) {
		if (image.equals(speedBlockerImage)) {
			TranslateAnimation animation = new TranslateAnimation(
					previousSpeedAnimationXOffset, (int) (value / 0.2 * 576),
					0, 0);
			animation.setDuration(LOCATION_MIN_TIME * 500);
			animation.setFillAfter(true);
			image.startAnimation(animation);
			previousSpeedAnimationXOffset = (int) (value / 0.2 * 576);
		} else if (image.equals(heartPeakImage)) {
			TranslateAnimation animation = new TranslateAnimation(0, -576 * 2,
					0, 0);
			animation.setDuration((int) (60000 / value));
			// animation.setFillAfter(true);
			image.startAnimation(animation);
		}
	}

	/*
	 * Initialize Google map.
	 */
	private void initilizeMap() {
		LoginScreen.appendLog("initilizeMap()", "entered");
		if (gMap == null) {
			LoginScreen.appendLog("initilizeMap()", "gmap null");
			gMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			LoginScreen.appendLog("initilizeMap()", "gmap did getmap");

			// Check if map is created successfully
			if (gMap == null) {
				LoginScreen.appendLog("initilizeMap()", "gmap still null");
				Toast.makeText(getApplicationContext(),
						"Unable to create map.", Toast.LENGTH_SHORT).show();
			}
		}
		LoginScreen.appendLog("initilizeMap()", "exiting");
	}

	/*
	 * Calculate distance using two latitude and longitude values. Spherical law
	 * of cosines formula and example Excel script referenced from
	 * http://www.movable-type.co.uk/scripts/latlong.html.
	 */
	private double getDistance(double lat_1, double long_1, double lat_2, double long_2) {
		// Convert coordinates to radians
		lat_1 = lat_1 * Math.PI / 180.0;
		long_1 = long_1 * Math.PI / 180.0;
		lat_2 = lat_2 * Math.PI / 180.0;
		long_2 = long_2 * Math.PI / 180.0;

		double distance = Math
				.acos(Math.sin(lat_1) * Math.sin(lat_2) + Math.cos(lat_1)
						* Math.cos(lat_2) * Math.cos(long_2 - long_1))
				* EARTH_RADIUS;
		return distance;
	}

	/*
	 * Focus Google map camera to current location.
	 */
	private void getCameraToCurrentLocation(double latitude, double longitude) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude)).zoom(ZOOM_LEVEL)
				.build();
		gMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));
	}

	/*
	 * Update polyline object with a new latitude and longitude.
	 */
	private void updatePolyline(double latitude, double longitude) {
		rectOptions.add(new LatLng(latitude, longitude));
		polyline = gMap.addPolyline(rectOptions);
	}
	
	private void setTimer(int frequency, int delay) {
		final Handler handler = new Handler();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						try {
							sample();
						} catch (Exception e) {
						}
					}
				});
			}
		}, frequency * 1000, delay * 1000);
	}
	
	private void sample() {
		//count = count + SAMPLING_INTERVAL;
		count++;

		if (speed < 15) { // Fastest recorded running speed is about 12 m/s
			//add a runMetric without a heart rate
			addRunMetric(0.0, 0);

			speedMap.put(count + "", speed);
			heartMap.put(count + "", heart_rate);

		}
	}
	
	private void addRunMetric(double currentHeartRate, int totalStepsTaken) {
		LoginScreen.appendLog("in addrunmetric", " adding " + currentHeartRate);
		if(location!=null) {
			LatLng currentPosition = new LatLng(location.getLatitude(),
					location.getLongitude());
			LoginScreen.appendLog("in addrunmetric", "after currentPos");
			double currentTimestamp = System.currentTimeMillis();
			double currentSpeed = speed;
			LoginScreen.appendLog("addrunmetric", "adding heart rate "+ currentHeartRate + " as newRunMetric");
			LoginScreen.appendLog("addrunmetric", "with total steps = "+ totalStepsTaken);
			RunMetric newRunMetric = new RunMetric(currentPosition, LoginScreen.round(currentSpeed, 2),
					LoginScreen.round(currentHeartRate, 2), totalStepsTaken, LoginScreen.round(currentTimestamp,2));
			runMetrics.add(newRunMetric);
		}
		LoginScreen.appendLog("in addrunmetric", " leaving");
	}

	private void updateLocation() {
		LoginScreen.appendLog("updatelocation()", "entered");
		if(location!=null) {
			double newDistance = 0;
			LoginScreen.appendLog("updatelocation()", "location not null anymore?");
			double accuracy = location.getAccuracy();
			LoginScreen.appendLog("updatelocation()", "accuracy = " + accuracy);
			newLatitude = location.getLatitude();
			LoginScreen.appendLog("updatelocation()", "newlat = " + newLatitude);
			newLongitude = location.getLongitude();
			LoginScreen.appendLog("updatelocation()", "newlong = " + newLongitude);

			//prevLatitudeField.setText(String.valueOf(latitude));
			//prevLongitudeField.setText(String.valueOf(longitude));
			//latitudeField.setText(String.valueOf(newLatitude));
			//longitudeField.setText(String.valueOf(newLongitude));
			
			newDistance = getDistance(latitude, longitude, newLatitude,
					newLongitude);
			LoginScreen.appendLog("updatelocation()", "newdistance = " + distance);
			
			speed = newDistance / (LOCATION_MIN_TIME * errorFactor);
			
			speedField.setText(LoginScreen.round(((double) Math.round(speed * 100) / 100), 2) + "");
			//heartbeat = 150 + (count / 5 * 10);
			heartField.setText(LoginScreen.round(heart_rate, 2) + "");
			
			setAnimation(speedBlockerImage, speed);
			setAnimation(heartPeakImage, heartbeat);
			LoginScreen.appendLog("updatelocation()", "set animations");
			
			if (speed < 12 && accuracy < 15) { // If latest detected location makes sense
				distance = distance + newDistance;
				
				try {
					updatePolyline(newLatitude, newLongitude);
					LoginScreen.appendLog("updatelocation()", "update polyline");
					getCameraToCurrentLocation(newLatitude, newLongitude);
					LoginScreen.appendLog("updatelocation()", "get cam to currentlocation");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				latitude = newLatitude;
				longitude = newLongitude;
				
				errorFactor = 1;
			}
			else { // If latest detected location is off
				errorFactor++;
			}
			
			// The first time location is detected - TO IMPROVE LATER
			if (latitude == 0 && longitude == 0) {
				latitude = newLatitude;
				longitude = newLongitude;
			}
		}
		
		LoginScreen.appendLog("updatelocation()", "exiting updateloc()");
	}
	
	public void onStop(){
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		super.onStop();
	}

	// // put in LocalStorageUtils.java
	public static List<Route> retrieveRoutes(SharedPreferences sharedPreferences) {
		String json = sharedPreferences.getString("routes", null);
		Type type = new TypeToken<List<Route>>() {
		}.getType();
		List<Route> routes = new Gson().fromJson(json, type);
		return routes;
	}

	public static Route retrieveRoute(SharedPreferences sharedPreferences,
			int routePosition) {
		List<Route> routes = retrieveRoutes(sharedPreferences);
		return routes.get(routePosition);
	}
	// // Put in LocalStorateUtils.java
	
	
	public void runIntervalTimer(View v) {
		runIntervalTimer.start();
		
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
             
             // add a runMetric with a valid heart rate
             //addRunMetric(heart_rate );
             addRunMetric(heart_rate, mStepValue);
             LoginScreen.appendLog("after addRunMetric", " wasup");
             
    		 
             /* YERUSHA: value heart_rate after 60second interval. something with sample() or addmetric or something
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
    	 				LoginScreen.appendLog("started interval timer again", " after getting HR");
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
