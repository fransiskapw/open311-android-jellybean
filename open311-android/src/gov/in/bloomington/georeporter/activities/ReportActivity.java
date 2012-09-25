/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.activities;

import org.json.JSONObject;

import gov.in.bloomington.georeporter.R;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment;
import gov.in.bloomington.georeporter.fragments.ReportFragment;
import gov.in.bloomington.georeporter.fragments.ChooseGroupFragment.OnGroupSelectedListener;
import gov.in.bloomington.georeporter.fragments.ChooseServiceFragment.OnServiceSelectedListener;
import gov.in.bloomington.georeporter.models.Open311;

import com.actionbarsherlock.app.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ReportActivity extends BaseFragmentActivity implements OnGroupSelectedListener, OnServiceSelectedListener {
	public static final int CHOOSE_LOCATION_REQUEST = 1;
	private ActionBar mActionBar;
	
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
		
		ReportFragment report = new ReportFragment();
		report.setService(service);
		getSupportFragmentManager() .beginTransaction()
									.replace(android.R.id.content, report)
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
				
			}
		}
	}
	
}
