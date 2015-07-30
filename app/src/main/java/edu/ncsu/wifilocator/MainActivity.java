/**
 * MainActivity
 * 
 * The main screen of the application. Contains a tab layout of the map and the suggest screen.
 * gives users option to search for a route as well as search for a room which fits certain 
 * criteria
 * 
 */
package edu.ncsu.wifilocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.util.Base64;
import java.net.URL;
import java.io.OutputStream;
import android.os.Handler;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.repackaged.gson_v2_3_1.com.google.gson.GsonBuilder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.IndoorLevel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends FragmentActivity implements ListView.OnItemClickListener {

	private static final String ESTIMOTE_PROXIMITY_UUID = "A1234567-B123-C123-D123-E1234567890E";
	private static final Region REGION_ART_WALL = new Region("artwall", ESTIMOTE_PROXIMITY_UUID, 1, null);
    private static final Region REGION_THEATER = new Region("theater", ESTIMOTE_PROXIMITY_UUID, 2, null);
	private static final Region REGION_GAMELAB = new Region("gamelab", ESTIMOTE_PROXIMITY_UUID, 3, null);
	private BeaconManager beaconManager;


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        try {
            beaconManager.stopRanging(REGION_ART_WALL);
            beaconManager.stopRanging(REGION_THEATER);
			beaconManager.stopRanging(REGION_GAMELAB);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }
        beaconManager.disconnect();
		super.onDestroy();
		Log.d("MAP","onDestroy called");
		if(map == null)
		{
			Log.d("MAP","map null");
		}
		else
		{
			map.setIndoorEnabled(false);
			Log.d("MAP","indoor : "+map.isIndoorEnabled());
		}
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("MAP","onPause called");
		if(map == null)
		{
			Log.d("MAP","map null");
		}
		else
		{
			map.setIndoorEnabled(false);
			Log.d("MAP","indoor : "+map.isIndoorEnabled());
		}
		
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("MAP","onResume called");
		if(map == null)
		{
			Log.d("MAP","map null");
		}
		else
		{
			map.setIndoorEnabled(true);
			Log.d("MAP","indoor : "+map.isIndoorEnabled());
		}
	}


	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("MAP","restart called");
		if(map == null)
		{
			Log.d("MAP","map null");
		}
	}

	// constants for use in calling the web service
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc";
	private final String SOAP_ACTION = "http://tempuri.org/IMediationService/GetPlan";
	private final String UPDATE_SOAP_ACTION = "http://tempuri.org/IMediationService/UpdateLocation";
	private final String METHOD_NAME = "GetPlan";
	private final String UPDATE_METHOD_NAME = "UpdateLocation";
	
	private String TAG = "NCSU";	//for logging
	
	boolean UpdateContent = false;
	
	private static String location;
	private static LatLng CurrentCoords;
	private static String destination;
	private static String path;
	private static String NoRouteString = "GetPlanResponse{GetPlanResult=anyType{}; }";
	private MapFragment map1;
	
	Button b;
	TextView tv;
	EditText et;
	FrameLayout f;
	Button roomSearch;
	ActionBar actionBar;
    
	static final LatLng CENTER = new LatLng(35.769301, -78.676406);	//library center on map
	LatLng lines[] = new LatLng[100];
	private GoogleMap map;
	private MainApplication application;
	private DrawerLayout mDrawerLayout;	// for sliding drawer of floor maps
	private ListView mDrawerList;	//list of floors
	ArrayAdapter<String> adapter;
    private String[] drawerListViewItems; //items in list of floor
	IndoorBuilding building;
    
	ArrayList<String> pointsList;
	HashMap<String, LatLng> places = new HashMap<String,LatLng>();	//hashmap for places and coords
	String[] result;
	 
    // url to get all existing points list
    private static String url_points = "http://people.engr.ncsu.edu/mvbecvar/gen_json_for_android.php";
    
    // JSON Node names
    private static final String TAG_POINTS = "points";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    private static final String TAG_LOC = "loc";
	protected static final String GET_ROOMS_METHOD_NAME = "GetMatchingRooms";
  
    // contacts JSONArray
    JSONArray points = null;

    @Override
    protected  void onStart(){
        super.onStart();
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startMonitoring(REGION_ART_WALL);
					beaconManager.startMonitoring(REGION_THEATER);
					beaconManager.startMonitoring(REGION_GAMELAB);
				} catch (RemoteException e) {
					Log.d(TAG, "Error while starting monitoring");
				}
			}
		});
		//AsyncContentPOST contentTask = new AsyncContentPOST(MainActivity.this);
		//contentTask.execute();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //beaconManager = new BeaconManager(getApplicationContext());
        /*beaconManager.setRangingListener(new BeaconManager.RangingListener(){
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(TAG, "Ranged beacons: " + beacons);
            }
        });*/
        beaconManager = new BeaconManager(this);
        beaconManager.setBackgroundScanPeriod(500, 500);
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
				Toast.makeText(getApplicationContext(), "Entered region: "+region.getIdentifier(), Toast.LENGTH_SHORT).show();
                if(region.getIdentifier() == "artwall") {
                    double lat = 35.769446745703576;
                    double lng = -78.67625534534454;
                    String loc = "entrancehall";
                    LatLng coordinate = new LatLng(lat, lng);
                    updateLocation(coordinate, loc);
                    getCurrentloc(coordinate, loc);
                    application.inIbeaconRange = true;
                }
                else if (region.getIdentifier() == "theater") {
                    double lat = 35.76934935715676;
                    double lng = -78.67633447051048;
                    String loc = "theater";
                    LatLng coordinate = new LatLng(lat, lng);
                    updateLocation(coordinate, loc);
                    getCurrentloc(coordinate, loc);
                    application.inIbeaconRange = true;
                }
				else if (region.getIdentifier() == "gamelab") {
					double lat = 35.769367311478824;
					double lng = -78.67638912051916;
					String loc = "gamelab";
					LatLng coordinate = new LatLng(lat, lng);
					updateLocation(coordinate, loc);
					getCurrentloc(coordinate, loc);
					application.inIbeaconRange = true;
				}
                // Execute some code after 2 seconds have passed
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        application.inIbeaconRange = false;
                    }
                }, 2000);
            }

            @Override
            public void onExitedRegion(Region region) {
                Toast.makeText(getApplicationContext(), "Exited region: "+region.getIdentifier(), Toast.LENGTH_SHORT).show();
                //application.inIbeaconRange = false;
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(REGION_ART_WALL);
                    beaconManager.startRanging(REGION_THEATER);
                    Log.d(TAG, "Start Ranging");
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
		
		Log.d("MAP","onCreate called");
		if(map == null)
		{
			Log.d("MAP","map null");
		}
		
		MapsInitializer.initialize(this);
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
	        @Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
	        	
	        	android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
	            
	        	// Suggest tab -- search room based on criteria
	        	if(tab.getText().equals("Suggest"))
	        	{
	            // show the given tab
	        	//Toast.makeText(MainActivity.this, tab.getText(), Toast.LENGTH_SHORT).show();
	        	QuestionFragment fh = new QuestionFragment();	// fragment for auggest tab
	        	Log.d("TAB",fh.getId()+"");
	        	
	        	//replace existing tab (Map) with Suggest tab
	        	android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	            fragmentTransaction.add(R.id.parent, fh);
	            fragmentTransaction.commit();
	            
	            // Hide unnecessary elements
	            f.setVisibility(android.view.View.INVISIBLE);
	            et.setVisibility(android.view.View.INVISIBLE);
	            b.setVisibility(android.view.View.INVISIBLE);
	            
	        	}	         
	        	else	// Map tab is clicked
	        	{
	        		if(f!=null)	// if the suggest tab is present on the screen
	        		{
	        			Log.d(TAG,"Map clicked");
	        			// show necessary elements of screen
	        			f.setVisibility(android.view.View.VISIBLE);
	        			et.setVisibility(android.view.View.VISIBLE);
	        			b.setVisibility(android.view.View.VISIBLE);
	        			
	        			//replace fragment
	        			android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	        			android.support.v4.app.Fragment fRemove = fragmentManager.findFragmentById(R.id.parent);
	        			/*if(fRemove == null)
	        			{
	        				Log.d("FRAG", "null");
	        			}*/
	        			fragmentTransaction.detach(fRemove);
	        			fragmentTransaction.commit();
	        			
	        		}
	        	}
	        }

	        @Override
			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
	            // hide the given tab
	        	// leave as it is as no clean up required when the tab is unselected
	        }

	        @Override
			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
	            // ignore this event for same reason as above
	        }

	    };
	    
	    // Add a Map and Suggest tab on the screen and add listeners for their clicks
	    actionBar.addTab(
                actionBar.newTab()
                        .setText("Map")
                        .setTag("Map")
                        .setTabListener(tabListener));

	    actionBar.addTab(
                actionBar.newTab()
                        .setText("Suggest")
                        .setTag("Suggest")
                        .setTabListener(tabListener));
	    
	    // set layout of screen as activity_main
		setContentView(R.layout.activity_main);
		
		pointsList = new ArrayList<String>();
		
		// get instance of a google map
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		// Move the camera instantly to the center with a zoom of 20.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 20));
		
		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
		
		application = (MainApplication) MainActivity.this.getApplication();
        application.mainActivity = this;
        
        // Drawer layout for floor selection
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setBackgroundColor(00110000);
	    mDrawerList = (ListView) findViewById(R.id.left_drawer);
	    mDrawerList.setBackgroundColor(11110000);

	    // get floors 
	    drawerListViewItems = getResources().getStringArray(R.array.items);
	     	
	    // Set the adapter for the drawer layout list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(MainActivity.this,R.layout.drawer_listview_item,drawerListViewItems));
        
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);

        //destination Edit Control
      		et = (EditText) findViewById(R.id.roomname);
      		f = (FrameLayout) findViewById(R.id.frameLayout);
      		
      		et.setOnEditorActionListener(new OnEditorActionListener() {
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEARCH) 
					{
						Log.d("SEARCH",v.getText().toString());						
						b.performClick();
					}
					return true;
				}
			});
      	building = map.getFocusedBuilding();
      	
      	//Button to trigger web service invocation
    		b = (Button) findViewById(R.id.button1);
    		
    		b.setOnClickListener(new OnClickListener() {
    			@Override
				public void onClick(View v) {  				
    				map.clear();
    				
    				// show current location on map
    				Marker marker = map.addMarker(new MarkerOptions()
    				.position(CurrentCoords)
    				.title(location));
    				marker.showInfoWindow();
    				
//    				building = map.getFocusedBuilding();

    				/*if (building == null) {
    				 // return null;
    					Log.d("Indoor","NULL");
    				}*/
    				Log.d("button",et.getText().toString());
    				//Check if location & destination is not empty
    				if (et.getText().toString().trim().length() != 0 && et.getText().toString().trim() != "" && location.length() != 0 && location.toString() != "") 
    				{
    					Log.d("button","inside: "+et.getText().toString().trim()+" "+et.getText().toString().trim().length());
    					//Get the destination value
    					destination = et.getText().toString().trim();
    					
    					//if destination is same as current location, display an Alert 
    					//saying destination reached
    					if(destination.equalsIgnoreCase(location))
    					{
    						et.setText("");
    						new AlertDialog.Builder(MainActivity.this)
    					    .setTitle("Destination Reached")
    					    .setMessage("You have reached your destination")
    					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    					        @Override
								public void onClick(DialogInterface dialog, int which) { 
    					        	result = null;
    					        	dialog.cancel();
    					        	return;
    					        }
    					     })
    					    .setIcon(android.R.drawable.checkbox_on_background)
    					     .show();
    					}
    					else	//otherwise get route from current location to destination
    					{
    						UpdateContent = true;
    						Toast.makeText(MainActivity.this, "Calling AsyncCall", Toast.LENGTH_SHORT).show();
	    					AsyncCallWS task = new AsyncCallWS(MainActivity.this);
	    					task.execute();
    					}
    					
    					//hide soft keyboard
    					InputMethodManager imm = (InputMethodManager)getSystemService(
    						      Context.INPUT_METHOD_SERVICE);
    						imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    				} 
    				else 
    				{
    					//et.setText("Please enter location");
    					et.setText("");
    					et.setHint("Please enter location");
    					result = null;
    				}
    			}
    		});
    		
    		
    		// check if intent is called from displayRoomsActivity, if it is,
    		// then search for route to the clicked location
    		Intent callingIntent = getIntent();
    		if(callingIntent.getBooleanExtra("calledFromDisplayRooms", false))
    		{
    			String dest = callingIntent.getStringExtra("destination");
    			Toast.makeText(getApplicationContext(), dest, Toast.LENGTH_SHORT).show();
    			Log.d("ListOfRooms",dest);
    			//building = map.getFocusedBuilding();
    			map.setIndoorEnabled(true);
    			//actionBar.getTabAt(0).select();
    			//actionBar.setSelectedNavigationItem(0);
    			if(map == null)
    			{
    				Log.d("MAP","map null");
    			}
    			Log.d("MAP","map isIndoorEnabled "+map.isIndoorEnabled());
    		
    			map.setIndoorEnabled(true);
    			Log.d("MAP","map isIndoorEnabled after change "+map.isIndoorEnabled());
    			et.setText(dest.trim());
    			map.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 20));
    			map.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
    			
    			Log.d("MAP","current zoom levele is "+map.getCameraPosition().zoom);
    			Log.d("MAP","max level is "+map.getMaxZoomLevel());
    			//IndoorBuilding bldg = map.getFocusedBuilding();
    			/*IndoorLevel il1 = bldg.getLevels().get(1) ;
    			il1.activate();*/
    			//mDrawerLayout.callOnClick();
    			b.performClick();
    			
    			
    		}
        
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId())
		{
		case R.id.content:
			sendContent();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}


	private void sendContent() {
		Toast.makeText(getApplicationContext(), "Content", Toast.LENGTH_SHORT).show();
		
		// Show dialog box with drop down
		contentDialogFragment t = new contentDialogFragment();
		t.show(getFragmentManager(), "TAGS");
		
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*
	 * method updateLocation
	 * 
	 * updates the current location coordinates to cor and name of location to loc
	 * 
	 * @param cor - coordinates in LatLng format
	 * @param loc - name of location
	 */
	public void updateLocation(LatLng cor, String loc){
		boolean isSame = false;
		if(cor.equals(CurrentCoords))
		{
			isSame = true;
		}
		if (!loc.equals(location) && UpdateContent)
		{
			//call update location 
			Toast.makeText(MainActivity.this, "Calling update location", Toast.LENGTH_SHORT).show();
			AsyncUpdate Updatetask = new AsyncUpdate(MainActivity.this);
			Updatetask.execute();
		}
		CurrentCoords = cor;
		location = loc;
		
		//clear map
		map.clear();
		
		//update current location on map
		Marker marker = map.addMarker(new MarkerOptions()
		.position(cor)
		.title(loc));
		marker.showInfoWindow();
		
		
		// plot current route, if any
		plotLines(MainActivity.this.result);
		
	}
	
	/**
	 * method getCurrentloc
	 * 
	 * get the current locations coordinates and name
	 * 
	 * @param cor coordinate in LatLng format
	 * @param loc name of location
	 * 
	 */
	public void getCurrentloc(LatLng cor, String loc){
		location = loc;
		CurrentCoords = cor;
	}
	
	public void updateStatus(String s){
		Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
	}
	
    public void updatePath(String location) {
		
//		Toast.makeText(MainActivity.this, "getPath Called", Toast.LENGTH_SHORT).show();		//Create request
		SoapObject request = new SoapObject(NAMESPACE, UPDATE_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo locationPI = new PropertyInfo();
		//Set Name
		locationPI.setName("source");
		//Set Value
		locationPI.setValue(location);
		//Set dataType
		locationPI.setType(double.class);
		//Add the property to request object
		request.addProperty(locationPI);
		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);
		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL,80000);
		androidHttpTransport.debug = true;
		//androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        //Toast.makeText(getApplicationContext(), "soap action0", Toast.LENGTH_SHORT).show();

		try {
			//Invoke web service
			androidHttpTransport.call(UPDATE_SOAP_ACTION, envelope);
			//Get the response, including Fault if any
			if (envelope.bodyIn instanceof SoapFault)
			{
			    final SoapFault sf = (SoapFault) envelope.bodyIn;
			    System.out.println(sf.faultstring);
			}
			SoapObject resp = (SoapObject) envelope.bodyIn;
			//Assign it to path1 variable
			String path1 = resp.toString();
			Log.d("UPDATE",path1);
            //Toast.makeText(getApplicationContext(), "soap action", Toast.LENGTH_SHORT).show();
			// if no route found, set result to null and return
			if(path1.equalsIgnoreCase(NoRouteString))
			{
//				result = null;
				return;				
			}
			else
			{
				//extract path from the response
				int start = path1.indexOf('=');
				int end = path1.indexOf(';');
				String path = path1.substring(start+1, end);
				result = path.split("\\r?\\n");
				Log.d("UPDATE",path);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @method getPath
	 * 
	 * get the route from source to destination and store it in result
	 *   
	 * @param location current location / source
	 * @param destination destination
	 * 
	 */
	public void getPath(String location, String destination) {
		
//		Toast.makeText(MainActivity.this, "getPath Called", Toast.LENGTH_SHORT).show();		//Create request
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo locationPI = new PropertyInfo();
		PropertyInfo destinationPI = new PropertyInfo();
		//Set Name
		locationPI.setName("source");
		destinationPI.setName("destination");
		//Set Value
		locationPI.setValue(location);
		destinationPI.setValue(destination);
		//Set dataType
		locationPI.setType(double.class);
		destinationPI.setType(double.class);
		//Add the property to request object
		request.addProperty(locationPI);
		request.addProperty(destinationPI);
		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);
		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL,80000);
		androidHttpTransport.debug = true;
		//androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		try {
			//Invoke web service
			androidHttpTransport.call(SOAP_ACTION, envelope);
			//Get the response, including Fault if any
			if (envelope.bodyIn instanceof SoapFault)
			{
			    final SoapFault sf = (SoapFault) envelope.bodyIn;
			    System.out.println(sf.faultstring);
			}
			SoapObject resp = (SoapObject) envelope.bodyIn;
			//Assign it to path1 variable
			String path1 = resp.toString();
			
			// if no route found, set result to null and return
			if(path1.equalsIgnoreCase(NoRouteString))
			{
				result = null;
				return;				
			}
			else
			{
				//extract path from the response
				int start = path1.indexOf('=');
				int end = path1.indexOf(';');
				String path = path1.substring(start+1, end);
				result = path.split("\\r?\\n");
				Log.d(TAG,path);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     * @method plotLines
     * 
     * plots the route on the map
     * 
     * @param results  array of the points on the route to be taken
     */
    private void plotLines(String[] results)
    {
    	
    	results = result;
    	// if we have stored the existing places and route found is not empty
    	if(!places.isEmpty() && results!=null) {
    		String temp[];
        	for(int i=0; i<result.length ; i++) {
        		temp = result[i].split(" ");
        		
        		// needed because the web service sometimes gives 'move name' instead of 'name'
        		if(temp.length!=2) {
        			//System.out.println(result[i]);
        			results[i] = temp[0];
        		}
        		else {
        			results[i] = temp[1];
        		}
        	}
    		
			if(places.get(location) == null || results[0]==null)
				return;

			try {
			// add line on map from current location to first point
			map.addPolyline(new PolylineOptions()
			.add(places.get(location), places.get(results[0]))
			.width(5)
			.color(Color.RED));

			//add line on map from point to subsequent point
			for(int i = 0; i< results.length - 1; i++) {
				LatLng t = places.get(results[i]);
				map.addPolyline(new PolylineOptions()
				.add(places.get(results[i]), places.get(results[i + 1]))
				.width(5)
				.color(Color.RED));

				map.addMarker(new MarkerOptions()
				.position(places.get(results[i]))
				.title(results[i])
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
			}

			map.addMarker(new MarkerOptions()
			.position(places.get(results[results.length - 1]))
			.title(results[results.length - 1])
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

			}
			catch(Exception e) {
				return;
			}
		}
    }

    /**
     * 
     * @author ronak
     *  AsyncCallWS
     *  
     *  Async call to web service to get the route from current location to destination
     *  
     */
	private class AsyncCallWS extends AsyncTask<String, Void, Void> {
		private ProgressDialog dialog;
		private Context ctx;
		
		public AsyncCallWS(Context c) {
			dialog = new ProgressDialog(c);
			ctx = c;
		}
		
		@Override
	    protected void onPreExecute() {
	        dialog.setMessage("Calculating Route, please wait.");
	        dialog.show();
	    }
		
		@Override
		protected Void doInBackground(String... params) {
			Log.i(TAG, "doInBackground");
			
			getPath(location, destination);	// get the route
			return null;
		}

		@Override
		protected void onPostExecute(Void res) {
			Log.i(TAG, "onPostExecute");
			//Toast.makeText(ctx, "after getPath", Toast.LENGTH_SHORT).show();		//Create request
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
			
			//if no route found, display appropriate alert
			if(result == null)
			{
			new AlertDialog.Builder(MainActivity.this)
		    .setTitle("No Route found")
		    .setMessage("Sorry, We were not able to find a route for this location")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        	dialog.cancel();
		        	return;
		        }
		     })
		    .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
		     .show();
			
			}
			// show the points on map
			showPoints(getApplicationContext(),result);	
		}

	}
	
	/**
     * 
     * @author ronak
     *  AsyncCallWS
     *  
     *  
     */
	private class AsyncUpdate extends AsyncTask<String, Void, Void> {

		private Context ctx;

		public AsyncUpdate(Context c) {
			ctx = c;
		}

		@Override
		protected Void doInBackground(String... params) {
			Log.i(TAG, "doInBackground");

			updatePath(location);    // get the route
			return null;
		}

		@Override
		protected void onPostExecute(Void res) {
			Log.i(TAG, "onPostExecute");
			//Toast.makeText(ctx, "after updatePath", Toast.LENGTH_SHORT).show();		//Create request

			//if no route found, display appropriate alert
			// show the points on map
			showPoints(getApplicationContext(),result);
		}

	}
/*
	private class AsyncContentPOST extends AsyncTask<String, Void, String> {
		private Context ctx;

		public AsyncContentPOST(Context c) {
			ctx = c;
		}
		@Override
		protected String doInBackground(String... params) {
			Log.i(TAG, "doInBackground");
			String use = "artw";
			String name = "faheem";
			String password = "f@h33m";
			String authString = name + ":" + password;
			String authStringEnc = Base64.encodeToString((authString).getBytes(),Base64.NO_WRAP);
			String content="";
			String input="";
			try {
				URL url = new URL("https://alice.lib.ncsu.edu/api/v1/action/");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestProperty("Authorization", "Basic "+authStringEnc);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				JSONObject obj = new JSONObject();
				JSONObject request = new JSONObject();

				try {
					obj.put("user_id", "faheem");
					obj.put("device_id", "immw");
					request.put("content", "http://www.ces.ncus.edu/plymouth/ent/pics/ncsu_logo_1.gif");
					request.put("hint", "fullscreen");
					obj.put("request_json", request);

				} catch (JSONException e) {
					Log.e("MainActivity", "unexpected JSON exception", e);
				}

				OutputStream os = conn.getOutputStream();
				os.write(obj.toString().getBytes());
				os.close();
				return "success";
			}
			catch (UnsupportedEncodingException e){
				e.printStackTrace();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			return "fail";
		}
		@Override
		protected void onPostExecute(String success) {
			Log.i(TAG, "onPostExecute");
			Toast.makeText(ctx, "after POST " + success, Toast.LENGTH_SHORT).show();
		}

	}
	*/
	/**
	 * method showPoints
	 * 
	 * 
	 * @param c
	 * @param result
	 */
	public void showPoints(final Context c, String[] result){
		new PostJSONDataAsyncTask(c, null, url_points, false){
			@Override
			protected void onPostExecute(String response)
			{
				application = (MainApplication) MainActivity.this.getApplication();
				super.onPostExecute(response);
				
				if (response != null)
				{
					Log.d("wifiloc", response);
					JSONObject json = null;
					try{
						json = new JSONObject(response);
					} catch (JSONException e){
						e.printStackTrace();
						Log.d("wifiloc", "Error parsing JSON");
					}

					if(json == null){
						Log.d("wifiloc", "Error parsing server response");
						return;
					}

					if(json.length() > 0){
						try {
							// Getting Array of existing points
							points = json.getJSONArray(TAG_POINTS);

							System.out.println(points);
							// looping through All points
							for(int i = 0; i < points.length(); i++){
								JSONObject c = points.getJSONObject(i);

								// Storing each json item in variable
								double lat = c.getDouble(TAG_LAT);
								double lng = c.getDouble(TAG_LNG);
								String loc = c.getString(TAG_LOC);

								// adding each coordinate to ArrayList
								LatLng temp = new LatLng(lat, lng);
								places.put(loc, temp);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else {
						//TODO Do something here if no teams have been made yet
					}

					Log.d("wifiloc", "Update Success");

					for(int i = 0; i < pointsList.size(); i++){
						map.addMarker(new MarkerOptions()
						.position(places.get(pointsList.get(i)))
						.title(""+pointsList.get(i)));
					}
					
					plotLines(MainActivity.this.result);
				}
				else
				{
					Log.d("wifiloc", "Error Connecting to Server");
				}

			}
							}.execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		IndoorLevel il = building.getLevels().get(position);
		il.activate();
		mDrawerLayout.closeDrawers();
		
	}
/*
	public static void POST(String use) {
		String name = "faheem";
		String password = "f@h33m";
		String authString = name + ":" + password;
		byte[] authEncBytes = Base64.encode(authString.getBytes(), Base64.DEFAULT);
		String authStringEnc = new String(authEncBytes);
		String content="";
		String input="";
		try {
			URL url = new URL("https://alice.lib.ncsu.edu");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			content="\"http://www.ces.ncsu.edu/plymouth/ent/pics/ncsu_logo_1.gif\"";
			input = "{\"user_id\":\"faheem\",\"device_id\":\""+use+"\",\"request_json\":{\"content\":"+content+",\"hint\":\"fullscreen\"}}";


			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			/*if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED){
				//throw exception
			}*/
			/*
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			String output;*/
	/*
			conn.disconnect();
		}
		catch (IOException e){
			//Toast.makeText(this, getApplicationContext(), "Comm failed", Toast.LENGTH_SHORT).show();

		}
	}*/
}
