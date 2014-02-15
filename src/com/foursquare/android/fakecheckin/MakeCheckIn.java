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

public class MakeCheckIn extends AsyncTask<Object, View, Activity> {
	Venue venueList[];

	@Override
	protected Activity doInBackground(Object... params) {

		venueList = (Venue[]) params[0];
		
		int position = (Integer) params[1];

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"https://api.foursquare.com/v2/checkins/add");
		try {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("venueId",
					venueList[position].venueId));
			nameValuePairs.add(new BasicNameValuePair("oauth_token",
					MainActivity.ACCESS_TOKEN));

			// gecici olarak alttaki satiri tut, program çalýþtýrýrken
			// sil
			nameValuePairs.add(new BasicNameValuePair("broadcast", "private"));

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

		return null;
	}

}
