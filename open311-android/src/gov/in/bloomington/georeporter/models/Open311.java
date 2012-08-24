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
import java.util.ArrayList;
import java.util.HashMap;

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
	public static final String SERVICE_CODE = "service_code";
	
	private static String mBaseUrl;
	private static String mJurisdiction;
	private static String mApiKey;
	
	private static JSONArray  mServiceList = null;
	private static ArrayList<String> mGroups;
	private static HashMap<String, JSONObject> mServiceDefinitions;
	
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
		mBaseUrl      = null;
		mJurisdiction = null;
		mApiKey       = null;
		mGroups       = new ArrayList<String>();
		mServiceList  = null;
		mServiceDefinitions = new HashMap<String, JSONObject>();
		
		try {
			mBaseUrl      = current_server.getString(URL);
			mJurisdiction = current_server.optString(JURISDICTION);
			mApiKey       = current_server.optString(API_KEY);
		} catch (JSONException e) {
			return false;
		}
		try {
			mServiceList = new JSONArray(loadStringFromUrl(getServiceListUrl()));
			
			// Go through all the services and pull out the seperate groups
			// Also, while we're running through, load any service_definitions
			String group = "";
			int len = mServiceList.length();
			for (int i=0; i<len; i++) {
				JSONObject s = mServiceList.getJSONObject(i);
				// Add groups to mGroups
				group = s.optString("group");
				if (group != "" && !mGroups.contains(group)) { mGroups.add(group); }
				
				// Add Service Definitions to mServiceDefinitions
				if (s.optString("metadata") == "true") {
					String service_code = s.optString(SERVICE_CODE);
					JSONObject service_definition = getServiceDefinition(service_code);
					if (service_definition != null) {
						mServiceDefinitions.put(service_code, service_definition);
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Returns the services for a given group
	 * 
	 * @param group
	 * @return
	 * ArrayList<JSONObject>
	 */
	public static ArrayList<JSONObject> getServices(String group) {
		ArrayList<JSONObject> services = new ArrayList<JSONObject>();
		int len = mServiceList.length();
		for (int i=0; i<len; i++) {
			try {
				JSONObject s = mServiceList.getJSONObject(i);
				if (s.optString("group") == group) { services.add(s); }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return services;
	}
	
	/**
	 * @param service_code
	 * @return
	 * JSONObject
	 */
	public static JSONObject getServiceDefinition(String service_code) {
		try {
			return new JSONObject(loadStringFromUrl(getServiceDefinitionUrl(service_code)));
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
		return null;
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
	
	/**
	 * @param service_code
	 * @return
	 * String
	 */
	private static String getServiceDefinitionUrl(String service_code) {
		return mBaseUrl + "/services/" + service_code + ".json?" + JURISDICTION + "=" + mJurisdiction;
	}
}
