/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.fragments;

import java.util.Calendar;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.models.Open311;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
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

public class ReportFragment extends SherlockFragment {
	private JSONObject mService;
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
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			try {
				mService = new JSONObject(savedInstanceState.getString("service"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_report, container, false);
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
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("service", mService.toString());
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
					SherlockDialogFragment picker = new DatePicker(v);
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
	
	private class DatePicker extends SherlockDialogFragment implements OnDateSetListener {
		private TextView mInput;
		
		public DatePicker(View v) {
			mInput = (TextView) v;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar c = Calendar.getInstance();
			return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		}

		@Override
		public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Calendar c = Calendar.getInstance();
			c.set(year, monthOfYear, dayOfMonth);
			mInput.setText(DateFormat.getDateFormat(getActivity()).format(c.getTime()));
		}
	}
}
