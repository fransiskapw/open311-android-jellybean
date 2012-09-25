/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.fragments;

import java.util.ArrayList;

import gov.in.bloomington.georeporter.MainActivity;
import gov.in.bloomington.georeporter.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class ReportFragment extends SherlockFragment {
	private JSONObject mService;
	private ArrayList<NameValuePair> mPost;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_report, container, false);
		
		v.findViewById(R.id.button_submit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
		return v;
		
	}
	
	public void setService(JSONObject service) {
		mService = service;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TextView service_description = (TextView) getView().findViewById(R.id.service_description);
		service_description.setText(mService.optString("description"));
		
		mPost = new ArrayList<NameValuePair>();
		mPost.add(new BasicNameValuePair("service_code", mService.optString("service_code")));
	}
}
