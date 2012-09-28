/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.models;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Open311 {
	/**
	 * Constants for Open311 keys
	 * 
	 * I'm tired of making typos in key names
	 */
	public static final String JURISDICTION = "jurisdiction_id";
	public static final String API_KEY      = "api_key";
	public static final String SERVICE_CODE = "service_code";
	public static final String DESCRIPTION  = "description";
	public static final String LATITUDE     = "lat";
	public static final String LONGITUDE    = "long";
	public static final String ADDRESS      = "address_string";
	public static final String EMAIL        = "email";
	public static final String DEVICE_ID    = "devide_id";
	public static final String FIRST_NAME   = "first_name";
	public static final String LAST_NAME    = "last_name";
	public static final String PHONE        = "phone";
	public static final String MEDIA        = "media";
	public static final String MEDIA_URL    = "media_url";
	public static final String URL          = "url";
	
	private static Open311 mInstance;
	
	private static String mBaseUrl;
	private static String mJurisdiction;
	private static String mApiKey;
	
	private static JSONArray  mServiceList = null;
	private static HashMap<String, JSONObject> mServiceDefinitions;
	public static ArrayList<String> sGroups;
	
	private static DefaultHttpClient mClient = null;
	private static final int TIMEOUT = 3000;
	
	private Open311() {}
	public static synchronized Open311 getInstance() {
		if (mInstance == null) {
			mInstance = new Open311();
		}
		return mInstance;
	}
	
	/**
	 * Lazy load an Http client
	 * 
	 * @return
	 * DefaultHttpClient
	 */
	private static DefaultHttpClient getClient() {
		if (mClient == null) {
			mClient = new DefaultHttpClient();
			mClient.getParams().setParameter(CoreProtocolPNames  .HTTP_CONTENT_CHARSET, "UTF-8");
			mClient.getParams().setParameter(CoreProtocolPNames  .PROTOCOL_VERSION,     HttpVersion.HTTP_1_1);
			mClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,           TIMEOUT);
			mClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,   TIMEOUT);
		}
		return mClient;
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
		sGroups       = new ArrayList<String>();
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
				if (group != "" && !sGroups.contains(group)) { sGroups.add(group); }
				
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
				if (s.optString("group").equals(group)) { services.add(s); }
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
	 * POST new service request data to the endpoint
	 * 
	 * @param data
	 * @return
	 * JSONObject
	 */
	public static JSONArray postServiceRequest(List<NameValuePair> data) {
		HttpPost post = new HttpPost(mBaseUrl + "/requests.json");
		JSONArray response = new JSONArray();
		try {
			post.setEntity(new UrlEncodedFormEntity(data));
			HttpResponse r = mClient.execute(post);
			response = new JSONArray(EntityUtils.toString(r.getEntity()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Returns the response content from an HTTP request
	 * 
	 * @param url
	 * @return
	 * String
	 */
	private static String loadStringFromUrl(String url) throws ClientProtocolException, IOException, IllegalStateException {
		HttpResponse r = getClient().execute(new HttpGet(url));
		String response = EntityUtils.toString(r.getEntity());
		
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
