package com.foursquare.android.fakecheckin;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class Search extends Activity {
	List<Venue> venueSearchList = new ArrayList<Venue>();
	Activity currentAct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		currentAct = this;
		findViewById(R.id.lvSearchVenues).setVisibility(View.GONE);
		findViewById(R.id.progressBarSearch).setVisibility(View.GONE);
		findViewById(R.id.searchImage).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						venueSearchList = new ArrayList<Venue>();
						new LoadVenues().execute(CheckIn.staticLocation,
								venueSearchList, currentAct,
								LoadVenues.CONST_SUGGESTVENUES,
								((EditText) findViewById(R.id.editTxtSearch))
										.getText().toString());
						InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

						inputManager.hideSoftInputFromWindow(getCurrentFocus()
								.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
				});

		EditText editText = (EditText) findViewById(R.id.editTxtSearch);
		editText.requestFocus();
		editText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					venueSearchList = new ArrayList<Venue>();
					new LoadVenues().execute(CheckIn.staticLocation,
							venueSearchList, currentAct,
							LoadVenues.CONST_SUGGESTVENUES,
							((EditText) findViewById(R.id.editTxtSearch))
									.getText().toString());
					InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

					inputManager.hideSoftInputFromWindow(getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					return true;
				}
				return false;
			}
		});
		final Activity act = this;
		ListView lvSuggest = (ListView) findViewById(R.id.lvSearchVenues);
		lvSuggest.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				arg1.requestFocusFromTouch();
				new MakeCheckIn().execute(venueSearchList, arg2, arg1, act,
						LoadVenues.CONST_SUGGESTVENUES);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search, menu);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
