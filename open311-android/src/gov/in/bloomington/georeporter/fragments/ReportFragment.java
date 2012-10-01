/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.fragments;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.models.Open311;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class ReportFragment extends SherlockFragment {
	private JSONObject mService;
	private View mView;
	private LayoutInflater mInflater;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mView = mInflater.inflate(R.layout.fragment_report, container, false);
		return mView;
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TextView service_description = (TextView) getView().findViewById(R.id.service_description);
		service_description.setText(mService.optString(Open311.DESCRIPTION));
		
		LinearLayout layout = (LinearLayout) mView.findViewById(R.id.attributes);
		
		if (mService.optBoolean(Open311.METADATA)) {
			JSONObject definition = Open311.sServiceDefinitions.get(mService.opt(Open311.SERVICE_CODE));
			JSONArray  attributes = definition.optJSONArray(Open311.ATTRIBUTES);
			
			int len = attributes.length();
			for (int i=0; i<len; i++) {
				JSONObject a = attributes.optJSONObject(i);
				String description = a.optString(Open311.DESCRIPTION);
				
				View v = mInflater.inflate(R.layout.list_item_report_attributes_string, null);
				TextView t = (TextView) v.findViewById(android.R.id.text1);
				t.setText(description);
				
				layout.addView(v);
			}
		}
	}
	
	
	public void setService(JSONObject service) {
		mService = service;
	}
}
