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

import com.foursquare.android.fakecheckin.R.string;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author smsng
 * 
 */
public class LoadVenues extends AsyncTask<Object, View, Activity> {
	List<Venue> venueList;
	JSONArray arrayVenues;
	public static final int CONST_LOADVENUES = 0;
	public static final int CONST_SUGGESTVENUES = 1;
	int loadType;
	String query;

	@Override
	protected Activity doInBackground(Object... params) {

		Location ll = (Location) params[0];
		venueList = (List<Venue>) params[1];

		final Activity act = (Activity) params[2];
		loadType = (Integer) params[3];
		final ProgressBar prog;
		String venueSearchUrl = "";

		ListView nonFinalLv = null;
		ProgressBar nonFinalProg = null;
		if (loadType == CONST_LOADVENUES) {
			act.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					
						act.findViewById(R.id.btnRefresh).setEnabled(false);
				}
			});
			nonFinalProg = (ProgressBar) act.findViewById(R.id.progressBar);
			nonFinalLv = (ListView) act.findViewById(R.id.lvVenues);
			venueSearchUrl = "https://api.foursquare.com/v2/venues/search?ll="
					+ ll.getLatitude() + "," + ll.getLongitude()
					+ "&llAcc=1&altAcc=1&limit=20&intent=checkin&oauth_token="
					+ MainActivity.ACCESS_TOKEN + "&v="
					+ DateSingleton.getDate();
		} else if (loadType == CONST_SUGGESTVENUES) {
			nonFinalProg = (ProgressBar) act
					.findViewById(R.id.progressBarSearch);
			nonFinalLv = (ListView) act.findViewById(R.id.lvSearchVenues);
			query = (String) params[4];
			venueSearchUrl = "https://api.foursquare.com/v2/venues/search?ll="
					+ ll.getLatitude() + "," + ll.getLongitude()
					+ "&llAcc=10&altAcc=10&query=" + query
					+ "&intent=checkin&limit=20&oauth_token="
					+ MainActivity.ACCESS_TOKEN + "&v="
					+ DateSingleton.getDate();
		}
		prog = nonFinalProg;
		final ListView lv = nonFinalLv;
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				prog.setVisibility(View.VISIBLE);
				lv.setVisibility(View.GONE);
			}
		});

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
			arrayVenues = responseJson.getJSONArray("venues");
			if (arrayVenues.length() == 0)

			{
				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(act.getApplicationContext(),
								"Mekan bulunamadý", Toast.LENGTH_LONG).show();

					}
				});
			} else {
				if (loadType == CONST_LOADVENUES) {
					MakeCheckIn.initializeCheckedInArrays(CONST_LOADVENUES,
							arrayVenues.length());
				} else if (loadType == CONST_SUGGESTVENUES) {
					MakeCheckIn.initializeCheckedInArrays(CONST_SUGGESTVENUES,
							arrayVenues.length());
				}
				for (int i = 0; i < arrayVenues.length(); i++) {
					JSONObject jObj = arrayVenues.getJSONObject(i);
					venueList.add(new Venue());
					venueList.get(i).name = jObj.getString("name");
					venueList.get(i).venueId = jObj.getString("id");
					venueList.get(i).address = "";
					venueList.get(i).category = "";
					if (jObj.has("categories")) {
						JSONArray jCategoryArr = jObj
								.getJSONArray("categories");
						if (!jCategoryArr.isNull(0)) {
							JSONObject jCategoryObj = jCategoryArr
									.getJSONObject(0);
							if (jCategoryObj.has("shortName"))
								venueList.get(i).category = jCategoryObj
										.getString("shortName");
						}
						// else
						// venueList[i].category= "";
					}
					String locationStr = jObj.getString("location");
					JSONObject responseLocation = new JSONObject(locationStr);

					venueList.get(i).distance = responseLocation
							.getString("distance");
					if (responseLocation.has("address"))
						venueList.get(i).address = responseLocation
								.getString("address");
					else if (responseLocation.has("city"))
						venueList.get(i).address = responseLocation
								.getString("city");
					else if (responseLocation.has("country"))
						venueList.get(i).address = responseLocation
								.getString("country");
					// else venueList[i].address="";

				}
			}
		} catch (Exception e) {
			Toast.makeText(act.getApplicationContext(), e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		return act;
	}

	@Override
	protected void onPostExecute(final Activity result) {
		super.onPostExecute(result);
		final ListView listView;
		ListView nonFinalLv = null;
		if (loadType == CONST_LOADVENUES) {
			nonFinalLv = (ListView) result.findViewById(R.id.lvVenues);
		} else if (loadType == CONST_SUGGESTVENUES) {
			nonFinalLv = (ListView) result.findViewById(R.id.lvSearchVenues);
		}
		listView = nonFinalLv;
		if (arrayVenues.length() > 0) {
			final List<Map<String, String>> data = new ArrayList<Map<String, String>>();

			for (Venue v : venueList) {
				Map<String, String> datum = new HashMap<String, String>(2);
				datum.put("First Line", v.name);
				datum.put("Second Line", v.category + " / " + v.address + " ("
						+ v.distance + "m)");
				data.add(datum);
			}
			result.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					SimpleAdapter adapter = new SimpleAdapter(result, data,
							android.R.layout.simple_list_item_2, new String[] {
									"First Line", "Second Line" }, new int[] {
									android.R.id.text1, android.R.id.text2 }) {

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {

							if (convertView != null) {
								TextView text1 = ((TextView) convertView
										.findViewById(android.R.id.text1));
								TextView text2 = ((TextView) convertView
										.findViewById(android.R.id.text2));
								if (loadType == CONST_LOADVENUES
										&& MakeCheckIn.checkedInVenuesIds[position]) {
									convertView
											.setBackgroundResource(R.color.custom1);
									text1.setTextColor(Color.BLACK);
									text2.setTextColor(Color.BLACK);
								} else if (loadType == CONST_SUGGESTVENUES
										&& MakeCheckIn.checkedInSuggestedVenuesIds[position]) {
									convertView
											.setBackgroundResource(R.color.custom1);
									text1.setTextColor(Color.BLACK);
									text2.setTextColor(Color.BLACK);
								} else {
									convertView
											.setBackgroundColor(Color.TRANSPARENT);
									text1.setTextColor(Color.WHITE);
									text2.setTextColor(Color.WHITE);
								}
							}
							this.notifyDataSetChanged();

							return super.getView(position, convertView, parent);
						}

					};

					listView.setAdapter(adapter);
					listView.setVisibility(View.VISIBLE);
				}
			});
		}
		// if koslu disina yapýlarak liste bos oldugunda da progressBar ýn
		// donmesi onlendi
		result.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (loadType == CONST_LOADVENUES) {
					result.findViewById(R.id.progressBar).setVisibility(
							View.GONE);
							result.findViewById(R.id.btnRefresh).setEnabled(true);
				} else if (loadType == CONST_SUGGESTVENUES) {
					result.findViewById(R.id.progressBarSearch).setVisibility(
							View.GONE);
				}

			}
		});

	}
}