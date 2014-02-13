/**
 * 
 */
package com.foursquare.android.fakecheckin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

/**
 * @author smsng
 * 
 */
public class LoadVenues extends AsyncTask<Object, View, Activity> {
	String names[];
	String venuesId[];

	@Override
	protected Activity doInBackground(Object... params) throws RuntimeException {
		// TODO Auto-generated method stub

		Location ll = (Location) params[0];
		names = (String[]) params[1];
		venuesId = (String[]) params[2];
		Activity act = (Activity) params[3];
		final ProgressBar prog = (ProgressBar) act
				.findViewById(R.id.progressBar);
		
		final ListView lv = (ListView) act.findViewById(R.id.lvVenues);
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				prog.setVisibility(View.VISIBLE);
				lv.setVisibility(View.GONE);
			}
		});

		final String venueSearchUrl = "https://api.foursquare.com/v2/venues/search?ll="
				+ ll.getLatitude()
				+ ","
				+ ll.getLongitude()
				+ "&llAcc=1&altAcc=1&limit=20&oauth_token="
				+ MainActivity.ACCESS_TOKEN + "&v=20140212";

		try {
			DefaultHttpClient defaultClient = new DefaultHttpClient();
			HttpGet httpGetRequest = new HttpGet(venueSearchUrl);
			HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpResponse.getEntity().getContent(), "UTF-8"));
			String json = reader.readLine();

			JSONObject jsonObject = new JSONObject(json);
			String s = jsonObject.getString("response");

			JSONObject responseJson = new JSONObject(s);
			JSONArray arrayVenues = responseJson.getJSONArray("venues");
			for (int i = 0; i < arrayVenues.length(); i++) {
				JSONObject jObj = arrayVenues.getJSONObject(i);
				names[i] = jObj.getString("name");
				venuesId[i] = jObj.getString("id");
			}
		} catch (Exception e) {

			throw new RuntimeException();
		}

		return act;
	}

	@Override
	protected void onPostExecute(final Activity result) {
		super.onPostExecute(result);

		final ListView listView = (ListView) result.findViewById(R.id.lvVenues);

		result.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(result,
						android.R.layout.simple_list_item_1, names);
				ProgressBar prog = (ProgressBar) result
						.findViewById(R.id.progressBar);

				prog.setVisibility(View.GONE);
				listView.setAdapter(adapter);
				listView.setVisibility(View.VISIBLE);
			}
		});

	}
}