/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.fragments;

import java.util.ArrayList;
import java.util.List;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.activities.ChooseLocationActivity;
import gov.in.bloomington.georeporter.activities.MainActivity;
import gov.in.bloomington.georeporter.dialogs.DatePickerDialogFragment;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.tasks.ReverseGeocodingTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.maps.GeoPoint;

public class ReportFragment extends SherlockFragment {
	public static final int CHOOSE_LOCATION_REQUEST = 1;
	
	private JSONObject          mService;
	private List<NameValuePair> mReport;
	
	private EditText mLocationView;
	
	/**
	 * Initialize the report with a service
	 * 
	 * This should be called before adding this fragment to the stack
	 * Since fragments cannot have constructors, you must call
	 * this function immediately after instantiating this fragment.
	 * 
	 * @param service
	 * void
	 */
	public void setService(JSONObject service) {
		mService = service;
		mReport  = new ArrayList<NameValuePair>();
		mReport.add(new BasicNameValuePair(Open311.SERVICE_CODE, service.optString(Open311.SERVICE_CODE)));
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("service", mService.toString());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			try {
				JSONObject s = new JSONObject(savedInstanceState.getString("service"));
				setService(s);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		View v = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_report, container, false);
		mLocationView = (EditText) v.findViewById(R.id.address_string);
		
		// Register onClick handlers for all the clickables in the layout
		v.findViewById(R.id.mapChooserButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ChooseLocationActivity.class);
				startActivityForResult(i, CHOOSE_LOCATION_REQUEST);
			}
		});
		v.findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		v.findViewById(R.id.button_submit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submit(v);
			}
		});
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TextView service_description = (TextView) getView().findViewById(R.id.service_description);
		service_description.setText(mService.optString(Open311.DESCRIPTION));
		
		// Inflate all the views for the service attributes
		if (mService.optBoolean(Open311.METADATA)) {
			LinearLayout layout     = (LinearLayout) getView().findViewById(R.id.attributes);
			JSONObject   definition = Open311.sServiceDefinitions.get(mService.opt(Open311.SERVICE_CODE));
			JSONArray    attributes = definition.optJSONArray(Open311.ATTRIBUTES);
			
			int len = attributes.length();
			for (int i=0; i<len; i++) {
				JSONObject a = attributes.optJSONObject(i);
				
				View v = loadViewForAttribute(a, savedInstanceState);
				if (v != null) {
					String description = a.optString(Open311.DESCRIPTION);
					TextView label = (TextView) v.findViewById(R.id.label);
					label.setText(description);
					
					layout.addView(v);
				}
			}
		}
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
				mLocationView.setText(String.format("%s, %s", latitude, longitude));
				
				mReport.add(new BasicNameValuePair(Open311.LATITUDE,  latitude));
				mReport.add(new BasicNameValuePair(Open311.LONGITUDE, longitude));
				
				new ReverseGeocodingTask(getActivity(), mLocationView).execute(new GeoPoint(latitudeE6, longitudeE6));
			}
		}
	}
	
	/**
	 * Inflates the appropriate view for each datatype
	 * 
	 * @param attribute
	 * @param savedInstanceState
	 * @return
	 * View
	 */
	private View loadViewForAttribute(JSONObject attribute, Bundle savedInstanceState) {
		LayoutInflater inflater = getLayoutInflater(savedInstanceState);
		String         datatype = attribute.optString(Open311.DATATYPE, Open311.STRING);

		if (datatype.equals(Open311.STRING) || datatype.equals(Open311.NUMBER) || datatype.equals(Open311.TEXT)) {
			View v = inflater.inflate(R.layout.list_item_report_attributes_string, null);
			EditText input = (EditText) v.findViewById(R.id.input);
			
			if (datatype.equals(Open311.NUMBER)) {
				input.setInputType(InputType.TYPE_CLASS_NUMBER);
			}
			if (datatype.equals(Open311.TEXT)) {
				input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			}
			return v;
		}
		else if (datatype.equals(Open311.DATETIME)) {
			View v = inflater.inflate(R.layout.list_item_report_attributes_datetime, null);
			TextView input = (TextView) v.findViewById(R.id.input);
			input.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SherlockDialogFragment picker = new DatePickerDialogFragment(v);
					picker.show(getActivity().getSupportFragmentManager(), "datePicker");
				}
			});
			return v;
		}
		else if (datatype.equals(Open311.SINGLEVALUELIST) || datatype.equals(Open311.MULTIVALUELIST)) {
			JSONArray values = attribute.optJSONArray(Open311.VALUES);
			int len = values.length();
			
			if (datatype.equals(Open311.SINGLEVALUELIST)) {
				View v = inflater.inflate(R.layout.list_item_report_attributes_singlevaluelist, null);
				RadioGroup input = (RadioGroup) v.findViewById(R.id.input);
				for (int i=0; i<len; i++) {
					JSONObject value = values.optJSONObject(i);
					RadioButton button = (RadioButton) inflater.inflate(R.layout.radiobutton, null);
					button.setText(value.optString(Open311.KEY));
					input.addView(button);
				}
				return v;
			}
			else if (datatype.equals(Open311.MULTIVALUELIST)) {
				View v = inflater.inflate(R.layout.list_item_report_attributes_multivaluelist, null);
				LinearLayout input = (LinearLayout) v.findViewById(R.id.input);
				for (int i=0; i<len; i++) {
					JSONObject value = values.optJSONObject(i);
					CheckBox checkbox = (CheckBox) inflater.inflate(R.layout.checkbox, null);
					checkbox.setText(value.optString(Open311.KEY));
					input.addView(checkbox);
				}
				return v;
			}
		}
		return null;
	}

	/**
	 * Reads in all the values from the ReportFragment view
	 * POST the report to the server
	 * Sends the user to the saved report screen
	 * 
	 * @param v
	 * void
	 */
	public void submit(View v) {
		
	}
}
