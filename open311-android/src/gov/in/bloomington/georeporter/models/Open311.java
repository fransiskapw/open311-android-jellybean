/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Open311 {
	private static Open311 mInstance;
	
	public static final String URL          = "url";
	public static final String JURISDICTION = "jurisdiction_id";
	public static final String API_KEY      = "api_key";
	
	private static JSONObject mEndpoint    = null;
	private static String mBaseUrl;
	private static String mJurisdiction;
	private static String mApiKey;
	
	private static JSONArray  mServiceList = null;
	
	private static DefaultHttpClient mClient = null;
	
	private Open311() {}
	public static synchronized Open311 getInstance() {
		if (mInstance == null) {
			mInstance = new Open311();
		}
		return mInstance;
	}
	
	/**
	 * Loads all the service information from the endpoint
	 * 
	 * Endpoints will have a service_list, plus, for each
	 * service, there may be a service_definition.
	 * To make the user experience smoother, we are downloading
	 * and saving all the possible service information at once.
	 * 
	 * Returns false if there was a problem
	 * 
	 * @param current_server
	 * @return
	 * Boolean
	 */
	public static Boolean setEndpoint(JSONObject current_server) {
		mEndpoint     = current_server;
		mBaseUrl      = null;
		mJurisdiction = null;
		mApiKey       = null;
		mServiceList  = null;
		
		try {
			mBaseUrl      = current_server.getString(URL);
			mJurisdiction = current_server.optString(JURISDICTION);
			mApiKey       = current_server.optString(API_KEY);
		} catch (JSONException e) {
			return false;
		}
		try {
			mServiceList = new JSONArray(loadStringFromUrl(getServiceListUrl()));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Returns the response content from an HTTP request
	 * 
	 * @param url
	 * @return
	 * String
	 */
	private static String loadStringFromUrl(String url) throws ClientProtocolException, IOException, IllegalStateException {
		if (mClient == null) {
			mClient = new DefaultHttpClient();
		}
		HttpResponse r;
		InputStream content;
		String response = "";
		r = mClient.execute(new HttpGet(url));
		content = r.getEntity().getContent();
		
		BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
		String s = "";
		while ((s = buffer.readLine()) != null) {
			response += s;
		}
		
		return response;
	}
	
	
	/**
	 * @return
	 * String
	 */
	private static String getServiceListUrl() {
		return mBaseUrl + "/services.json?" + JURISDICTION + "=" + mJurisdiction;
	}
}
