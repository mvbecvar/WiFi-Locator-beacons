/**
 * MainApplication
 * 
 * Used to set up the WiFi broadcast receiver which will listen
 * to the WiFi signals and send the information about estimated position
 * 
 * 
 */
package edu.ncsu.wifilocator;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import android.os.RemoteException;
import com.google.android.gms.maps.model.LatLng;

public class MainApplication extends Application {
	
	WifiManager wifi;
	BroadcastReceiver wifiDataReceiver = null;
	DefaultHttpClient httpClient;		//to send the data
	public Timer timer;
	
	public MainActivity mainActivity = null;
	public boolean inIbeaconRange = false;
	boolean shouldScan = false;
	
	// url to get the estimated position
    private static String url_position = "http://people.engr.ncsu.edu/mvbecvar/get_position.php";
    
    // JSON Node names
    private static final String TAG_POINTS = "coordinate";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    private static final String TAG_LOC = "loc";

    
    // contacts JSONArray
    JSONArray position = null;
    
    private int timer_t = 5000;  // changed from 40000 for testing
    
    @Override
	public void onCreate() {
		super.onCreate();
        //beaconManager = new BeaconManager(getApplicationContext());
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        // Register to get the Signal Strengths
     	wifiDataReceiver = new BroadcastReceiver(){
     		// onReceive for the WiFi broadcast signal
     		@Override
     		public void onReceive(Context c, Intent intent){
     			if(shouldScan && !inIbeaconRange){
     				// Scan signal strengths if it is time
     			    List<ScanResult> results = wifi.getScanResults();

     			    String postParameters = "success=1";

     			    for(int i = 0; i < results.size(); i++){
     			    	ScanResult result = results.get(i);
     			    	String parameters = "&";

						// Add this signal strength reading as a parameter where the name is the BSSID
						String measurementString = result.BSSID + "=" + Integer.toString(result.level);

							parameters = parameters + measurementString;
							postParameters = postParameters + parameters;
     			    }

     			    //send data to the server
     			   new PostJSONDataAsyncTask(c, postParameters, url_position, false){
    	                // Override the onPostExecute to do whatever you want
    	                @Override
    	                protected void onPostExecute(String response)
    	                {
    	                    super.onPostExecute(response);
    	                    
    	                    if (response != null){
    	                    	Log.d("wifiloc", response);
    	                    	
    	                    	JSONObject json = null;
    	                    	
    	                    	try{
    	                    		json = new JSONObject(response);
    	                    	} catch (JSONException e){
    	                            Log.d("wifiloc", e.toString());
    	                    	}
    	            			
    	            			if(json == null){
    	                        	Log.d("wifiloc", "Error parsing server response");
    	                            return;
    	                        }
    	            			
    	                        // If returned object length is >0
    	                        if(json.length() > 0){
    	                			try {
    	                				if (json.has("error")){
        	                        		String errorMessage = json.getString("error").toString();
        	                                // Check errors
        	                                if(errorMessage.equalsIgnoreCase("Could not find any matches in the db"))
        	                                {
        	                                	if(mainActivity != null)
            	                                {
            	                					Log.d("wifiloc", errorMessage);
            	                                    mainActivity.updateStatus(errorMessage);
            	                                }
        	                                }
        	                        	}
        	                        	else{
        	                        		double lat = json.getDouble(TAG_LAT);
        	                				double lng = json.getDouble(TAG_LNG);
        	                				String loc = json.getString(TAG_LOC);
        	                				LatLng coordinate = new LatLng(lat, lng);
        	                				
        	                				if(mainActivity != null)
        	                                {
        	                					Log.d("wifiloc", "good to draw");
        	                					Toast.makeText(getApplicationContext(), "location is "+loc, Toast.LENGTH_SHORT).show();
        	                                    mainActivity.updateLocation(coordinate, loc);
        	                                    mainActivity.getCurrentloc(coordinate,loc);
        	                                }
        	                        	}
    	                	        } catch (JSONException e) {
    	                	            e.printStackTrace();
    	                	        }
    	                        }
    	                    }
    	                    else
    	                    {
    	                    	//log the error
    	                    	Log.d("wifiloc", "Error Connecting to Server");
    	                    }
    	                    
    	                }
    	            }.execute();
					
    	            //log successfully sent location
                    Log.d("wifiloc", "Successfully sent location info! :D");
     			    
                    shouldScan = false;
     			}
     		}
     	};
     		    
     	// register to receive the WiFi updates
     	registerReceiver(wifiDataReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); 
     	
     	timer = new Timer();	//timer to periodically update the location
        timer.scheduleAtFixedRate(new UpdateLocationTask(), 50, timer_t);
        
	}

	@Override
	public void onTerminate() {
		unregisterReceiver(wifiDataReceiver);
    	wifiDataReceiver = null;
    	timer.cancel();
		super.onTerminate();      
	}
	
	public void updateTimerInterval(int t){
		timer_t = t;
		timer.cancel();
		timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateLocationTask(), 50, timer_t);
	}
	
	/**
	 * UpdateLocationTask
	 * 
	 * class which periodically scans the WiFi 
	 */
	class UpdateLocationTask extends TimerTask {
        @Override
		public void run() {
        	shouldScan = true;
        	if(wifi.startScan() == true){
        		// Great
        	} else {
        		// TODO We could do something like try again in 5 seconds
        	}
        }
    }
}
