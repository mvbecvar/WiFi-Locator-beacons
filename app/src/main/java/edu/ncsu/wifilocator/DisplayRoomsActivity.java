/**
 * DisplayRoomsActivity
 * 
 * Activity that displays the list of rooms given by the Suggest tab. on clicking any item, it
 * displays the route to that location on the map
 * 
 */
package edu.ncsu.wifilocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//TODO: implement the rest of this class
public class DisplayRoomsActivity extends Activity {

	TextView tv;
	ListView listOfRooms;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_rooms);
		//tv.setText("Rooms");
		listOfRooms = (ListView) findViewById(R.id.listOfRooms);
		
		/*String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
		        "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
		        "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
		        "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
		        "Android", "iPhone", "WindowsMobile" };*/
		
		Intent callingIntent = getIntent();
		String[] values = getNames(callingIntent.getStringExtra("rooms"));
		
		final ArrayList<String> list = new ArrayList<String>();
	    for (int i = 0; i < values.length; ++i) 
	    {
	    	if(values[i] != null)
	    		list.add(values[i]);
	    }
	    
	    final StableArrayAdapter adapter = new StableArrayAdapter(this,
	    		android.R.layout.simple_list_item_1, list);
	    listOfRooms.setAdapter(adapter);
	    
	    listOfRooms.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				/*Toast.makeText(getApplicationContext(),
						listOfRooms.getItemAtPosition(position).toString(), Toast.LENGTH_LONG)
					      .show();*/
				
				Intent in = new Intent(DisplayRoomsActivity.this,MainActivity.class);
				in.putExtra("calledFromDisplayRooms", true);
				in.putExtra("destination", listOfRooms.getItemAtPosition(position).toString());
				startActivity(in);
				finish();
			}
	    	
		});
		
	}

	private String[] getNames(String names) 
	{
		String rooms[]= new String[100];
		int c = 0;
		int e = names.indexOf("=");
		if (e != -1)
		{
			int prevS = e;
			int s = names.indexOf(";", e);
			while(s != -1)
			{
				rooms[c] = names.substring(prevS+1, s);
				prevS = s;
				s = names.indexOf(";", prevS+1);
				c++;
			}
		}
		return rooms;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_rooms, menu);
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



class StableArrayAdapter extends ArrayAdapter<String> 
{
	HashMap<String, Integer> mNames = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId,
        List<String> objects) 
    {
      super(context, textViewResourceId, objects);
      for (int i = 0; i < objects.size(); ++i) 
      {
        mNames.put(objects.get(i), i);
      }
    }
    
    
    @Override
    public long getItemId(int position) {
      String item = getItem(position);
      return mNames.get(item);
    }
}
