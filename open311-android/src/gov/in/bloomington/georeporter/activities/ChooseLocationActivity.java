/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.activities;

import gov.in.bloomington.georeporter.R;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.maps.MapActivity;

public class ChooseLocationActivity extends MapActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_chooser);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * OnClick handler for the submit button
	 * 
	 * Reads the lat/long at the center of the map and returns
	 * them to the activity that opened the map
	 * 
	 * void
	 */
	public void submit() {
		Intent result = new Intent();
		result.putExtra("latitude", 0);
		result.putExtra("longitude", 0);
		setResult(RESULT_OK, result);
		finish();
	}
	
	/**
	 * OnClick handler for the cancel button
	 * 
	 * void
	 */
	public void cancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

}
