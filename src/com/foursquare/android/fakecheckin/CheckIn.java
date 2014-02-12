package com.foursquare.android.fakecheckin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CheckIn extends FragmentActivity {
	final String[] names = new String[20];
	final String[] venuesId = new String[20];
	private GoogleMap myMap;
	private SharedPreferences.Editor prefsEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_in);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		final ListView lv = (ListView) findViewById(R.id.lvVenues);

		// map settings
		android.support.v4.app.FragmentManager myFragmentManager = getSupportFragmentManager();
		SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
				.findFragmentById(R.id.map);
		myMap = mySupportMapFragment.getMap();

		// settting initial location for map and venues
		SharedPreferences sharedPref = getSharedPreferences(
				"fakeCheckInTokenFile", MODE_PRIVATE);
		prefsEditor = sharedPref.edit();
		String latitude = sharedPref.getString("latitude", "0");
		String longitude = sharedPref.getString("longitude", "0");
		final Location ll = new Location("");
		if (!latitude.equals("0")) {
			ll.setLatitude(Double.parseDouble(latitude));
			ll.setLongitude(Double.parseDouble(longitude));
		} else {
			String locationProvider = LocationManager.NETWORK_PROVIDER;
			LocationManager locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			ll.setLatitude(locationManager.getLastKnownLocation(
					locationProvider).getLatitude());
			ll.setLongitude(locationManager.getLastKnownLocation(
					locationProvider).getLongitude());
		}

		parseVenues(ll);
		adjustMap(ll); // sadece ilk seferinde haritayý eski yerine götürmek
						// için

		myMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition position) {
				// TODO Auto-generated method stub
					myMap.clear();
					myMap.addMarker(new MarkerOptions().position(position.target));
					
			}
		});

		final Button btn = (Button) findViewById(R.id.refresh);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ll.setLatitude(myMap.getCameraPosition().target.latitude);
				ll.setLongitude(myMap.getCameraPosition().target.longitude);
				parseVenues(ll);
				prefsEditor.putString("latitude",
						String.valueOf(ll.getLatitude()));
				prefsEditor.putString("longitude",
						String.valueOf(ll.getLongitude()));
				prefsEditor.commit();
			}
		});

		lv.setClickable(true);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"https://api.foursquare.com/v2/checkins/add");

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// arg0.getChildAt(position).setBackgroundColor(Color.RED);
				// arg1.setBackgroundColor(Color.RED);
				// lv.getChildAt(position).setBackgroundColor(Color.RED);
				try {

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("venueId",
							venuesId[position]));
					nameValuePairs.add(new BasicNameValuePair("oauth_token",
							MainActivity.ACCESS_TOKEN));

					// gecici olarak alttaki satiri tut, program çalýþtýrýrken
					// sil
					nameValuePairs.add(new BasicNameValuePair("broadcast",
							"private"));

					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

					// Execute HTTP Post Request
					HttpResponse response = httpclient.execute(httppost);
					// lv.getChildAt(position).setBackgroundColor(Color.BLUE);
					// arg1.setBackgroundColor(Color.BLUE);
					// arg0.getChildAt(position).setBackgroundColor(Color.BLUE);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.check_in, menu);
		return true;
	}

	private Location parseVenues(Location ll) {

		String venueSearchUrl = "https://api.foursquare.com/v2/venues/search?ll="
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
			ListView listView = (ListView) findViewById(R.id.lvVenues);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, names);

			listView.setAdapter(adapter);

		} catch (Exception e) {
			// In your production code handle any errors and catch the
			// individual exceptions
			e.printStackTrace();
		}
		return ll;
	}

	private LatLng adjustMap(Location lastKnownLocation) {

		UiSettings ui = myMap.getUiSettings();

		// ui.setAllGesturesEnabled(false);
		// ui.setMyLocationButtonEnabled(false);
		// ui.setZoomControlsEnabled(false);
		myMap.setMyLocationEnabled(true);

		double lat = lastKnownLocation.getLatitude();
		double lng = lastKnownLocation.getLongitude();
		LatLng ll = new LatLng(lat, lng);
		myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 16));
		return ll;
	}
}
