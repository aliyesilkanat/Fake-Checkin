/**
 * 
 */
package com.foursquare.android.fakecheckin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

/**
 * @author smsng
 * 
 */
public class LoadVenues extends AsyncTask<Object, View, Activity> {
	Venue venueList[];

	@Override
	protected Activity doInBackground(Object... params) throws RuntimeException {
		// TODO Auto-generated method stub

		Location ll = (Location) params[0];
		venueList = (Venue[]) params[1];
		Activity act = (Activity) params[2];
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
				+ "&llAcc=1&altAcc=1&limit=20&intent=checkin&oauth_token="
				+ MainActivity.ACCESS_TOKEN + "&v=20140215";

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
				venueList[i].name = jObj.getString("name");
				venueList[i].venueId = jObj.getString("id");

				JSONArray jCategoryArr = jObj.getJSONArray("categories");
				JSONObject jCategoryObj = jCategoryArr.getJSONObject(0);
				venueList[i].category = jCategoryObj.getString("shortName");

				String locationStr = jObj.getString("location");
				JSONObject responseLocation = new JSONObject(locationStr);
				if (responseLocation.has("address"))

					venueList[i].address = responseLocation
							.getString("address");
				else if (responseLocation.has("city"))
					venueList[i].address = responseLocation.getString("city");
				else
					venueList[i].address = responseLocation
							.getString("country");
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

				List<Map<String, String>> data = new ArrayList<Map<String, String>>();

				for (Venue v : venueList) {
					Map<String, String> datum = new HashMap<String, String>(2);
					datum.put("First Line", v.name);
					datum.put("Second Line", v.category + " / " + v.address);
					data.add(datum);
				}

				SimpleAdapter adapter = new SimpleAdapter(result, data,
						android.R.layout.simple_list_item_2, new String[] {
								"First Line", "Second Line" }, new int[] {
								android.R.id.text1, android.R.id.text2 });

				ProgressBar prog = (ProgressBar) result
						.findViewById(R.id.progressBar);

				prog.setVisibility(View.GONE);
				listView.setAdapter(adapter);
				listView.setVisibility(View.VISIBLE);
			}
		});

	}
}