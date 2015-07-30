/**
 * PostJSONDataAsyncTask
 * 
 * Async class that calls the web service to get the list of all rooms or to get the 
 * name of the current location
 * 
 */
package edu.ncsu.wifilocator;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class PostJSONDataAsyncTask extends AsyncTask<Object, Void, String>{
private Context context;
public ProgressDialog dialog;
private String string;
private String postURL;
private boolean showProgressDialog;

/**
 * web service to get all points as well as update location
 * 
 * @param context	application context
 * @param string 	if null, get all points
 * 					if not null, update location to server web service
 * @param postURL    // the address to post to
 */
public PostJSONDataAsyncTask(Context context, String string, String postURL, boolean showProgressDialog){
	this.context = context;
	this.string = string;
	this.postURL = postURL;
	this.showProgressDialog = showProgressDialog;
}


@Override
protected String doInBackground(Object... arg){

	try{
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;

		// If this is null, it means we'll  get all points from web service
		if(string == null){
			responseBody = httpclient.execute(new HttpGet(postURL), responseHandler);
		}
		// Execute HTTP POST to update current location to web service
		else {

			HttpPost httppost = new HttpPost(postURL);
			StringEntity tmp = null;
			httppost.setHeader("User-Agent", "Agent_Smith");
			httppost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			tmp = new StringEntity(string, "UTF-8");
			httppost.setEntity(tmp);

			responseBody = httpclient.execute(httppost, responseHandler);
			//Toast.makeText(context, "Location Updated: "+ responseBody.toString(), Toast.LENGTH_SHORT).show();
			Log.d("Test JSON", responseBody.toString());
		}
		Log.d("Test null JSON", responseBody.toString());
		return responseBody;
	}
	catch (Exception e){
		Log.e("PostJSON", e.toString());
			return null;
		}
	}


}
