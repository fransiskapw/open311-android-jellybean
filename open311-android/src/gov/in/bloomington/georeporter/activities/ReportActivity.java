/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import gov.in.bloomington.georeporter.MainActivity;
import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment;
import gov.in.bloomington.georeporter.fragments.ReportFragment;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment.OnGroupSelectedListener;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment.OnServiceSelectedListener;
import gov.in.bloomington.georeporter.models.Open311;

import com.actionbarsherlock.app.ActionBar;
import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ReportActivity extends BaseFragmentActivity implements OnGroupSelectedListener, OnServiceSelectedListener {
	public static final int CHOOSE_LOCATION_REQUEST = 1;
	private ActionBar mActionBar;
	private List<NameValuePair> report;
	
	private ReportFragment mReportFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();
		mActionBar.setTitle(R.string.menu_report);
		
		ChooseGroupFragment chooseGroup = new ChooseGroupFragment();
		getSupportFragmentManager() .beginTransaction()
									.add(android.R.id.content, chooseGroup)
									.addToBackStack(null)
									.commit();
		report = new ArrayList<NameValuePair>();
	}
	
	@Override
	public void onGroupSelected(String group) {
		ChooseServiceFragment chooseService = new ChooseServiceFragment();
		chooseService.setServices(Open311.getServices(group));
		getSupportFragmentManager() .beginTransaction()
									.replace(android.R.id.content, chooseService)
									.addToBackStack(null)
									.commit();
	}
	
	@Override
	public void onServiceSelected(JSONObject service) {
		mActionBar.setTitle(service.optString("service_name"));
		
		mReportFragment = new ReportFragment();
		mReportFragment.setService(service);
		getSupportFragmentManager() .beginTransaction()
									.replace(android.R.id.content, mReportFragment)
									.addToBackStack(null)
									.commit();
	}
	/**
	 * OnClick handler for the Location text view in ReportFragment
	 * 
	 * @param view
	 * void
	 */
	public void openMapChooser(View view) {
		Intent i = new Intent(this, ChooseLocationActivity.class);
		startActivityForResult(i, CHOOSE_LOCATION_REQUEST);
	}
	
	/**
	 * Callback from ChooseLocationActivity
	 * 
	 * Intent data should have latitude and longitude
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CHOOSE_LOCATION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				int latitudeE6  = data.getIntExtra(Open311.LATITUDE,  0);
				int longitudeE6 = data.getIntExtra(Open311.LONGITUDE, 0);
				
				String latitude  = Double.toString(latitudeE6  / 1e6);
				String longitude = Double.toString(longitudeE6 / 1e6);
				// Display the lat/long as text for now
				// It will get replaced with the address when ReverseGeoCodingTask returns
				updateLocationText(String.format("%s, %s", latitude, longitude));
						
				report.add(new BasicNameValuePair(Open311.LATITUDE,  latitude));
				report.add(new BasicNameValuePair(Open311.LONGITUDE, longitude));
				
				new ReverseGeocodingTask(this).execute(new GeoPoint(latitudeE6, longitudeE6));
			}
		}
	}
	
	/**
	 * Updates the location text displayed in fragment_report layout
	 * 
	 * @param s
	 * void
	 */
	private void updateLocationText(String s) {
        TextView v = (TextView)mReportFragment.getView().findViewById(R.id.address_string);
        v.setText(s);
	}
	
	/**
	 * Callback from fragment_report layout
	 * 
	 * Reads in all the values from the ReportFragment view
	 * POST the report to the server
	 * Sends the user to the saved report screen
	 * 
	 * @param v
	 * void
	 */
	public void submit(View v) {
		
	}

	/**
	 * Callback from fragment_report layout
	 * 
	 * @param v
	 * void
	 */
	public void cancel(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * AsyncTask encapsulating the reverse-geocoding API.
	 * 
	 * Updates the view once it has an address
	 */
	private class ReverseGeocodingTask extends AsyncTask<GeoPoint, Void, Void> {
	    Context mContext;

	    public ReverseGeocodingTask(Context context) {
	        super();
	        mContext = context;
	    }

	    @Override
	    protected Void doInBackground(GeoPoint... params) {
	        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
	        GeoPoint point = params[0];
	        double latitude  = point.getLatitudeE6()  / 1e6;
	        double longitude = point.getLongitudeE6() / 1e6;

	        List<Address> addresses = null;
	        try {
	            addresses = geocoder.getFromLocation(latitude, longitude, 1);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        if (addresses != null && addresses.size() > 0) {
	            Address address = addresses.get(0);
	            updateLocationText(String.format("%s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : ""));
	        }
	        return null;
	    }
	}}
