package com.foursquare.android.fakecheckin;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SimpleAdapter;

public class CustomAdapter extends SimpleAdapter{

	public CustomAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		// TODO Auto-generated constructor stub
	}

}
