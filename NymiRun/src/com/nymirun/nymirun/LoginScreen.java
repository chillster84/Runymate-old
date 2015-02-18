package com.nymirun.nymirun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bionym.ncl.Ncl;
import com.bionym.ncl.NclCallback;
import com.bionym.ncl.NclEvent;
import com.bionym.ncl.NclEventAgreement;
import com.bionym.ncl.NclEventDiscovery;
import com.bionym.ncl.NclEventFind;
import com.bionym.ncl.NclEventInit;
import com.bionym.ncl.NclEventProvision;
import com.bionym.ncl.NclEventValidation;
import com.bionym.ncl.NclMode;
import com.bionym.ncl.NclProvision;

public class LoginScreen extends Activity {

	protected NclCallback cb;
	protected int nymiHandle = Ncl.NYMI_HANDLE_ANY;
	protected NclProvision nymiProvision;
	protected CountDownTimer nymiScan;
	protected State state;
	protected boolean[][] ledPatterns;
	protected static ArrayList<Integer> unwantedNymiList = new ArrayList<Integer>();
	
	protected WakeLock wakeLock;
	
	protected static final int NCL_PROVISION_ID_SIZE = 16;
	
	RadioButton led0;
	RadioButton led1;
	RadioButton led2;
	RadioButton led3;
	RadioButton led4;
	
	Activity mActivity = this;
	
	boolean provisionMode = true;
	
	private ProgressDialog pd;
	private ProgressDialog pdfind;
	
	public static ArrayList<NclProvision> provisions = new ArrayList<NclProvision>();
	
	/* onCreate function
	 * Everything to be done on startup
	 * Includes specifying provisioning or validation mode
	 * If validation mode, load user's Nymi provision from Shared Prefs
	 * Initiate Nymi Communication Library (NCL)
	 * Set onclicklistener for Discover button */
	
	@SuppressLint("SdCardPath")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_screen);
		
		// Using a log file. Delete old one if it exists
		try {
			new File("/mnt/sdcard" + "/nymilog.txt").delete(); 
	    }
	    catch (Exception e) {
	        Log.e("tag", e.getMessage());
	    }
		
		PowerManager mgr = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		//wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		//wakeLock.acquire();
		
		/* try to load user's Nymi configuration. If one exists, save it in the global 'nymiProvision' */
		final SharedPreferences prefs = this.getSharedPreferences("ProvisionSP", Context.MODE_PRIVATE);
		String provid = prefs.getString("id", "NULL");
		String provkey = prefs.getString("key", "NULL");
		
		if (!provid.equals("NULL") || !provkey.equals("NULL")) {
			//get the saved provision, do validation process
			nymiProvision = loadProvision(provid, provkey);
			provisionMode = false;
		}
		else {
			provisionMode = true;
		}
		
		/*Initialization of Nymi Communication Library (NCL)*/
		
		NclCallback nclCallback = new MyNclCallbackLogin();
		
		// If you are running on the android emulator, ip should be "ip of your comp"
		//String ip = "192.168.1.102";
		
		// If you are running on an android device, change it to the IP of the device
		//String ip ="192.168.158.38";
		
		//Initialize NCL Library below for nymulator
		//Ncl.setIpAndPort(ip, 9089);
        //boolean result = Ncl.init(nclCallback, null, "NymiRun", NclMode.NCL_MODE_DEV, this);
        
		//Initialize library below for actual band
		boolean result = Ncl.init(nclCallback, null, "NymiRun", NclMode.NCL_MODE_DEFAULT, this);
		
        if (!result) { // failed to initialize NCL
            Toast.makeText(mActivity, "Failed to initialize NCL library!", Toast.LENGTH_LONG).show();
        }
	}
	
	@Override
    public void onStop() {
        super.onStop();
        //wakeLock.release();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login_screen, menu);
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

	
	/* loadProvision function. There already exists an 
	 * entry for the connection between the Nymi and this app.
	 * Load this connection into the global 'nymiProvision' */
	protected NclProvision loadProvision(String provid, String provkey) {
		
		appendLog("loadProvision", "Loading the stored provision from shared prefs");
		byte[] id = new byte[16];
		byte[] key = new byte[16];
		NclProvision p = new NclProvision();
		
		appendLog("provid", provid);
		appendLog("provkey", provkey);
		
		String[] split1 = provid.substring(1, provid.length()-1).split(", ");
		String[] split2 = provkey.substring(1, provkey.length()-1).split(", ");
		
		for(int i=0; i < split1.length; i++) {
			id[i] = Byte.parseByte(split1[i]);
		}
		
		for(int i=0; i < split2.length; i++) {
			key[i] = Byte.parseByte(split2[i]);
		}
		
		p.id.v = Arrays.copyOf(id, id.length);
		p.key.v = Arrays.copyOf(key, key.length);
		
		appendLog("p.id", Arrays.toString(p.id.v));
		appendLog("p.key", Arrays.toString(p.key.v));
		
		return p;
	}
	
	/* storeProvision function. Update shared preferences with
	 * the key and id pair that represents the connection between
	 * the Nymi and the phone application. */
	protected void storeProvision(NclEventProvision provision) {
		//save the provision key and id to phone, so that it can be loaded up on subsequent startups
		SharedPreferences prefs = this.getSharedPreferences("ProvisionSP", Context.MODE_PRIVATE);
			
		//store in shared preferences
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("key", Arrays.toString(provision.provision.key.v));
		editor.putString("id", Arrays.toString(provision.provision.id.v));
		editor.commit();
	}

	/* showAgreePattern function
	 * Called when a Nymi has been found and user needs to verify 
	 * LED pattern. This function builds a popup which displays
	 * the agreement pattern and requires input from the user on 
	 * whether the pattern matches or not. If there is a match, provision
	 * on this handle. If not, add to unwanted list and scan again. */
	protected void showAgreePattern(final NclEventAgreement agreement) {
		
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				
				//build a dialog box showing the LED agreement pattern
				AlertDialog.Builder builder1 = new AlertDialog.Builder(mActivity);
				View agreeView = View.inflate(getApplicationContext(), R.layout.agreedialog, null);
				builder1.setView(agreeView);    
				
				state = State.PROVISIONING;
				
				led0 = (RadioButton) agreeView.findViewById(R.id.led0);
				led1 = (RadioButton) agreeView.findViewById(R.id.led1);
				led2 = (RadioButton) agreeView.findViewById(R.id.led2);
				led3 = (RadioButton) agreeView.findViewById(R.id.led3);
				led4 = (RadioButton) agreeView.findViewById(R.id.led4);
					
			    led0.setChecked(agreement.leds[0][0]);
				led1.setChecked(agreement.leds[0][1]);
				led2.setChecked(agreement.leds[0][2]);
				led3.setChecked(agreement.leds[0][3]);
				led4.setChecked(agreement.leds[0][4]);
				
			    builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			        	dialog.cancel();
			            //Pattern matches. provision on this nymiHandle
			            if(Ncl.provision(agreement.nymiHandle, false) == false) {
			            	//failed to provision. Abort?
			            }
			        }
			    });
			    
			    builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			    		dialog.cancel();
			            //Pattern does not match. Start discovery again for other nymis
			            //add this handle to unwanted list so don't try to connect again
			            unwantedNymiList.add(agreement.nymiHandle);
			            pd.show();
			                
			            nymiScan = new CountDownTimer(30000, 5000) {
							public void onTick(long millisUntilFinished) {
								appendLog("Countdown Timer", "seconds remaining: " + millisUntilFinished / 1000);
							}

							public void onFinish() {
								appendLog("Countdown Timer", "Finished 30 seconds of scanning");
								
								if(Ncl.stopScan() == false) {
									//error stopping scan. abort?
								}
								appendLog("Countdown Timer", "Stopped Scanning");
								pd.dismiss();
								
								AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

							    builder.setTitle("Discovery Timeout");
							    builder.setMessage("Do you want to go back and try again or just access your data?");

							    builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {

							        public void onClick(DialogInterface dialog, int which) {
							            // Dismiss dialog, user will click discover button again
							            dialog.dismiss();
							        }

							    });

							    builder.setNegativeButton("Access Data", new DialogInterface.OnClickListener() {

							        @Override
							        public void onClick(DialogInterface dialog, int which) {
							            // Go to maps screen
							            dialog.dismiss();
							            Intent intent = new Intent(mActivity, MapListScreen.class);
							            intent.putExtra("validated", false);
							            intent.putExtra("nymiHandle", -1);
						                startActivity(intent);
							        }
							    });

							    AlertDialog alert = builder.create();
							    alert.show();
								
								
							}
						}.start();
			            
						if(Ncl.startDiscovery() == false) {
							//failed to start discovery process. abort?
						}
			        }
			    });

			    AlertDialog alert1 = builder1.create();
			    alert1.show();
			}
		});
	}
	
	private void startNymiDiscovery() {
		
		if(Ncl.startDiscovery() == false) {
			//error starting discovery. Abort?
			state = State.FAILED;
			appendLog("startNymiDiscovery()", "Start discovery failed");
		}
		
		state = State.DISCOVERING;
		
		runOnUiThread(new Runnable() {
			public void run() {
				
				pd = new ProgressDialog(mActivity);
				pd.setTitle("Searching for Nymi Band...");
				pd.setMessage("Please wait");
				pd.setCancelable(false);
				pd.setIndeterminate(true);
				pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pd.dismiss();
						}
					});
					
				pd.show();
		
				nymiScan = new CountDownTimer(15000, 5000) {
					public void onTick(long millisUntilFinished) {
						appendLog("Countdown Timer", "seconds remaining: " + millisUntilFinished/1000);
					}

					public void onFinish() {
						appendLog("Countdown Timer", "Finished 30 seconds of scanning");
				
						if(Ncl.stopScan() == false) {
							//error stopping scan. abort?
						}
						
						pd.dismiss();
						
						AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		
					    builder.setTitle("Discovery Timeout");
					    builder.setMessage("Do you want to try again or just access your data?");
		
					    builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
		
					        public void onClick(DialogInterface dialog, int which) {
					            // call discover process again
					            dialog.dismiss();
					            startNymiDiscovery();
					        }
		
					    });
		
					    builder.setNegativeButton("Access Data", new DialogInterface.OnClickListener() {
		
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					            // Go to maps screen
					            dialog.dismiss();
					            Intent intent = new Intent(mActivity, MapListScreen.class);
					            intent.putExtra("validated", false);
					            intent.putExtra("nymiHandle", -1);
				                startActivity(intent);
					        }
					    });
		
					    AlertDialog alert = builder.create();
					    alert.show();
					}	
				}.start();	
			}
		});
	}
	
	private void startNymiFind() {
		// TODO Auto-generated method stub
		if(Ncl.startFinding(new NclProvision[] {nymiProvision}, false) == false) {
			//error starting find. Abort?
			state = State.FAILED;
			appendLog("startNymiFind()", "Start finding failed");
		}
		
		state = State.FINDING;
		
		runOnUiThread(new Runnable() {
			public void run() {
	
				pdfind = new ProgressDialog(mActivity);
				pdfind.setTitle("Searching for your Saved Nymi...");
				pdfind.setMessage("Please wait");
				pdfind.setCancelable(false);
				pdfind.setIndeterminate(true);
				pdfind.show();
				
				//search for this saved Nymi for 15 seconds
				nymiScan = new CountDownTimer(15000, 5000) {
					public void onTick(long millisUntilFinished) {
						appendLog("Countdown Timer", "seconds remaining: " + millisUntilFinished / 1000);
					}
		    		public void onFinish() {
		    			
						if(Ncl.stopScan() == false) {
							//error stopping scan. abort?
						}
						
						pdfind.dismiss();
						
						AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
						builder.setTitle("Cannot Find Your Nymi");
					    builder.setMessage("Do you want to try again or just access your data?");
					    builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
	
					        public void onClick(DialogInterface dialog, int which) {
					            dialog.dismiss();
					            startNymiFind();
					        }
	
					    });
					    builder.setNegativeButton("Access Data", new DialogInterface.OnClickListener() {
	
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					            // Go to maps screen
					            dialog.dismiss();
					            Intent intent = new Intent(mActivity, MapListScreen.class);
				                intent.putExtra("validated", false);
				                intent.putExtra("nymiHandle", -1);
				                startActivity(intent);
					        }
					    });
						
						AlertDialog alert = builder.create();
					    alert.show();
					}
				}.start();
			}
		});
	}
	
	/* MyNclCallback. The callback class which
	 * handles all events triggered by the Nymi
	 * band, such as initializing, discovering,
	 * validating, etc. */
	class MyNclCallbackLogin implements NclCallback {

		@Override
		public void call(NclEvent event, Object userData) {
			// TODO Auto-generated method stub

			//appendLog("Nymi Callback", this.toString() + ": " + event.getClass().getName());
			
			if (event instanceof NclEventInit) {
				
				appendLog("NCL_EVENT_INIT", "NCL INIT Returned " + ((NclEventInit) event).success + "\n");
				
	            if (((NclEventInit) event).success) {
	            	//if validation mode, just call Ncl.startfinding(). If not, wait for user to press discover button
	            	
	    			if(!provisionMode) {
	    				appendLog("NCL_EVENT_INIT", "Validation Mode: Start finding the Nymi");
	    				startNymiFind();
	    			}
	    			else {
	    				appendLog("NCL_EVENT_INIT", "Discovery Mode: Discover new Nymi");
	    				startNymiDiscovery();
	    			}
	            }
	            
	            else {
	            	runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        Toast.makeText(mActivity, "Failed to initialize NCL library!", Toast.LENGTH_LONG).show();
	                    }
	                });
	            }
			}
			
			else if (event instanceof NclEventDiscovery) {
				appendLog("Nymi Discovery", "Device discovered: " + 
										((NclEventDiscovery) event).nymiHandle + " rssi: " + ((NclEventDiscovery) event).rssi);
				if (state == State.DISCOVERING) { // we are still in discovery mode
					
					//unwantedNymiList contains Nymis already processed that we don't connect to. 
					//if a new Nymi is found, run Agreement process with Ncl.agree. If not, no action. EVENT_DISCOVERY will trigger again.
					if(!unwantedNymiList.contains(((NclEventDiscovery) event).nymiHandle)) {
					
						if(Ncl.stopScan() == false) {
							//error stopping scan. Abort?
						}
						else {
							appendLog("Nymi Discovery", "Stopping discovery process since Nymi found"); 
							
							nymiHandle = ((NclEventDiscovery) event).nymiHandle;
							
							//cancel the 30sec timer
							nymiScan.cancel();
							
							//call agree on this Nymi so user can check pattern
							if(Ncl.agree(nymiHandle) == false) {
								//error calling NCL agree. Abort?
							}
							state = State.AGREEING;
						}
					}
				}
			}
			
			else if (event instanceof NclEventAgreement) {
				if (((NclEventAgreement) event).nymiHandle == nymiHandle && state == State.AGREEING) {
					state = State.AGREED;
					nymiHandle = ((NclEventAgreement) event).nymiHandle;
					ledPatterns = ((NclEventAgreement) event).leds;
					appendLog("Nymi Agreement", "Agreement pattern: " + Arrays.toString(ledPatterns[0]));
					pd.dismiss();
					showAgreePattern((NclEventAgreement) event);
				}
			}
			
			else if (event instanceof NclEventProvision) {
				if (((NclEventProvision) event).nymiHandle == nymiHandle && state == State.PROVISIONING) {
					appendLog("Nymi Provision", "Provision is successful!");
					nymiProvision = ((NclEventProvision) event).provision;
	                state = State.SUCCEEDED;
	                
	                storeProvision((NclEventProvision) event);
	                
	                //Provisioning complete. Go to Map List Screen
	                Intent intent = new Intent(mActivity, MapListScreen.class);
	                intent.putExtra("validated", true);
	                intent.putExtra("nymiHandle", nymiHandle);
	                startActivity(intent);
	                
				}
			}
			
			else if (event instanceof NclEventFind) {
				if (state == State.FINDING) { // finding in progress
					//ensure found provisionID matches that of the provisioned Nymi startFinding() was called on
					appendLog("Nymi Find", "the provision given is " + Arrays.toString(((NclEventFind) event).provisionId.v));
					appendLog("Nymi Find", "the provision matching against is " + Arrays.toString(nymiProvision.id.v));
					
					if(Arrays.equals(((NclEventFind) event).provisionId.v, nymiProvision.id.v)) {
						//ok, same nymi. call validate
						appendLog("Nymi Find", "Found correct Nymi!");
						nymiHandle = ((NclEventFind) event).nymiHandle;
						if(Ncl.validate(((NclEventFind) event).nymiHandle) == false) {
							//failed to start validation. abort?
						}
						else {
							nymiScan.cancel();
							state = State.VALIDATING;
						}
					}
					else {
						//found incorrect Nymi. Keep finding until countdown times out
					}
				}
			}
			
			else if (event instanceof NclEventValidation) {
				if (nymiHandle == ((NclEventValidation) event).nymiHandle) {
					if(Ncl.stopScan() == false) {
						//error stopping scan
					}
					else {
						appendLog("Nymi Validate", "Stopping find process since Nymi Validated");
						state = State.VALIDATED;

						//Validation complete. Go to Map List Screen
		                Intent intent = new Intent(mActivity, MapListScreen.class);
		                intent.putExtra("validated", true);
		                intent.putExtra("nymiHandle", nymiHandle);
		                startActivity(intent);

					}
				}
			}
		}


	}
	
	/* appendLog function. Writes the app log
	 * to a file stored on phone memory. Used
	 * for debugging purposes */
	@SuppressLint("SdCardPath")
	public static void appendLog(String text1, String text2)
	{      
		Log.d(text1, text2);
		File logFile = new File("/mnt/sdcard" + "/nymilog.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	         Long tsLong = System.currentTimeMillis()/1000;
	         String ts = tsLong.toString();
	         BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		     buf.append(ts);
		     buf.newLine();
		     buf.close();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      //buf.append(text1 + " - ");
	      buf.append(text1);
	      buf.append(text2);
	      
	      if(!text1.equals("")) {
	    	  buf.newLine();
	      }
	      
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}

	
	public enum State {
		DISCOVERING, ///< \brief discovery started
		AGREEING, ///< \brief agreement in progress, but hasn't finished yet. \warning Stopping provision operation during this state will cause desynchronization between Nymi state and NCL state
		AGREED, ///< \brief agreement completed User should call \ref accept or \ref reject based on the \ref leds result
		PROVISIONING, ///< \brief provisioning in progress but hasn't finished yet. 
		SUCCEEDED, ///< \brief provision has successfully provisioned a Nymi. User may call \ref provision to obtain the provision data, which is used for starting a \ref Session
		NO_DEVICE, ///< \brief provision has failed due to no active devices in the area. Make sure the Nymi is nearby and is in provisioning mode
		FAILED, ///< \brief NCL initialization has failed, you may attempt to retry \ref init, but you should check if the ble connector is working first
		NO_BLE, ///< \brief the device has no BLE
		BLE_DISABLED, ///< \brief BLE is disabled
		AIRPLANE_MODE, ///< \brief The device is in airplane mode
		CREATED, ///< \brief ready to start provision process
		FINDING, ///< \brief discovery started
		VALIDATING, ///< \brief agreement in progress, but hasn't finished yet. \warning Stopping provision operation during this state will cause desynchronization between Nymi state and NCL state
		VALIDATED, ///< \brief agreement completed User should call \ref accept or \ref reject based on the \ref leds result
	}
}

