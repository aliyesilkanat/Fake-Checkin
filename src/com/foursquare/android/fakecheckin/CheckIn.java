package com.foursquare.android.fakecheckin;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
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
	List<Venue> venueList = new ArrayList<Venue>();
	private GoogleMap myMap;
	private SharedPreferences.Editor prefsEditor;
	public ProgressBar prog;
	public View row;
	public static Location staticLocation = new Location("");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_in);

		final ListView lv = (ListView) findViewById(R.id.lvVenues);
		prog = (ProgressBar) findViewById(R.id.progressBar);
		prog.setVisibility(View.GONE);
		// map settings
		android.support.v4.app.FragmentManager myFragmentManager = getSupportFragmentManager();
		SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
				.findFragmentById(R.id.map);
		myMap = mySupportMapFragment.getMap();
		myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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

		final Button btn = (Button) findViewById(R.id.btnRefresh);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
		final Activity act = this;

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				arg1.requestFocusFromTouch();

				new MakeCheckIn().execute(venueList, position, arg1, act,LoadVenues.CONST_LOADVENUES);

			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.check_in, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_search:
			Intent in = new Intent(this,
					com.foursquare.android.fakecheckin.Search.class);
			staticLocation
					.setLatitude(myMap.getCameraPosition().target.latitude);
			staticLocation
					.setLongitude(myMap.getCameraPosition().target.longitude);
			startActivity(in);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void parseVenues(Location ll) {

		ListView lv = (ListView) findViewById(R.id.lvVenues);
		lv.setVisibility(View.GONE);
		venueList = new ArrayList<Venue>();
		try {

			new LoadVenues().execute(ll, venueList, this,
					LoadVenues.CONST_LOADVENUES);
		} catch (Exception e) {

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
