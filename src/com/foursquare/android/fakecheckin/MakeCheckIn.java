package com.foursquare.android.fakecheckin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MakeCheckIn extends AsyncTask<Object, View, Activity> {
	List<Venue> venueList;
	public static Boolean checkedInSuggestedVenuesIds[];
	public static Boolean checkedInVenuesIds[];
	private int loadType;

	public static void initializeCheckedInArrays(int constant, int size) {
		if (constant == LoadVenues.CONST_LOADVENUES) {
			checkedInVenuesIds = new Boolean[size];
			for (int i = 0; i < checkedInVenuesIds.length; i++) {
				checkedInVenuesIds[i] = new Boolean(false);
			}
		} else if (constant == LoadVenues.CONST_SUGGESTVENUES) {
			checkedInSuggestedVenuesIds = new Boolean[size];
			for (int i = 0; i < checkedInSuggestedVenuesIds.length; i++) {
				checkedInSuggestedVenuesIds[i] = new Boolean(false);
			}
		}
	}

	@Override
	protected Activity doInBackground(Object... params) {

		venueList = (List<Venue>) params[0];
		int position = (Integer) params[1];
		final View view = (View) params[2];
		final Activity act = (Activity) params[3];
		loadType = (Integer) params[4];
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"https://api.foursquare.com/v2/checkins/add");
		try {

			act.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					TextView text = ((TextView) view
							.findViewById(android.R.id.text2));
					text.setText("CheckIn Yapýlýyor...");
				}
			});

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("venueId", venueList
					.get(position).venueId));
			nameValuePairs.add(new BasicNameValuePair("oauth_token",
					MainActivity.ACCESS_TOKEN));

			// gecici olarak alttaki satiri tut, program çalýþtýrýrken
			// sil
			nameValuePairs.add(new BasicNameValuePair("broadcast", "private"));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			if (loadType == LoadVenues.CONST_LOADVENUES)
				checkedInVenuesIds[position] = true;
			else if (loadType == LoadVenues.CONST_SUGGESTVENUES)
				checkedInSuggestedVenuesIds[position] = true;
			act.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					TextView text = ((TextView) view
							.findViewById(android.R.id.text2));
					text.setText("CheckIn Yapýldý");
					ListView listv = null;
					if (loadType == LoadVenues.CONST_LOADVENUES)
						listv = (ListView) act.findViewById(R.id.lvVenues);
					else if(loadType == LoadVenues.CONST_SUGGESTVENUES)
						listv=(ListView) act.findViewById(R.id.lvSearchVenues);
					SimpleAdapter adapter = (SimpleAdapter) listv.getAdapter();
					adapter.notifyDataSetChanged();
				}
			});

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
