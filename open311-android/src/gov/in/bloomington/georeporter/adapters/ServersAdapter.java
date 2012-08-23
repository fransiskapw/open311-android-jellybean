/**
 * @copyright 2012 City of Bloomington, Indiana
 * @license http://www.gnu.org/licenses/gpl.txt GNU/GPL, see LICENSE.txt
 * @author Cliff Ingham <inghamn@bloomington.in.gov>
 */
package gov.in.bloomington.georeporter.adapters;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ServersAdapter extends BaseAdapter {
	private JSONArray mServers;
	private static LayoutInflater mInflater;
	
	public ServersAdapter(JSONArray d, Context c) {
		mServers  = d;
		mInflater = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return (mServers == null) ? 0 : mServers.length();
	}

	@Override
	public JSONObject getItem(int position) {
		return mServers.optJSONObject(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private static class ViewHolder {
		public TextView name, url;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(android.R.layout.simple_list_item_2, null);
			holder = new ViewHolder();
			holder.name = (TextView)convertView.findViewById(android.R.id.text1);
			holder.url  = (TextView)convertView.findViewById(android.R.id.text2);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(mServers.optJSONObject(position).optString("name"));
		holder.url .setText(mServers.optJSONObject(position).optString("url"));
		return convertView;
	}
}