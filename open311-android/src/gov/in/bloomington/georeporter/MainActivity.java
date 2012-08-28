/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import gov.in.bloomington.georeporter.activities.BaseActivity;
import gov.in.bloomington.georeporter.activities.SettingsActivity;
import gov.in.bloomington.georeporter.models.Open311;
import gov.in.bloomington.georeporter.models.Preferences;
import gov.in.bloomington.georeporter.util.Util;

public class MainActivity extends BaseActivity {
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		JSONObject current_server = Preferences.getCurrentServer(this);
		
		if (current_server == null) {
    		startActivity(new Intent(this, SettingsActivity.class));
		}
		else {
			new EndpointLoader().execute(current_server);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask
	 */
	private class EndpointLoader extends AsyncTask<JSONObject, Void, Boolean> {
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.dialog_loading_services), "", true);
		}
		
		@Override
		protected Boolean doInBackground(JSONObject... server) {
			return Open311.setEndpoint(server[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (!result) {
				Util.displayCrashDialog(MainActivity.this, "Failed to load service information");
			}
			super.onPostExecute(result);
		}
	}
}
