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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CheckIn extends FragmentActivity {
	final String[] names = new String[20];
	final String[] venuesId = new String[20];
	private GoogleMap myMap;
	private SharedPreferences.Editor prefsEditor;
	public ProgressBar prog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_in);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		final ListView lv = (ListView) findViewById(R.id.lvVenues);
		prog = (ProgressBar) findViewById(R.id.progressBar);
		prog.setVisibility(View.GONE);
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

	private void parseVenues(Location ll) {

		ListView lv = (ListView) findViewById(R.id.lvVenues);
		lv.setVisibility(View.GONE);

		// prog.animate();
		try {
			new LoadVenues().execute(ll, names, venuesId, this);
		} catch (Exception e) {
			// In your production code handle any errors and catch the
			// individual exceptions
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		lv.setVisibility(View.VISIBLE);
		return;
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
