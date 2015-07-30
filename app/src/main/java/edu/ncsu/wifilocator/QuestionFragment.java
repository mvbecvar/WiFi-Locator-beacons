/**
 * QuestionFragment
 * 
 * This is the fragment that deals with the 'Suggest' tab. this class calls the web service 
 * to search for a room based on parameters like noise, light , sound and room type. It shows a 
 * list of the rooms returned by the web service. when the user clicks on any item of the list
 * it searches for the route to that room
 * 
 */

//TODO: yet to implement the display of the rooms returned and search it on the map
package edu.ncsu.wifilocator;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class QuestionFragment extends Fragment{


	View view;
	EditText tmpLo;
	EditText tmpHi;
	RadioGroup lightGroup;
	RadioGroup soundGroup;
	RadioGroup roomGroup;
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
		 view = inflater.inflate(R.layout.questions_fragment, container, false);
		 
		 tmpLo = (EditText) view.findViewById(R.id.tempLo);
		 tmpHi = (EditText) view.findViewById(R.id.tempHi);
		 lightGroup = (RadioGroup) view.findViewById(R.id.lightGroup);
		 soundGroup = (RadioGroup) view.findViewById(R.id.soundGroup);
		 roomGroup = (RadioGroup) view.findViewById(R.id.roomGroup);
		 
		 Button sButton = (Button)view.findViewById(R.id.searchButton);
		 
		 sButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("FRAGMENT", "Hey I was clicked");
				
				String tempLow = tmpLo.getText().toString();
				if(tempLow.trim().length() == 0)
					tempLow = "0";
				
				String tempHigh = tmpHi.getText().toString();
				if(tempHigh.trim().length() == 0)
					tempHigh = "100";
				
				String lightLow = "0";
				String lightHigh = "100";
				if(lightGroup.getCheckedRadioButtonId() == R.id.lightDim)
				{
					lightHigh = "50";
				}
				if(lightGroup.getCheckedRadioButtonId() == R.id.lightBright)
				{
					lightLow = "50";
				}
				
				String soundLow = "0";
				String soundHigh = "100";
				if(soundGroup.getCheckedRadioButtonId() == R.id.soundLow)
				{
					soundHigh = "50";
				}
				if(soundGroup.getCheckedRadioButtonId() == R.id.soundHigh)
				{
					soundLow = "50";
				}
				
				String roomType = "Group Study Room";
				
				new roomsAsync(getActivity(),lightLow, lightHigh, soundLow,
								soundHigh, tempLow, tempHigh, roomType).execute();
			}
		});
	     return view;
	    }
	 
	 
}
/**
 * roomsAsync
 * 
 * Async class to get the list of rooms matching the criteria gievn by the user
 * 
 */
class roomsAsync extends AsyncTask<String,Void,String>
{
	private ProgressDialog dialog;
	Context c;
	int tempLow;
	int tempHigh;
	String lightLow;
	String lightHigh;
	String soundLow;
	String soundHigh;
	String roomType;
	roomsAsync(Context ctx, String ll, String lh, String sl, String sh, String tl, String th, String rtype)
	{
		c = ctx;
		lightLow = ll;
		lightHigh = lh;
		soundLow = sl;
		soundHigh = sh;
		tempLow = Integer.parseInt(tl);
		tempHigh = Integer.parseInt(th);
		roomType = rtype;
		
		Log.d("val",lightLow+" "+lightHigh+" "+soundLow+" "+soundHigh+" "+tempLow+" "+
					tempHigh+" "+roomType);
		dialog = new ProgressDialog(c);
	}
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc";
	private final String SOAP_ACTION = "http://tempuri.org/IMediationService/GetMatchingRooms";
	private final String GET_ROOMS_METHOD_NAME = "GetMatchingRooms";
	
	
	@Override
    protected void onPreExecute() {
        dialog.setMessage("Finding list of places, please wait.");
        dialog.show();
    }
	
	@Override
	protected String doInBackground(String... params) {
		String res = getRooms();
		return res;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		if (dialog.isShowing()) {
            dialog.dismiss();
        }
		
		Log.d("OnPostExe",result);
//		EditText etn = QuestionFragment.getActivity().findViewById(R.id.roomname);
		//c.actionBar.setSelectedNavigationItem(0);
		Intent i = new Intent(c,DisplayRoomsActivity.class);
		i.putExtra("rooms", result);
		c.startActivity(i);
		/*Toast.makeText(,"", Toast.LENGTH_LONG).show();
		Toast.*/
	}
	
	/**
	 * 
	 * method getRooms
	 * 
	 * calls the web service to get the list of rooms matching the criteria
	 * 
	 * @return String of rooms separated by :
	 */
	public String getRooms() {
		
		SoapObject request = new SoapObject(NAMESPACE, GET_ROOMS_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo lightHi = new PropertyInfo();
		lightHi.setType(String.class);
		lightHi.setName("lightHi");
		lightHi.setValue(lightHigh);
		
		PropertyInfo lightLo = new PropertyInfo();
		lightLo.setType(String.class);
		lightLo.setName("lightLo");
		lightLo.setValue(lightLow);
		
		PropertyInfo soundHi = new PropertyInfo();
		soundHi.setType(String.class);
		soundHi.setName("soundHi");
		soundHi.setValue(soundHigh);
		
		PropertyInfo soundLo = new PropertyInfo();
		soundLo.setType(String.class);
		soundLo.setName("soundLo");
		soundLo.setValue(soundLow);
		
		PropertyInfo tempHi = new PropertyInfo();
		tempHi.setType(int.class);
		tempHi.setName("tempHi");
		tempHi.setValue(tempHigh);
		
		PropertyInfo tempLo = new PropertyInfo();
		tempLo.setType(int.class);
		tempLo.setName("tempLo");
		tempLo.setValue(tempLow);
		
		PropertyInfo type = new PropertyInfo();
		type.setType(String.class);
		type.setName("type");
		type.setValue("group_study_room");
		
		request.addProperty(lightHi);
		request.addProperty(lightLo);
		request.addProperty(soundHi);
		request.addProperty(soundLo);
		request.addProperty(tempHi);
		request.addProperty(tempLo);
		request.addProperty(type);
		
		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);
		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		androidHttpTransport.debug = true;
		androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		try {
			//Invoke web service
			androidHttpTransport.call(SOAP_ACTION, envelope);
			String xml = androidHttpTransport.responseDump;
			if (envelope.bodyIn instanceof SoapFault)
			{
			    final SoapFault sf = (SoapFault) envelope.bodyIn;
			    System.out.println(sf.faultstring);
			}
			//Get the response
			SoapObject resp = (SoapObject) envelope.bodyIn;
			//Assign it to path static variable
			String path1 = resp.toString();
			Log.d("ROOMS",path1);
			//System.out.println(path1);
			
			return path1;
			
			}
			

		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
		
	}
	 
}