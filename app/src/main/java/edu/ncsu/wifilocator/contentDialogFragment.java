package edu.ncsu.wifilocator;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


public class contentDialogFragment extends DialogFragment {
	String tag;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
				
		final String areas[] = {"Design","Industrial Design","Forestry","Material Science",
				/*"architecture",*/"Sculpture","Ceramics",
				"Education","Religious Studies","Paleobiology","Creative Writing",
				/*"psychology",*/"Film Studies","news"};
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Set the dialog title
	    builder.setTitle("Select Interest")
	    		.setSingleChoiceItems(areas, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d("DIALOG","in single choice item");
						Log.d("DIALOG",areas[which]);
						tag = areas[which];
					}
				})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d("DIALOG","in ok");
						new tagsAsync().execute(tag);
						
					}
				});
	    
	    return builder.create();
	    		
	}

}

class tagsAsync extends AsyncTask<String,Void,Void>
{
	private final String NAMESPACE = "http://tempuri.org/";
//	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc";
	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc?singleWsdl";
	private final String SOAP_ACTION = "http://tempuri.org/IMediationService/SetTags";
	private final String SET_TAG_METHOD_NAME = "SetTags";
	
	@Override
	protected Void doInBackground(String... params) {
		//Log.d("TAG",params[0]);
		setTag(params[0]);
		return null;
		
	}
	
	@Override
	protected void onPostExecute(Void v) {
		Log.d("OnPostExe","onPostExec");
		/*Toast.makeText(,"", Toast.LENGTH_LONG).show();
		Toast.*/
	}
	
	/**
	 * 
	 * method setTag
	 * 
	 * calls the web service to set the tag
	 * 
	 * 
	 */
	public void setTag(String tagName) {
		
		SoapObject request = new SoapObject(NAMESPACE, SET_TAG_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo tag = new PropertyInfo();
		tag.setType(String.class);
		//light.setType(type);
		tag.setName("tag");
		tag.setValue(tagName);
		
		
		request.addProperty(tag);
		
		
		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);
		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		androidHttpTransport.debug = true;
//		androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		try {
			//Invoke web service
			androidHttpTransport.call(SOAP_ACTION, envelope);
			if (envelope.bodyIn instanceof SoapFault)
			{
			    final SoapFault sf = (SoapFault) envelope.bodyIn;
			    System.out.println(sf.faultstring);
			}
			//Get the response
			SoapObject resp = (SoapObject) envelope.bodyIn;
			Log.d("TAG",resp.toString());
			
			}
			

		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	 
}